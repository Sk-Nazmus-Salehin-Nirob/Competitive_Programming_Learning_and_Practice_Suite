package com.cplps.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.models.LearningTopic;
import java.util.ArrayList;
import java.util.List;

public class LearningTopicAdapter extends RecyclerView.Adapter<LearningTopicAdapter.TopicViewHolder> {

    private Context context;
    private List<LearningTopic> topics;
    private OnTopicClickListener listener;

    public interface OnTopicClickListener {
        void onTopicClick(LearningTopic topic);

        void onTopicLongClick(LearningTopic topic);
    }

    public LearningTopicAdapter(Context context, OnTopicClickListener listener) {
        this.context = context;
        this.topics = new ArrayList<>();
        this.listener = listener;
    }

    public void setTopics(List<LearningTopic> topics) {
        this.topics = topics;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_learning_topic, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        LearningTopic topic = topics.get(position);
        holder.bind(topic);
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }

        public void bind(LearningTopic topic) {
            tvTitle.setText(topic.getTitle());

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onTopicClick(topic);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null)
                    listener.onTopicLongClick(topic);
                return true;
            });
        }
    }
}
