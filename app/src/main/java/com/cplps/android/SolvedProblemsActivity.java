package com.cplps.android;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class SolvedProblemsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textViewNoProblem;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solved_problems);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Solved Problems");
        }

        databaseHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewProblems);
        textViewNoProblem = findViewById(R.id.textViewNoProblems);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSolvedProblems();
    }

    private void loadSolvedProblems() {
        // Get Codeforces platform ID
        Cursor platformCursor = databaseHelper.getPlatform(1, "Codeforces");

        if (platformCursor != null && platformCursor.moveToFirst()) {
            int platformId = platformCursor.getInt(platformCursor.getColumnIndexOrThrow("platform_id"));
            platformCursor.close();

            // Get all solved problems
            Cursor problemsCursor = databaseHelper.getSolvedProblems(platformId);
            List<ProblemItem> problems = new ArrayList<>();

            if (problemsCursor != null && problemsCursor.moveToFirst()) {
                do {
                    String code = problemsCursor.getString(problemsCursor.getColumnIndexOrThrow("problem_code"));
                    String name = problemsCursor.getString(problemsCursor.getColumnIndexOrThrow("problem_name"));
                    int rating = problemsCursor.getInt(problemsCursor.getColumnIndexOrThrow("problem_rating"));

                    problems.add(new ProblemItem(code, name, rating));
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Inner classes
    static class ProblemItem {
        String code, name;
        int rating;

        ProblemItem(String code, String name, int rating) {
            this.code = code;
            this.name = name;
            this.rating = rating;
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
            holder.textRating.setText("Rating: " + (problem.rating > 0 ? problem.rating : "N/A"));
        }

        @Override
        public int getItemCount() {
            return problems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textCode, textName, textRating;

            ViewHolder(View view) {
                super(view);
                textCode = view.findViewById(R.id.textProblemCode);
                textName = view.findViewById(R.id.textProblemName);
                textRating = view.findViewById(R.id.textProblemRating);
            }
        }
    }
}
