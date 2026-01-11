package com.cplps.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.models.BookmarkedProblem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private Context context;
    private List<BookmarkedProblem> bookmarks;
    private OnBookmarkActionListener listener;

    public interface OnBookmarkActionListener {
        void onDeleteBookmark(BookmarkedProblem bookmark);
    }

    public BookmarkAdapter(Context context, OnBookmarkActionListener listener) {
        this.context = context;
        this.bookmarks = new ArrayList<>();
        this.listener = listener;
    }

    public void setBookmarks(List<BookmarkedProblem> bookmarks) {
        this.bookmarks = bookmarks;
        notifyDataSetChanged();
    }

    public void removeBookmark(BookmarkedProblem bookmark) {
        int position = bookmarks.indexOf(bookmark);
        if (position != -1) {
            bookmarks.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        BookmarkedProblem bookmark = bookmarks.get(position);
        holder.bind(bookmark);
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    class BookmarkViewHolder extends RecyclerView.ViewHolder {
        TextView tvProblemCode;
        TextView tvProblemName;
        TextView tvProblemRating;
        TextView tvAddedTime;
        ImageButton btnDelete;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProblemCode = itemView.findViewById(R.id.tv_bookmark_problem_code);
            tvProblemName = itemView.findViewById(R.id.tv_bookmark_problem_name);
            tvProblemRating = itemView.findViewById(R.id.tv_bookmark_problem_rating);
            tvAddedTime = itemView.findViewById(R.id.tv_bookmark_added_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_bookmark);
        }

        public void bind(BookmarkedProblem bookmark) {
            tvProblemCode.setText(bookmark.getProblemCode());
            tvProblemName.setText(bookmark.getProblemName() != null ? bookmark.getProblemName() : "Loading...");

            if (bookmark.getProblemRating() > 0) {
                tvProblemRating.setText(String.valueOf(bookmark.getProblemRating()));
                tvProblemRating.setVisibility(View.VISIBLE);
            } else {
                tvProblemRating.setVisibility(View.GONE);
            }

            tvAddedTime.setText("Added: " + getTimeAgo(bookmark.getAddedAt()));

            // Click on card to open problem URL
            itemView.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bookmark.getProblemUrl()));
                context.startActivity(browserIntent);
            });

            // Delete button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteBookmark(bookmark);
                }
            });
        }

        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long weeks = days / 7;
            long months = days / 30;

            if (months > 0) {
                return months + (months == 1 ? " month ago" : " months ago");
            } else if (weeks > 0) {
                return weeks + (weeks == 1 ? " week ago" : " weeks ago");
            } else if (days > 0) {
                return days + (days == 1 ? " day ago" : " days ago");
            } else if (hours > 0) {
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (minutes > 0) {
                return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
            } else {
                return "Just now";
            }
        }
    }
}
