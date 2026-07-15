package com.example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.models.Complaint;
import com.example.models.Leave;
import com.example.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal RecyclerView Adapter to display and process student requests (Complaints and Leaves).
 * Dynamically switches content types and displays interactive quick action buttons.
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private List<Complaint> complaints = new ArrayList<>();
    private List<Leave> leaves = new ArrayList<>();
    private boolean isComplaintMode = true;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onApprove(int id, boolean isComplaint);
        void onReject(int id, boolean isComplaint);
    }

    public RequestAdapter(OnRequestActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<Complaint> complaintsList, List<Leave> leavesList, boolean isComplaintMode) {
        this.isComplaintMode = isComplaintMode;
        if (isComplaintMode) {
            this.complaints = complaintsList != null ? complaintsList : new ArrayList<>();
        } else {
            this.leaves = leavesList != null ? leavesList : new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isComplaintMode) {
            Complaint c = complaints.get(position);
            holder.tvStudent.setText("Resident ID: " + c.getStudentId() + " (" + (c.getStudentName() != null ? c.getStudentName() : "Student") + ")");
            holder.tvStatus.setText(c.getStatus().toUpperCase());
            holder.tvTitle.setText("[" + c.getCategory() + "] " + c.getTitle());
            holder.tvDetails.setText("Description: " + c.getDescription());
            holder.tvDate.setVisibility(View.GONE);

            // Hide actions if already resolved
            if (!"PENDING".equalsIgnoreCase(c.getStatus())) {
                holder.layoutActions.setVisibility(View.GONE);
                holder.tvStatus.setBackgroundColor(0xFFE8F5E9); // green tint
                holder.tvStatus.setTextColor(0xFF2E7D32);
            } else {
                holder.layoutActions.setVisibility(View.VISIBLE);
                holder.tvStatus.setBackgroundColor(0xFFFFF3E0); // orange tint
                holder.tvStatus.setTextColor(0xFFEF6C00);
            }

            holder.btnApprove.setOnClickListener(v -> listener.onApprove(c.getId(), true));
            holder.btnReject.setOnClickListener(v -> listener.onReject(c.getId(), true));

        } else {
            Leave l = leaves.get(position);
            holder.tvStudent.setText("Resident ID: " + l.getStudentId() + " (" + (l.getStudentName() != null ? l.getStudentName() : "Student") + ")");
            holder.tvStatus.setText(l.getStatus().toUpperCase());
            holder.tvTitle.setText("Destination: " + l.getDestination());
            holder.tvDetails.setText("Reason: " + l.getReason());
            holder.tvDate.setVisibility(View.VISIBLE);
            holder.tvDate.setText("Dates: " + l.getStartDate() + " to " + l.getEndDate() + "\nEmergency Contact: " + l.getEmergencyContact());

            // Hide actions if already resolved
            if (!"PENDING".equalsIgnoreCase(l.getStatus())) {
                holder.layoutActions.setVisibility(View.GONE);
                holder.tvStatus.setBackgroundColor(0xFFE8F5E9); // green
                holder.tvStatus.setTextColor(0xFF2E7D32);
            } else {
                holder.layoutActions.setVisibility(View.VISIBLE);
                holder.tvStatus.setBackgroundColor(0xFFFFF3E0); // orange
                holder.tvStatus.setTextColor(0xFFEF6C00);
            }

            holder.btnApprove.setOnClickListener(v -> listener.onApprove(l.getId(), false));
            holder.btnReject.setOnClickListener(v -> listener.onReject(l.getId(), false));
        }
    }

    @Override
    public int getItemCount() {
        return isComplaintMode ? complaints.size() : leaves.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudent, tvStatus, tvTitle, tvDetails, tvDate;
        View layoutActions;
        Button btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudent = itemView.findViewById(R.id.tv_req_student);
            tvStatus = itemView.findViewById(R.id.tv_req_status);
            tvTitle = itemView.findViewById(R.id.tv_req_title);
            tvDetails = itemView.findViewById(R.id.tv_req_details);
            tvDate = itemView.findViewById(R.id.tv_req_date);
            layoutActions = itemView.findViewById(R.id.layout_req_actions);
            btnApprove = itemView.findViewById(R.id.btn_req_approve);
            btnReject = itemView.findViewById(R.id.btn_req_reject);
        }
    }
}
