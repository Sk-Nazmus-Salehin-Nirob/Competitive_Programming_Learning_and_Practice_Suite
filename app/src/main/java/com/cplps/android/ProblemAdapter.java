package com.cplps.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.api.models.CFProblem;
import java.util.ArrayList;
import java.util.List;

public class ProblemAdapter extends RecyclerView.Adapter<ProblemAdapter.ViewHolder> {

    private List<CFProblem> problems;
    private Context context;

    private java.util.Set<String> solvedProblems = new java.util.HashSet<>();

    public ProblemAdapter(Context context) {
        this.context = context;
        this.problems = new ArrayList<>();
    }

    public void setProblems(List<CFProblem> problems) {
        this.problems = problems;
        notifyDataSetChanged();
    }

    public void setSolvedProblems(java.util.Set<String> solvedProblems) {
        this.solvedProblems = solvedProblems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_problem, parent, false); // Reuse
                                                                                                            // existing
                                                                                                            // layout
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CFProblem problem = problems.get(position);

        String contestId = problem.getContestId() > 0 ? String.valueOf(problem.getContestId()) : "";
        String index = problem.getIndex() != null ? problem.getIndex() : "";
        String code = contestId + index;

        holder.textCode.setText(code);
        holder.textName.setText(problem.getName());

        int rating = problem.getRating();
        String ratingText = "Rating: " + (rating > 0 ? rating : "N/A");
        holder.textRating.setText(ratingText);
        holder.textRating.setTextColor(getRatingColor(rating));

        // Solved Check
        if (solvedProblems.contains(code)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E0F2F1")); // Light Green
            holder.textDate.setVisibility(View.VISIBLE);
            holder.textDate.setText("ACCEPTED");
            holder.textDate.setTextColor(Color.parseColor("#00695C"));
            holder.textDate.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            // Default Tag view
            StringBuilder tags = new StringBuilder();
            if (problem.getTags() != null) {
                for (String tag : problem.getTags()) {
                    if (tags.length() > 0)
                        tags.append(", ");
                    tags.append(tag);
                }
            }
            if (tags.length() > 0) {
                holder.textDate.setVisibility(View.VISIBLE);
                holder.textDate.setText(tags.toString());
                holder.textDate.setTextColor(Color.GRAY);
                holder.textDate.setTypeface(null, android.graphics.Typeface.NORMAL);
            } else {
                holder.textDate.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            String url = "https://codeforces.com/problemset/problem/" + contestId + "/" + index;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return problems.size();
    }

    private int getRatingColor(int rating) {
        if (rating == 0)
            return Color.BLACK;
        if (rating < 1200)
            return Color.GRAY; // Newbie
        if (rating < 1400)
            return Color.GREEN; // Pupil
        if (rating < 1600)
            return Color.parseColor("#03A89E"); // Specialist (Cyan)
        if (rating < 1900)
            return Color.BLUE; // Expert
        if (rating < 2100)
            return Color.parseColor("#AA00AA"); // CM (Violet)
        if (rating < 2300)
            return Color.parseColor("#FF8C00"); // Master (Orange)
        if (rating < 2400)
            return Color.parseColor("#FF8C00"); // Master
        if (rating < 2600)
            return Color.RED; // GM
        if (rating < 3000)
            return Color.RED; // IGM
        return Color.RED; // LGM
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textCode, textName, textRating, textDate;

        ViewHolder(View view) {
            super(view);
            textCode = view.findViewById(R.id.textProblemCode);
            textName = view.findViewById(R.id.textProblemName);
            textRating = view.findViewById(R.id.textProblemRating);
            textDate = view.findViewById(R.id.textProblemDate);
        }
    }
}
