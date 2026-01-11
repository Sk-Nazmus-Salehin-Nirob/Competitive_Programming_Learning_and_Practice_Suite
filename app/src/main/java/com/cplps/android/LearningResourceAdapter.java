package com.cplps.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.models.LearningResource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LearningResourceAdapter extends RecyclerView.Adapter<LearningResourceAdapter.ResourceViewHolder> {

    private Context context;
    private List<LearningResource> resources;
    private OnResourceClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public interface OnResourceClickListener {
        void onResourceClick(LearningResource resource);

        void onResourceLongClick(LearningResource resource);
    }

    public LearningResourceAdapter(Context context, OnResourceClickListener listener) {
        this.context = context;
        this.resources = new ArrayList<>();
        this.listener = listener;
    }

    public void setResources(List<LearningResource> resources) {
        this.resources = resources;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_learning_resource, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        LearningResource resource = resources.get(position);
        holder.bind(resource);
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    class ResourceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvContent;
        TextView tvMeta;
        ImageView ivAction;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvMeta = itemView.findViewById(R.id.tv_meta);
            ivAction = itemView.findViewById(R.id.iv_action);
        }

        public void bind(LearningResource resource) {
            String dateStr = dateFormat.format(new Date(resource.getCreatedAt()));

            if (LearningResource.TYPE_FILE.equals(resource.getType())) {
                tvContent.setText(resource.getName());
                tvMeta.setText("File • " + dateStr);
                ivIcon.setImageResource(R.drawable.ic_description); // document icon
                ivAction.setVisibility(View.VISIBLE);
            } else {
                tvContent.setText(resource.getContent());
                tvMeta.setText("Note • " + dateStr);
                ivIcon.setImageResource(R.drawable.ic_description); // note icon, could use different one
                ivAction.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onResourceClick(resource);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null)
                    listener.onResourceLongClick(resource);
                return true;
            });
        }
    }
}
