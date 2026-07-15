package com.example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.models.Notice;
import com.example.R;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for binding Notice announcements.
 */
public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    private List<Notice> notices = new ArrayList<>();

    public void setNotices(List<Notice> notices) {
        if (notices != null) {
            this.notices = notices;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notice notice = notices.get(position);
        holder.tvTitle.setText(notice.getTitle());
        holder.tvDesc.setText(notice.getDescription());
        holder.tvPriority.setText(notice.getPriority() != null ? notice.getPriority().toUpperCase() : "MEDIUM");
        holder.tvDate.setText("Published: " + notice.getPublishDate());

        // Set priority badge color
        if ("HIGH".equalsIgnoreCase(notice.getPriority())) {
            holder.tvPriority.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.error));
            holder.tvPriority.setBackgroundColor(0xFFFFEBEE); // soft red
        } else {
            holder.tvPriority.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.info));
            holder.tvPriority.setBackgroundColor(0xFFE3F2FD); // soft blue
        }
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvPriority, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_notice_title);
            tvDesc = itemView.findViewById(R.id.tv_item_notice_desc);
            tvPriority = itemView.findViewById(R.id.tv_item_notice_priority);
            tvDate = itemView.findViewById(R.id.tv_item_notice_date);
        }
    }
}
