package com.cplps.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.cplps.android.api.ApiClient;
import com.cplps.android.api.CodeforcesAPI;
import com.cplps.android.api.models.CFProblem;
import com.cplps.android.api.models.CFProblemSet;
import com.cplps.android.api.models.CodeforcesResponse;
import com.cplps.android.utils.SessionManager;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;
    private RecyclerView recyclerView;
    private ProblemAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private CodeforcesAPI api;

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
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setupNavigation();
        setupViews();

        api = ApiClient.getClient().create(CodeforcesAPI.class);

        // Initial load
        fetchProblems();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProblemAdapter(this);
        recyclerView.setAdapter(adapter);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(this::fetchProblems);

        // Filter UI
        btnSelectTopics = findViewById(R.id.btn_select_topics);
        rgLogic = findViewById(R.id.rg_logic);
        spinnerRating = findViewById(R.id.spinner_rating);
        btnApply = findViewById(R.id.btn_apply_filter);
        tvSelectedInfo = findViewById(R.id.tv_selected_info);

        // Rating Spinner
        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, RATINGS);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(ratingAdapter);

        // Topics Dialog
        selectedTopicsBool = new boolean[TOPICS.length];
        btnSelectTopics.setOnClickListener(v -> showTopicDialog());

        // Apply Button
        btnApply.setOnClickListener(v -> applyFilters());
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
        // We fetch ALL problems once (or blindly), then filter locally for complex
        // logic
        // Passing null tags fetches everything.
        api.getProblemSet(null).enqueue(new Callback<CodeforcesResponse<CFProblemSet>>() {
            @Override
            public void onResponse(Call<CodeforcesResponse<CFProblemSet>> call,
                    Response<CodeforcesResponse<CFProblemSet>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && "OK".equals(response.body().getStatus())) {
                    allProblemsCache = response.body().getResult().getProblems();
                    applyFilters(); // Filter logic happens here
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch problems", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CodeforcesResponse<CFProblemSet>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        if (allProblemsCache == null || allProblemsCache.isEmpty())
            return;

        List<CFProblem> filteredList = new ArrayList<>();

        // Filter options
        boolean isAndLogic = rgLogic.getCheckedRadioButtonId() == R.id.rb_and;
        String ratingStr = (String) spinnerRating.getSelectedItem();
        int targetRating = -1;
        if (!"Any".equals(ratingStr)) {
            try {
                targetRating = Integer.parseInt(ratingStr);
            } catch (NumberFormatException ignored) {
            }
        }

        // Iterate through all problems (cached)
        for (CFProblem p : allProblemsCache) {
            // Rating check
            if (targetRating != -1) {
                if (p.getRating() != targetRating)
                    continue;
            }

            // Topics check
            if (!selectedTopics.isEmpty()) {
                List<String> problemTags = p.getTags();
                if (problemTags == null || problemTags.isEmpty())
                    continue;

                if (isAndLogic) {
                    // AND: Problem must have ALL selected topics
                    // Check if selectedTopics is a subset of problemTags
                    if (!new HashSet<>(problemTags).containsAll(selectedTopics))
                        continue;
                } else {
                    // OR: Problem must have AT LEAST ONE selected topic
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

            // Limit to 100 recent matching problems
            if (filteredList.size() >= 100)
                break;
        }

        adapter.setProblems(filteredList);

        // Update info text
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

    private void setupNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                findViewById(R.id.toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_problems) {
            // Already here
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_bookmarks) {
            startActivity(new Intent(this, BookmarksActivity.class));
        } else if (id == R.id.nav_learning) {
            startActivity(new Intent(this, LearningActivity.class));
        } else if (id == R.id.nav_solved_problems) {
            startActivity(new Intent(this, SolvedProblemsActivity.class));
        } else if (id == R.id.nav_notes) {
            startActivity(new Intent(this, NotesActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
