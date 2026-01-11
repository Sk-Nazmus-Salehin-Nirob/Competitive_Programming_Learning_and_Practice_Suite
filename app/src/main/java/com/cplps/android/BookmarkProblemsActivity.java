package com.cplps.android;

import android.app.Dialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.api.ApiClient;
import com.cplps.android.api.CodeforcesAPI;
import com.cplps.android.api.models.CFProblem;
import com.cplps.android.api.models.CodeforcesResponse;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.models.BookmarkedProblem;
import com.cplps.android.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookmarkProblemsActivity extends AppCompatActivity implements BookmarkAdapter.OnBookmarkActionListener {

    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    private static final String TAG = "BookmarkProblems";

    private String categoryName;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private BookmarkAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private CodeforcesAPI codeforcesAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark_problems);

        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (categoryName == null)
            finish();

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        codeforcesAPI = ApiClient.getCodeforcesAPI();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName);
        }

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        fabAdd = findViewById(R.id.fab_add_problem);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookmarkAdapter(this, this);
        recyclerView.setAdapter(adapter);

        // Check and remove solved problems if in "Problems to solve" category
        if ("Problems to solve".equals(categoryName)) {
            checkAndRemoveSolvedProblems();
        }

        fabAdd.setOnClickListener(v -> showAddProblemDialog());

        loadProblems();
    }

    private void checkAndRemoveSolvedProblems() {
        new Thread(() -> {
            try {
                String username = sessionManager.getUsername();
                int userId = dbHelper.getUserIdByUsername(username);

                // Get all problems in this category
                Cursor cursor = dbHelper.getBookmarks(userId, categoryName);
                List<BookmarkedProblem> problems = new ArrayList<>();
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        BookmarkedProblem p = new BookmarkedProblem();
                        p.setBookmarkId(cursor.getInt(cursor.getColumnIndexOrThrow("bookmark_id")));
                        p.setProblemCode(cursor.getString(cursor.getColumnIndexOrThrow("problem_code")));
                        problems.add(p);
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                boolean removed = false;
                for (BookmarkedProblem p : problems) {
                    if (dbHelper.isProblemSolved(userId, p.getProblemCode())) {
                        dbHelper.deleteBookmark(p.getBookmarkId());
                        removed = true;
                    }
                }

                if (removed) {
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, "Solved problems detected and removed!", Toast.LENGTH_SHORT).show();
                            loadProblems();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking solved problems", e);
            }
        }).start();
    }

    private void loadProblems() {
        String username = sessionManager.getUsername();
        int userId = dbHelper.getUserIdByUsername(username);

        Cursor cursor = dbHelper.getBookmarks(userId, categoryName);
        List<BookmarkedProblem> list = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int bookmarkId = cursor.getInt(cursor.getColumnIndexOrThrow("bookmark_id"));
                int uId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String problemUrl = cursor.getString(cursor.getColumnIndexOrThrow("problem_url"));
                String problemCode = cursor.getString(cursor.getColumnIndexOrThrow("problem_code"));
                String problemName = cursor.getString(cursor.getColumnIndexOrThrow("problem_name"));
                int problemRating = cursor.getInt(cursor.getColumnIndexOrThrow("problem_rating"));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                long addedAt = cursor.getLong(cursor.getColumnIndexOrThrow("added_at"));

                BookmarkedProblem bookmark = new BookmarkedProblem(bookmarkId, uId, problemUrl,
                        problemCode, problemName, problemRating, cat, addedAt);
                list.add(bookmark);
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.setBookmarks(list);

        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddProblemDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_bookmark);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextInputEditText etUrl = dialog.findViewById(R.id.et_problem_url);

        // Hide category selection as we are adding to current category
        // In a more advanced version, we could show checkboxes to add to multiple
        dialog.findViewById(R.id.radio_group_category).setVisibility(View.GONE);
        ((TextView) dialog.findViewById(R.id.tv_category_label)).setText("Adding to: " + categoryName);

        Button btnAdd = dialog.findViewById(R.id.btn_add);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty())
                return;

            parseAndAddProblem(url, dialog);
        });

        dialog.show();
    }

    private void parseAndAddProblem(String url, Dialog dialog) {
        Pattern pattern = Pattern.compile("codeforces\\.com/(?:contest|problemset/problem)/(\\d+)/problem/([A-Z]\\d*)");
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find()) {
            Toast.makeText(this, "Invalid Codeforces URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String contestId = matcher.group(1);
        String problemIndex = matcher.group(2);
        String problemCode = contestId + problemIndex;

        Toast.makeText(this, "Fetching details...", Toast.LENGTH_SHORT).show();

        codeforcesAPI.getContestStandings(Integer.parseInt(contestId), 1, 1)
                .enqueue(new Callback<CodeforcesResponse<com.cplps.android.api.models.CFContestStandings>>() {
                    @Override
                    public void onResponse(
                            Call<CodeforcesResponse<com.cplps.android.api.models.CFContestStandings>> call,
                            Response<CodeforcesResponse<com.cplps.android.api.models.CFContestStandings>> response) {
                        String name = "Problem " + problemCode;
                        int rating = 0;

                        if (response.isSuccessful() && response.body() != null) {
                            List<CFProblem> problems = response.body().getResult().getProblems();
                            for (CFProblem p : problems) {
                                if (p.getIndex().equals(problemIndex)) {
                                    name = p.getName();
                                    rating = p.getRating();
                                    break;
                                }
                            }
                        }

                        addProblemToDb(url, problemCode, name, rating);
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(
                            Call<CodeforcesResponse<com.cplps.android.api.models.CFContestStandings>> call,
                            Throwable t) {
                        addProblemToDb(url, problemCode, "Problem " + problemCode, 0);
                        dialog.dismiss();
                    }
                });
    }

    private void addProblemToDb(String url, String code, String name, int rating) {
        int userId = dbHelper.getUserIdByUsername(sessionManager.getUsername());
        long res = dbHelper.addBookmark(userId, url, code, name, rating, categoryName);
        if (res != -1) {
            Toast.makeText(this, "Added to " + categoryName, Toast.LENGTH_SHORT).show();
            loadProblems();
        } else {
            Toast.makeText(this, "Already exists in this category", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteBookmark(BookmarkedProblem bookmark) {
        if (dbHelper.deleteBookmark(bookmark.getBookmarkId())) {
            adapter.removeBookmark(bookmark);
            if (adapter.getItemCount() == 0) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
