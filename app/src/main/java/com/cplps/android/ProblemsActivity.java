package com.cplps.android;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.cplps.android.api.ApiClient;
import com.cplps.android.api.CodeforcesAPI;
import com.cplps.android.api.models.CFProblem;
import com.cplps.android.api.models.CFProblemSet;
import com.cplps.android.api.models.CodeforcesResponse;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.utils.SessionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProblemsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProblemAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private CodeforcesAPI api;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    // Filter UI
    private Button btnSelectTopics;
    private RadioGroup rgLogic;
    private Spinner spinnerRating;
    private Button btnApply;
    private TextView tvSelectedInfo;

    // Data
    private List<CFProblem> allProblemsCache = new ArrayList<>();
    private boolean[] selectedTopicsBool;
    private List<String> selectedTopics = new ArrayList<>();

    // Constants
    private final String[] TOPICS = {
            "2-sat", "binary search", "bitmasks", "brute force", "chinese remainder theorem",
            "combinatorics", "constructive algorithms", "data structures", "dfs and similar",
            "divide and conquer", "dp", "dsu", "expression parsing", "fft", "flows", "games",
            "geometry", "graph matchings", "graphs", "greedy", "hashing", "implementation",
            "interactive", "math", "matrices", "meet-in-the-middle", "number theory",
            "probabilities", "schedules", "shortest paths", "sortings", "string suffix structures",
            "strings", "ternary search", "trees", "two pointers"
    };

    private final String[] RATINGS = {
            "Any", "800", "900", "1000", "1100", "1200", "1300", "1400", "1500", "1600",
            "1700", "1800", "1900", "2000", "2100", "2200", "2300", "2400", "2500", "2600",
            "2700", "2800", "2900", "3000", "3100", "3200", "3300", "3400", "3500"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problems);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Problems");
        }

        setupViews();
        api = ApiClient.getCodeforcesAPI();

        loadSolvedProblems();
        fetchProblems();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProblemAdapter(this);
        recyclerView.setAdapter(adapter);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(this::fetchProblems);

        btnSelectTopics = findViewById(R.id.btn_select_topics);
        rgLogic = findViewById(R.id.rg_logic);
        spinnerRating = findViewById(R.id.spinner_rating);
        btnApply = findViewById(R.id.btn_apply_filter);
        tvSelectedInfo = findViewById(R.id.tv_selected_info);

        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, RATINGS);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(ratingAdapter);

        selectedTopicsBool = new boolean[TOPICS.length];
        btnSelectTopics.setOnClickListener(v -> showTopicDialog());

        btnApply.setOnClickListener(v -> applyFilters());
    }

    private void loadSolvedProblems() {
        // Fetch solved problems from DB for specific user if possible, or all for
        // simplicity since we filter by current user usually
        // But DatabaseHelper.getSolvedProblems() needs platformId.
        // Assuming current user's Codeforces platform ID.

        new Thread(() -> {
            try {
                int userId = dbHelper.getUserIdByUsername(sessionManager.getUsername());
                Cursor platformCursor = dbHelper.getPlatform(userId, "Codeforces");
                int platformId = -1;
                if (platformCursor != null && platformCursor.moveToFirst()) {
                    platformId = platformCursor.getInt(platformCursor.getColumnIndexOrThrow("platform_id"));
                    platformCursor.close();
                }

                Set<String> solved = new HashSet<>();
                if (platformId != -1) {
                    Cursor c = dbHelper.getSolvedProblems(platformId);
                    if (c != null && c.moveToFirst()) {
                        do {
                            String code = c.getString(c.getColumnIndexOrThrow("problem_code"));
                            solved.add(code);
                        } while (c.moveToNext());
                        c.close();
                    }
                }

                runOnUiThread(() -> adapter.setSolvedProblems(solved));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showTopicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Topics");
        builder.setMultiChoiceItems(TOPICS, selectedTopicsBool, (dialog, which, isChecked) -> {
            selectedTopicsBool[which] = isChecked;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            updateSelectedTopicsList();
        });
        builder.setNeutralButton("Clear All", (dialog, which) -> {
            Arrays.fill(selectedTopicsBool, false);
            updateSelectedTopicsList();
        });
        builder.show();
    }

    private void updateSelectedTopicsList() {
        selectedTopics.clear();
        for (int i = 0; i < TOPICS.length; i++) {
            if (selectedTopicsBool[i]) {
                selectedTopics.add(TOPICS[i]);
            }
        }
        btnSelectTopics.setText(selectedTopics.isEmpty() ? "Select Topics" : "Topics (" + selectedTopics.size() + ")");
    }

    private void fetchProblems() {
        swipeRefresh.setRefreshing(true);
        api.getProblemSet(null).enqueue(new Callback<CodeforcesResponse<CFProblemSet>>() {
            @Override
            public void onResponse(Call<CodeforcesResponse<CFProblemSet>> call,
                    Response<CodeforcesResponse<CFProblemSet>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && "OK".equals(response.body().getStatus())) {
                    allProblemsCache = response.body().getResult().getProblems();
                    applyFilters();
                } else {
                    Toast.makeText(ProblemsActivity.this, "Failed to fetch problems", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CodeforcesResponse<CFProblemSet>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(ProblemsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        if (allProblemsCache == null || allProblemsCache.isEmpty())
            return;

        List<CFProblem> filteredList = new ArrayList<>();

        boolean isAndLogic = rgLogic.getCheckedRadioButtonId() == R.id.rb_and;
        String ratingStr = (String) spinnerRating.getSelectedItem();
        int targetRating = -1;
        if (!"Any".equals(ratingStr)) {
            try {
                targetRating = Integer.parseInt(ratingStr);
            } catch (NumberFormatException ignored) {
            }
        }

        for (CFProblem p : allProblemsCache) {
            if (targetRating != -1) {
                if (p.getRating() != targetRating)
                    continue;
            }

            if (!selectedTopics.isEmpty()) {
                List<String> problemTags = p.getTags();
                if (problemTags == null || problemTags.isEmpty())
                    continue;

                if (isAndLogic) {
                    if (!new HashSet<>(problemTags).containsAll(selectedTopics))
                        continue;
                } else {
                    boolean match = false;
                    for (String topic : selectedTopics) {
                        if (problemTags.contains(topic)) {
                            match = true;
                            break;
                        }
                    }
                    if (!match)
                        continue;
                }
            }

            filteredList.add(p);
            if (filteredList.size() >= 100)
                break;
        }

        adapter.setProblems(filteredList);

        String logic = isAndLogic ? "AND" : "OR";
        String info = "Showing top " + filteredList.size() + " matches. " +
                (selectedTopics.isEmpty() ? "No topics selected."
                        : "Topics: " + selectedTopics.size() + " (" + logic + ")");
        if (targetRating != -1)
            info += " | Rating: " + targetRating;
        tvSelectedInfo.setText(info);

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No problems found matching filters", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
