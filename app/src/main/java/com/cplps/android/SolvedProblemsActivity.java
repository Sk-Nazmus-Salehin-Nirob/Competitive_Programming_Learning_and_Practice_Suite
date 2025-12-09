package com.cplps.android;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SolvedProblemsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textViewNoProblem;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solved_problems);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Solved Problems");
        }

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        recyclerView = findViewById(R.id.recyclerViewProblems);
        textViewNoProblem = findViewById(R.id.textViewNoProblems);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSolvedProblems();
    }

    private void loadSolvedProblems() {
        String username = sessionManager.getUsername();
        if (username == null) {
            textViewNoProblem.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        int userId = databaseHelper.getUserIdByUsername(username);
        Cursor platformCursor = databaseHelper.getPlatform(userId, "Codeforces");

        if (platformCursor != null && platformCursor.moveToFirst()) {
            int platformId = platformCursor.getInt(platformCursor.getColumnIndexOrThrow("platform_id"));
            platformCursor.close();

            Cursor problemsCursor = databaseHelper.getSolvedProblems(platformId);
            List<ProblemItem> problems = new ArrayList<>();

            if (problemsCursor != null && problemsCursor.moveToFirst()) {
                do {
                    String code = problemsCursor.getString(problemsCursor.getColumnIndexOrThrow("problem_code"));
                    String name = problemsCursor.getString(problemsCursor.getColumnIndexOrThrow("problem_name"));
                    int rating = problemsCursor.getInt(problemsCursor.getColumnIndexOrThrow("problem_rating"));
                    long solvedAt = problemsCursor.getLong(problemsCursor.getColumnIndexOrThrow("solved_at"));

                    problems.add(new ProblemItem(code, name, rating, solvedAt));
                } while (problemsCursor.moveToNext());

                problemsCursor.close();
            }

            if (problems.isEmpty()) {
                textViewNoProblem.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textViewNoProblem.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(new ProblemsAdapter(problems));
            }
        } else {
            textViewNoProblem.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private int getCodeforcesColor(int rating) {
        if (rating < 1200)
            return getColor(R.color.cf_newbie);
        else if (rating < 1400)
            return getColor(R.color.cf_pupil);
        else if (rating < 1600)
            return getColor(R.color.cf_specialist);
        else if (rating < 1900)
            return getColor(R.color.cf_expert);
        else if (rating < 2100)
            return getColor(R.color.cf_candidate_master);
        else if (rating < 2400)
            return getColor(R.color.cf_master);
        else
            return getColor(R.color.cf_grandmaster);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    static class ProblemItem {
        String code, name;
        int rating;
        long solvedAt;

        ProblemItem(String code, String name, int rating, long solvedAt) {
            this.code = code;
            this.name = name;
            this.rating = rating;
            this.solvedAt = solvedAt;
        }

        String getCodeforcesUrl() {
            String numericPart = code.replaceAll("[^0-9]", "");
            String letterPart = code.replaceAll("[0-9]", "");
            return "https://codeforces.com/problemset/problem/" + numericPart + "/" + letterPart;
        }
    }

    class ProblemsAdapter extends RecyclerView.Adapter<ProblemsAdapter.ViewHolder> {
        List<ProblemItem> problems;

        ProblemsAdapter(List<ProblemItem> problems) {
            this.problems = problems;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_problem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ProblemItem problem = problems.get(position);
            holder.textCode.setText(problem.code);
            holder.textName.setText(problem.name);

            String ratingText = "Rating: " + (problem.rating > 0 ? problem.rating : "N/A");
            holder.textRating.setText(ratingText);
            holder.textRating.setTextColor(getCodeforcesColor(problem.rating));

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new Date(problem.solvedAt * 1000));
            holder.textDate.setText("Solved: " + dateStr);

            holder.itemView.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(problem.getCodeforcesUrl()));
                v.getContext().startActivity(browserIntent);
            });
        }

        @Override
        public int getItemCount() {
            return problems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
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
}
