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
import com.example.adapters.NoticeAdapter;
import com.example.databinding.FragmentStudentDashboardBinding;
import com.example.models.Fee;
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
 * Fragment governing student resident operations, profile detail binding, 
 * support requests, leaves, and fee simulations.
 */
public class StudentDashboardFragment extends Fragment {

    private FragmentStudentDashboardBinding binding;
    private HostelViewModel viewModel;
    private NoticeAdapter noticeAdapter;
    private Student currentStudent;
    private Room currentRoom;
    private Fee currentUnpaidFee;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }

        if (viewModel == null) return;

        // Setup Notices RecyclerView
        noticeAdapter = new NoticeAdapter();
        binding.rvStudentNotices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStudentNotices.setAdapter(noticeAdapter);

        // Setup Tab Navigation
        binding.studentTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                binding.panelRoomProfile.setVisibility(pos == 0 ? View.VISIBLE : View.GONE);
                binding.panelComplaintsLeaves.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
                binding.panelFeesVisitors.setVisibility(pos == 2 ? View.VISIBLE : View.GONE);
                binding.panelNotices.setVisibility(pos == 3 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Observe Logged In Student to populate layout details
        viewModel.getLoggedInStudent().observe(getViewLifecycleOwner(), student -> {
            if (student == null) {
                // Not logged in, escape
                return;
            }
            this.currentStudent = student;
            binding.tvStudentWelcome.setText("Welcome, " + student.getFullName());
            binding.tvProfileName.setText("Name: " + student.getFullName());
            binding.tvProfileReg.setText("Reg Number: " + student.getRegNumber());
            binding.tvProfileDept.setText("Department: " + (student.getDepartment() != null ? student.getDepartment() : "Not Set"));
            binding.tvProfileEmail.setText("Email: " + student.getEmail());
            binding.tvProfilePhone.setText("Phone: " + student.getPhone());

            // Check room allocation
            Room r = viewModel.getRoomForStudent(student.getId());
            this.currentRoom = r;
            if (r != null) {
                binding.tvAllocatedRoomNum.setText("Room Number: " + r.getRoomNumber());
                binding.tvAllocatedBlock.setText("Block Code: " + r.getBlockId() + " (Resident Wing)");
                binding.tvAllocatedType.setText("Sharing Type: " + r.getRoomType());
            } else {
                binding.tvAllocatedRoomNum.setText("Room Number: Not Allocated Yet");
                binding.tvAllocatedBlock.setText("Block Code: -");
                binding.tvAllocatedType.setText("Sharing Type: -");
            }

            // Load student specific finances
            loadFeesDetails(student.getId());
        });

        // Observe Notices Board List
        viewModel.getNotices().observe(getViewLifecycleOwner(), notices -> {
            noticeAdapter.setNotices(notices);
        });

        // Observe Fees List Updates
        viewModel.getFees().observe(getViewLifecycleOwner(), fees -> {
            if (currentStudent != null) {
                loadFeesDetails(currentStudent.getId());
            }
        });

        // Logout student
        binding.btnStudentLogout.setOnClickListener(v -> {
            viewModel.logout();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(new SplashFragment(), false);
            }
        });

        // Action: Submit Support Complaint
        binding.btnSubmitComplaint.setOnClickListener(v -> {
            String title = binding.etComplaintTitle.getText().toString().trim();
            String cat = binding.etComplaintCategory.getText().toString().trim();
            String desc = binding.etComplaintDesc.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(cat) || TextUtils.isEmpty(desc)) {
                Toast.makeText(requireContext(), "All complaint details are required", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = viewModel.submitComplaint(cat, title, desc);
            if (success) {
                binding.etComplaintTitle.setText("");
                binding.etComplaintCategory.setText("");
                binding.etComplaintDesc.setText("");
                Snackbar.make(binding.getRoot(), "Support ticket opened successfully!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Apply Leave Form
        binding.btnApplyLeave.setOnClickListener(v -> {
            String start = binding.etLeaveStart.getText().toString().trim();
            String end = binding.etLeaveEnd.getText().toString().trim();
            String dest = binding.etLeaveDest.getText().toString().trim();
            String reason = binding.etLeaveReason.getText().toString().trim();

            if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end) || TextUtils.isEmpty(dest) || TextUtils.isEmpty(reason)) {
                Toast.makeText(requireContext(), "All leave form parameters are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String contact = currentStudent != null ? currentStudent.getPhone() : "-";
            boolean success = viewModel.applyLeave(start, end, dest, reason, contact);
            if (success) {
                binding.etLeaveStart.setText("");
                binding.etLeaveEnd.setText("");
                binding.etLeaveDest.setText("");
                binding.etLeaveReason.setText("");
                Snackbar.make(binding.getRoot(), "Leave application submitted to Warden", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Submit Visitor Access Pass
        binding.btnRequestVisitor.setOnClickListener(v -> {
            String name = binding.etVisitorName.getText().toString().trim();
            String rel = binding.etVisitorRelation.getText().toString().trim();
            String cnic = binding.etVisitorCnic.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(rel) || TextUtils.isEmpty(cnic)) {
                Toast.makeText(requireContext(), "All visitor details are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            String time = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());

            boolean success = viewModel.requestVisitor(name, rel, cnic, "000-000", date, time, "Family Visit");
            if (success) {
                binding.etVisitorName.setText("");
                binding.etVisitorRelation.setText("");
                binding.etVisitorCnic.setText("");
                Snackbar.make(binding.getRoot(), "Visitor Access pass requested successfully!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Pay Dues Mock
        binding.btnPayDuesMock.setOnClickListener(v -> {
            if (currentUnpaidFee == null) return;
            String payDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            boolean success = viewModel.payFee(currentUnpaidFee.getId(), payDate);
            if (success) {
                Snackbar.make(binding.getRoot(), "Fee bill paid offline successfully!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Action: Generate Resident Card
        binding.btnDownloadId.setOnClickListener(v -> {
            showIdentityCardDialog();
        });
    }

    private void loadFeesDetails(int studentId) {
        List<Fee> fees = viewModel.getFees().getValue();
        this.currentUnpaidFee = null;
        if (fees != null) {
            for (Fee f : fees) {
                if (f.getStudentId() == studentId && "UNPAID".equalsIgnoreCase(f.getStatus())) {
                    this.currentUnpaidFee = f;
                    break;
                }
            }
        }

        if (currentUnpaidFee != null) {
            binding.tvUnpaidFeeText.setText("Pending Dues: $" + currentUnpaidFee.getAmount() + " (" + currentUnpaidFee.getCategory() + ")");
            binding.tvUnpaidFeeText.setTextColor(getResources().getColor(R.color.error));
            binding.btnPayDuesMock.setVisibility(View.VISIBLE);
        } else {
            binding.tvUnpaidFeeText.setText("Pending Dues: None (All settled)");
            binding.tvUnpaidFeeText.setTextColor(getResources().getColor(R.color.success));
            binding.btnPayDuesMock.setVisibility(View.GONE);
        }
    }

    private void showIdentityCardDialog() {
        if (currentStudent == null) return;

        String cardText = "HOSTEL ASSOCIATION PRO IDENTITY CARD\n\n"
                + "===================================\n"
                + "Full Name: " + currentStudent.getFullName() + "\n"
                + "Reg Number: " + currentStudent.getRegNumber() + "\n"
                + "Department: " + (currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "General") + "\n"
                + "Blood Group: " + (currentStudent.getBloodGroup() != null ? currentStudent.getBloodGroup() : "O+") + "\n"
                + "CNIC Number: " + currentStudent.getCnic() + "\n"
                + "Room Assigned: " + (currentRoom != null ? currentRoom.getRoomNumber() : "PENDING ALLOCATION") + "\n"
                + "===================================\n\n"
                + "Verification Status: Active Local Resident (Verified Offline)";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Resident Identity Card")
                .setMessage(cardText)
                .setPositiveButton("Close ID", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
