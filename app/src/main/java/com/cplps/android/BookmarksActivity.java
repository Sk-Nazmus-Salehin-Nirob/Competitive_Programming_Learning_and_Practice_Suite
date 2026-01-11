package com.cplps.android;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.cplps.android.api.ApiClient;
import com.cplps.android.api.CodeforcesAPI;
import com.cplps.android.api.models.CFProblem;
import com.cplps.android.api.models.CodeforcesResponse;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookmarksActivity extends AppCompatActivity {

    private static final String TAG = "BookmarksActivity";

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddBookmark;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private CodeforcesAPI codeforcesAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        // Initialize
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        codeforcesAPI = ApiClient.getCodeforcesAPI();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup ViewPager and Tabs
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        fabAddBookmark = findViewById(R.id.fab_add_bookmark);

        setupViewPager();

        // FAB click
        fabAddBookmark.setOnClickListener(v -> showAddBookmarkDialog());
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(BookmarkListFragment.newInstance("to_solve"), "Problems to solve");
        adapter.addFragment(BookmarkListFragment.newInstance("interesting"), "Interesting Problems");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void showAddBookmarkDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_bookmark);
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);

        TextInputEditText etProblemUrl = dialog.findViewById(R.id.et_problem_url);
        RadioButton radioToSolve = dialog.findViewById(R.id.radio_to_solve);
        RadioButton radioInteresting = dialog.findViewById(R.id.radio_interesting);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnAdd = dialog.findViewById(R.id.btn_add);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String url = etProblemUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Please enter a problem URL", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = radioToSolve.isChecked() ? "to_solve" : "interesting";

            // Parse URL and fetch problem details
            parseProblemUrl(url, category, dialog);
        });

        dialog.show();
    }

    private void parseProblemUrl(String url, String category, Dialog dialog) {
        // Regex to extract contest ID and problem index from Codeforces URL
        // Examples:
        // https://codeforces.com/contest/1234/problem/A
        // https://codeforces.com/problemset/problem/1234/A
        Pattern pattern = Pattern.compile("codeforces\\.com/(?:contest|problemset/problem)/(\\d+)/problem/([A-Z]\\d*)");
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find()) {
            Toast.makeText(this, "Invalid Codeforces URL format", Toast.LENGTH_SHORT).show();
            return;
        }

        String contestId = matcher.group(1);
        String problemIndex = matcher.group(2);
        String problemCode = contestId + problemIndex;

        // Show loading
        Toast.makeText(this, "Fetching problem details...", Toast.LENGTH_SHORT).show();

        // Fetch problem details from API
        fetchProblemDetails(Integer.parseInt(contestId), problemIndex, problemCode, url, category, dialog);
    }

    private void fetchProblemDetails(int contestId, String problemIndex, String problemCode,
            String url, String category, Dialog dialog) {
        // Fetch contest standings to get problem details
        codeforcesAPI.getContestStandings(contestId, 1, 1)
                .enqueue(new Callback<CodeforcesResponse<com.cplps.android.api.models.CFContestStandings>>() {
                    @Override
                    public void onResponse(
                            Call<CodeforcesResponse<com.cplps.android.api.models.CFContestStandings>> call,
                            Response<CodeforcesResponse<com.cplps.android.api.models.CFContestStandings>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getStatus().equals("OK")) {
                            // Find the specific problem
                            List<CFProblem> problems = response.body().getResult().getProblems();
                            CFProblem targetProblem = null;

                            for (CFProblem problem : problems) {
                                if (problem.getIndex().equals(problemIndex)) {
                                    targetProblem = problem;
                                    break;
                                }
                            }

                            if (targetProblem != null) {
                                addBookmarkToDatabase(problemCode, url, targetProblem.getName(),
                                        targetProblem.getRating(), category);
                            } else {
                                // Problem not found, add with default values
                                addBookmarkToDatabase(problemCode, url, "Problem " + problemCode, 0, category);
                            }
                        } else {
                            // API failed, add with default values
                            addBookmarkToDatabase(problemCode, url, "Problem " + problemCode, 0, category);
                        }
                        dialog.dismiss();
                        refreshCurrentFragment();
                    }

                    @Override
                    public void onFailure(
                            Call<CodeforcesResponse<com.cplps.android.api.models.CFContestStandings>> call,
                            Throwable t) {
                        Log.e(TAG, "Failed to fetch problem details", t);
                        // Still add the bookmark with default values
                        addBookmarkToDatabase(problemCode, url, "Problem " + problemCode, 0, category);
                        dialog.dismiss();
                        refreshCurrentFragment();
                    }
                });
    }

    private void addBookmarkToDatabase(String problemCode, String url, String problemName,
            int rating, String category) {
        String username = sessionManager.getUsername();
        int userId = dbHelper.getUserIdByUsername(username);

        long result = dbHelper.addBookmark(userId, url, problemCode, problemName, rating, category);

        if (result != -1) {
            Toast.makeText(this, "Bookmark added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Bookmark already exists or failed to add", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshCurrentFragment() {
        int currentItem = viewPager.getCurrentItem();
        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        if (adapter != null) {
            BookmarkListFragment fragment = (BookmarkListFragment) adapter.getItem(currentItem);
            if (fragment != null) {
                fragment.loadBookmarks();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // ViewPager Adapter
    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}
