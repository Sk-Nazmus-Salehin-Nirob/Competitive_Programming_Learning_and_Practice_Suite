package com.cplps.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.models.BookmarkCategory;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<BookmarkCategory> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(BookmarkCategory category);

        void onCategoryLongClick(BookmarkCategory category);
    }

    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = new ArrayList<>();
        this.listener = listener;
    }

    public void setCategories(List<BookmarkCategory> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        BookmarkCategory category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvCount;
        CardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvCount = itemView.findViewById(R.id.tv_problem_count);
            cardView = itemView.findViewById(R.id.card_category);
        }

        public void bind(BookmarkCategory category) {
            tvName.setText(category.getCategoryName());
            tvCount.setText(category.getProblemCount() + " problems");

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onCategoryClick(category);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null)
                    listener.onCategoryLongClick(category);
                return true;
            });
        }
    }
}
