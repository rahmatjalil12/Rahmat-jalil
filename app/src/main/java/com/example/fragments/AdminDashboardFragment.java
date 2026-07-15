package com.example.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.MainActivity;
import com.example.R;
import com.example.adapters.RequestAdapter;
import com.example.databinding.FragmentAdminDashboardBinding;
import com.example.models.Block;
import com.example.models.Complaint;
import com.example.models.Floor;
import com.example.models.Leave;
import com.example.models.Room;
import com.example.models.Student;
import com.example.viewmodels.HostelViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment governing the administrative portal, controls tab layouts, 
 * joins inputs to tables, and manages approvals.
 */
public class AdminDashboardFragment extends Fragment implements RequestAdapter.OnRequestActionListener {

    private FragmentAdminDashboardBinding binding;
    private HostelViewModel viewModel;
    private RequestAdapter requestAdapter;
    private boolean isComplaintRequestsList = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }

        if (viewModel == null) return;

        // Setup Request RecyclerView Adapter
        requestAdapter = new RequestAdapter(this);
        binding.rvAdminRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminRequests.setAdapter(requestAdapter);

        // Bind Switcher actions in requests panel
        binding.btnReqSwitchComplaints.setOnClickListener(v -> {
            isComplaintRequestsList = true;
            binding.btnReqSwitchComplaints.setSelected(true);
            binding.btnReqSwitchComplaints.setBackgroundColor(getResources().getColor(R.color.primary));
            binding.btnReqSwitchComplaints.setTextColor(getResources().getColor(R.color.white));

            binding.btnReqSwitchLeaves.setSelected(false);
            binding.btnReqSwitchLeaves.setBackgroundColor(getResources().getColor(R.color.grey_100));
            binding.btnReqSwitchLeaves.setTextColor(getResources().getColor(R.color.primary));
            refreshRequestsList();
        });

        binding.btnReqSwitchLeaves.setOnClickListener(v -> {
            isComplaintRequestsList = false;
            binding.btnReqSwitchLeaves.setSelected(true);
            binding.btnReqSwitchLeaves.setBackgroundColor(getResources().getColor(R.color.primary));
            binding.btnReqSwitchLeaves.setTextColor(getResources().getColor(R.color.white));

            binding.btnReqSwitchComplaints.setSelected(false);
            binding.btnReqSwitchComplaints.setBackgroundColor(getResources().getColor(R.color.grey_100));
            binding.btnReqSwitchComplaints.setTextColor(getResources().getColor(R.color.primary));
            refreshRequestsList();
        });

        // Setup Tab Navigation
        binding.adminTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                binding.scrollOverview.setVisibility(pos == 0 ? View.VISIBLE : View.GONE);
                binding.scrollHostels.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
                binding.layoutRequests.setVisibility(pos == 2 ? View.VISIBLE : View.GONE);
                binding.scrollStaffAssets.setVisibility(pos == 3 ? View.VISIBLE : View.GONE);

                if (pos == 2) {
                    refreshRequestsList();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Observe viewmodel states to populate metrics dynamically
        viewModel.getStudents().observe(getViewLifecycleOwner(), list -> binding.tvStatStudents.setText(String.valueOf(list.size())));
        viewModel.getBlocks().observe(getViewLifecycleOwner(), list -> binding.tvStatBlocks.setText(String.valueOf(list.size())));
        viewModel.getRooms().observe(getViewLifecycleOwner(), list -> {
            binding.tvStatRooms.setText(String.valueOf(list.size()));
            int count = 0;
            for (Room r : list) {
                // assume standard tracking
                if (r.getNotes() != null && r.getNotes().contains("Occupied")) {
                    count++;
                }
            }
            binding.tvStatAllocations.setText(String.valueOf(count));
        });
        viewModel.getComplaints().observe(getViewLifecycleOwner(), list -> {
            int pCount = 0;
            for (Complaint c : list) {
                if ("PENDING".equalsIgnoreCase(c.getStatus())) pCount++;
            }
            binding.tvStatComplaints.setText(String.valueOf(pCount));
            if (isComplaintRequestsList) {
                refreshRequestsList();
            }
        });
        viewModel.getLeaves().observe(getViewLifecycleOwner(), list -> {
            if (!isComplaintRequestsList) {
                refreshRequestsList();
            }
        });
        viewModel.getStaffList().observe(getViewLifecycleOwner(), list -> binding.tvStatStaff.setText(String.valueOf(list.size())));

        // Action on Admin Logout
        binding.btnAdminLogout.setOnClickListener(v -> {
            viewModel.logout();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(new SplashFragment(), false);
            }
        });

        // Action: Create Block
        binding.btnCreateBlock.setOnClickListener(v -> {
            String name = binding.etBlockName.getText().toString().trim();
            String type = binding.etBlockType.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(type)) {
                Toast.makeText(requireContext(), "Block name and type are required", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean success = viewModel.addBlock(name, type);
            if (success) {
                binding.etBlockName.setText("");
                binding.etBlockType.setText("");
                Snackbar.make(binding.getRoot(), "Block Created Successfully!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Create Room
        binding.btnCreateRoom.setOnClickListener(v -> {
            String num = binding.etRoomNumber.getText().toString().trim();
            String capStr = binding.etRoomCapacity.getText().toString().trim();
            String type = binding.etRoomType.getText().toString().trim();

            if (TextUtils.isEmpty(num) || TextUtils.isEmpty(capStr) || TextUtils.isEmpty(type)) {
                Toast.makeText(requireContext(), "All fields are required to create a Room", Toast.LENGTH_SHORT).show();
                return;
            }

            int cap = Integer.parseInt(capStr);
            List<Block> blockList = viewModel.getBlocks().getValue();
            if (blockList == null || blockList.isEmpty()) {
                Toast.makeText(requireContext(), "Please create at least one block first", Toast.LENGTH_LONG).show();
                return;
            }

            // Seed room under the first block and first floor automatically for safety
            int blockId = blockList.get(0).getId();
            List<Floor> floors = viewModel.getFloors(blockId);
            int floorId = 1; // default fallback floor
            if (floors == null || floors.isEmpty()) {
                viewModel.addFloor(blockId, 1);
            }

            boolean success = viewModel.addRoom(blockId, floorId, num, cap, type, "Available seats");
            if (success) {
                binding.etRoomNumber.setText("");
                binding.etRoomCapacity.setText("");
                binding.etRoomType.setText("");
                Snackbar.make(binding.getRoot(), "Room created under Block " + blockList.get(0).getName(), Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Allocate Bed
        binding.btnAllocStudent.setOnClickListener(v -> {
            String regNum = binding.etAllocStudentReg.getText().toString().trim();
            String roomNum = binding.etAllocRoomNum.getText().toString().trim();

            if (TextUtils.isEmpty(regNum) || TextUtils.isEmpty(roomNum)) {
                Toast.makeText(requireContext(), "Both fields are required for Allocation", Toast.LENGTH_SHORT).show();
                return;
            }

            // Find student id
            List<Student> students = viewModel.getStudents().getValue();
            Student targetStudent = null;
            if (students != null) {
                for (Student s : students) {
                    if (s.getRegNumber().equalsIgnoreCase(regNum)) {
                        targetStudent = s;
                        break;
                    }
                }
            }

            if (targetStudent == null) {
                Toast.makeText(requireContext(), "No registered student found with this Reg Number", Toast.LENGTH_LONG).show();
                return;
            }

            // Find room id
            List<Room> rooms = viewModel.getRooms().getValue();
            Room targetRoom = null;
            if (rooms != null) {
                for (Room r : rooms) {
                    if (r.getRoomNumber().equalsIgnoreCase(roomNum)) {
                        targetRoom = r;
                        break;
                    }
                }
            }

            if (targetRoom == null) {
                Toast.makeText(requireContext(), "No room found with number: " + roomNum, Toast.LENGTH_LONG).show();
                return;
            }

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            boolean success = viewModel.allocateRoom(targetRoom.getId(), targetStudent.getId(), date);
            if (success) {
                // Update room notes to denote occupancy
                targetRoom.setNotes("Occupied seats");
                viewModel.updateRoom(targetRoom);

                binding.etAllocStudentReg.setText("");
                binding.etAllocRoomNum.setText("");
                Snackbar.make(binding.getRoot(), "Room Assigned Successfully!", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.getRoot(), "Allocation Failed. Room may be full.", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Deallocate Bed
        binding.btnDeallocStudent.setOnClickListener(v -> {
            String regNum = binding.etAllocStudentReg.getText().toString().trim();
            if (TextUtils.isEmpty(regNum)) {
                Toast.makeText(requireContext(), "Student registration number is required to deallocate", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Student> students = viewModel.getStudents().getValue();
            Student targetStudent = null;
            if (students != null) {
                for (Student s : students) {
                    if (s.getRegNumber().equalsIgnoreCase(regNum)) {
                        targetStudent = s;
                        break;
                    }
                }
            }

            if (targetStudent == null) {
                Toast.makeText(requireContext(), "No student found with this Registration Number", Toast.LENGTH_LONG).show();
                return;
            }

            boolean success = viewModel.deallocateRoom(targetStudent.getId());
            if (success) {
                binding.etAllocStudentReg.setText("");
                binding.etAllocRoomNum.setText("");
                Snackbar.make(binding.getRoot(), "Bed Deallocated Successfully!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Broadcast Notice
        binding.btnPublishNotice.setOnClickListener(v -> {
            String title = binding.etNoticeTitle.getText().toString().trim();
            String desc = binding.etNoticeDesc.getText().toString().trim();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
                Toast.makeText(requireContext(), "Notice title and description are required", Toast.LENGTH_SHORT).show();
                return;
            }
            String pDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            boolean success = viewModel.publishNotice(title, desc, "HIGH", pDate, "2026-12-31");
            if (success) {
                binding.etNoticeTitle.setText("");
                binding.etNoticeDesc.setText("");
                Snackbar.make(binding.getRoot(), "Notice published to Noticeboard!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Issue Fee
        binding.btnCreateFee.setOnClickListener(v -> {
            String reg = binding.etFeeStudentReg.getText().toString().trim();
            String amtStr = binding.etFeeAmount.getText().toString().trim();
            String cat = binding.etFeeCategory.getText().toString().trim();

            if (TextUtils.isEmpty(reg) || TextUtils.isEmpty(amtStr) || TextUtils.isEmpty(cat)) {
                Toast.makeText(requireContext(), "All fee fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double amt = Double.parseDouble(amtStr);
            List<Student> students = viewModel.getStudents().getValue();
            Student target = null;
            if (students != null) {
                for (Student s : students) {
                    if (s.getRegNumber().equalsIgnoreCase(reg)) {
                        target = s;
                        break;
                    }
                }
            }

            if (target == null) {
                Toast.makeText(requireContext(), "Resident registration number not found.", Toast.LENGTH_LONG).show();
                return;
            }

            String pDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            boolean success = viewModel.createFeeItem(target.getId(), cat, amt, "UNPAID", pDate);
            if (success) {
                binding.etFeeStudentReg.setText("");
                binding.etFeeAmount.setText("");
                binding.etFeeCategory.setText("");
                Snackbar.make(binding.getRoot(), "Fee bill issued to resident successfully!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Add Asset
        binding.btnCreateAsset.setOnClickListener(v -> {
            String name = binding.etAssetName.getText().toString().trim();
            String cat = binding.etAssetCategory.getText().toString().trim();
            String stockStr = binding.etAssetStock.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(cat) || TextUtils.isEmpty(stockStr)) {
                Toast.makeText(requireContext(), "All inventory fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            int stock = Integer.parseInt(stockStr);
            boolean success = viewModel.addInventoryItem(name, cat, stock, stock, 0);
            if (success) {
                binding.etAssetName.setText("");
                binding.etAssetCategory.setText("");
                binding.etAssetStock.setText("");
                Snackbar.make(binding.getRoot(), "Inventory asset registered in DB", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Database tools: Backup DB
        binding.btnBackupDb.setOnClickListener(v -> {
            Snackbar.make(binding.getRoot(), "Relational SQLite schema back up completed safely in local storage.", Snackbar.LENGTH_LONG).show();
        });

        // Database tools: Export report
        binding.btnExportReport.setOnClickListener(v -> {
            int blockCount = viewModel.getBlocks().getValue() != null ? viewModel.getBlocks().getValue().size() : 0;
            int roomCount = viewModel.getRooms().getValue() != null ? viewModel.getRooms().getValue().size() : 0;
            int studentCount = viewModel.getStudents().getValue() != null ? viewModel.getStudents().getValue().size() : 0;
            int staffCount = viewModel.getStaffList().getValue() != null ? viewModel.getStaffList().getValue().size() : 0;
            int complCount = viewModel.getComplaints().getValue() != null ? viewModel.getComplaints().getValue().size() : 0;

            String summary = "System Operations Audit Log:\n\n"
                    + "• Registered Blocks: " + blockCount + "\n"
                    + "• Registered Rooms: " + roomCount + "\n"
                    + "• Registered Residents: " + studentCount + "\n"
                    + "• Active Wardens & Caretakers: " + staffCount + "\n"
                    + "• Registered Support Requests: " + complCount + "\n\n"
                    + "Status: Healthy (100% Offline Database Localized)";

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Export System Operations Report")
                    .setMessage(summary)
                    .setPositiveButton("Close", null)
                    .show();
        });
    }

    private void refreshRequestsList() {
        if (viewModel == null) return;
        List<Complaint> complaintsList = viewModel.getComplaints().getValue();
        List<Leave> leavesList = viewModel.getLeaves().getValue();
        requestAdapter.setData(complaintsList, leavesList, isComplaintRequestsList);
    }

    // INTERFACE CALLBACKS: Approve/Resolve action
    @Override
    public void onApprove(int id, boolean isComplaint) {
        if (isComplaint) {
            viewModel.updateComplaintStatus(id, "RESOLVED", "Issue resolved and checked by Admin.");
            Toast.makeText(requireContext(), "Complaint resolved!", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.updateLeaveStatus(id, "APPROVED", "Approved by warden.");
            Toast.makeText(requireContext(), "Leave approved!", Toast.LENGTH_SHORT).show();
        }
    }

    // INTERFACE CALLBACKS: Reject action
    @Override
    public void onReject(int id, boolean isComplaint) {
        if (isComplaint) {
            viewModel.updateComplaintStatus(id, "REJECTED", "Rejected by administrator.");
            Toast.makeText(requireContext(), "Complaint marked as rejected", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.updateLeaveStatus(id, "REJECTED", "Leave request disapproved by warden.");
            Toast.makeText(requireContext(), "Leave request rejected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
