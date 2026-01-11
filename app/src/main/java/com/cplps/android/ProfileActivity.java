package com.cplps.android;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.Spanned;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.cplps.android.api.ApiClient;
import com.cplps.android.api.models.*;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.utils.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.XAxis;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;
import java.text.SimpleDateFormat;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewUsername, textViewCFHandle, textViewCFRating, textViewCFSolved, textViewTotalSolved;
    private TextInputEditText editTextCFHandle;
    private MaterialButton buttonAddCFHandle, buttonSyncCF, buttonChangeHandle;
    private LinearLayout layoutAddHandle, layoutHandleInfo;
    private LineChart chartRating;
    private GridLayout gridHeatmap;
    private View cardHeatmap;
    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private int currentUserId = 1;
    private int platformId = -1;
    private String currentHandle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadUserData();
        checkCodeForcesHandle();
        setupListeners();
    }

    private void initViews() {
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewCFHandle = findViewById(R.id.textViewCFHandle);
        textViewCFRating = findViewById(R.id.textViewCFRating);
        textViewCFSolved = findViewById(R.id.textViewCFSolved);
        textViewTotalSolved = findViewById(R.id.textViewTotalSolved);
        editTextCFHandle = findViewById(R.id.editTextCFHandle);
        buttonAddCFHandle = findViewById(R.id.buttonAddCFHandle);
        buttonSyncCF = findViewById(R.id.buttonSyncCF);
        buttonChangeHandle = findViewById(R.id.buttonChangeHandle);
        layoutAddHandle = findViewById(R.id.layoutAddHandle);
        layoutHandleInfo = findViewById(R.id.layoutHandleInfo);
        chartRating = findViewById(R.id.chartRating);
        gridHeatmap = findViewById(R.id.gridHeatmap);
        cardHeatmap = findViewById(R.id.cardHeatmap);
    }

    private void loadUserData() {
        String username = sessionManager.getUsername();
        if (username != null) {
            textViewUsername.setText(username);
            currentUserId = databaseHelper.getUserIdByUsername(username);
        }
    }

    private void checkCodeForcesHandle() {
        Cursor cursor = databaseHelper.getPlatform(currentUserId, "Codeforces");

        if (cursor != null && cursor.moveToFirst()) {
            platformId = cursor.getInt(cursor.getColumnIndexOrThrow("platform_id"));
            currentHandle = cursor.getString(cursor.getColumnIndexOrThrow("handle"));
            int rating = cursor.getInt(cursor.getColumnIndexOrThrow("rating"));
            int maxRating = cursor.getInt(cursor.getColumnIndexOrThrow("max_rating"));

            String currentRank = getCodeforcesRank(rating);
            String maxRank = getCodeforcesRank(maxRating);

            textViewCFHandle.setText(currentRank + "\n" + currentHandle);
            textViewCFHandle.setTextColor(getCodeforcesColor(rating));

            String ratingText = "Contest rating: " + rating + " (max. " + maxRank + ", " + maxRating + ")";
            SpannableString spannableRating = new SpannableString(ratingText);

            int ratingStart = ratingText.indexOf(String.valueOf(rating));
            spannableRating.setSpan(
                    new ForegroundColorSpan(getCodeforcesColor(rating)),
                    ratingStart, ratingStart + String.valueOf(rating).length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int maxRankStart = ratingText.indexOf(maxRank);
            spannableRating.setSpan(
                    new ForegroundColorSpan(getCodeforcesColor(maxRating)),
                    maxRankStart, maxRankStart + maxRank.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int maxRatingStart = ratingText.lastIndexOf(String.valueOf(maxRating));
            spannableRating.setSpan(
                    new ForegroundColorSpan(getCodeforcesColor(maxRating)),
                    maxRatingStart, maxRatingStart + String.valueOf(maxRating).length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            textViewCFRating.setText(spannableRating);

            int solvedCount = databaseHelper.getSolvedProblemsCount(platformId);
            textViewCFSolved.setText("Solved: " + solvedCount + " problems");
            textViewTotalSolved.setText("Total Problems Solved: " + solvedCount);

            layoutAddHandle.setVisibility(View.GONE);
            layoutHandleInfo.setVisibility(View.VISIBLE);

            fetchRatingHistory(currentHandle);
            displayHeatmap();
            cursor.close();
        } else {
            layoutAddHandle.setVisibility(View.VISIBLE);
            layoutHandleInfo.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        buttonAddCFHandle.setOnClickListener(v -> addCodeforcesHandle());
        buttonSyncCF.setOnClickListener(v -> syncData());
        buttonChangeHandle.setOnClickListener(v -> showChangeHandleForm());
    }

    private void showChangeHandleForm() {
        layoutHandleInfo.setVisibility(View.GONE);
        layoutAddHandle.setVisibility(View.VISIBLE);
        editTextCFHandle.setText(currentHandle);
        editTextCFHandle.requestFocus();
    }

    private void addCodeforcesHandle() {
        String handle = editTextCFHandle.getText().toString().trim();

        if (handle.isEmpty()) {
            editTextCFHandle.setError("Please enter a handle");
            return;
        }

        buttonAddCFHandle.setEnabled(false);
        buttonAddCFHandle.setText("Verifying...");

        ApiClient.getCodeforcesAPI().getUserInfo(handle).enqueue(new Callback<CodeforcesResponse<List<CFUser>>>() {
            @Override
            public void onResponse(Call<CodeforcesResponse<List<CFUser>>> call,
                    Response<CodeforcesResponse<List<CFUser>>> response) {
                buttonAddCFHandle.setEnabled(true);
                buttonAddCFHandle.setText("Add Handle & Sync Data");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CFUser> users = response.body().getResult();
                    if (users != null && !users.isEmpty()) {
                        CFUser user = users.get(0);
                        saveHandleAndFetchData(handle, user);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Handle not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to verify handle", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CodeforcesResponse<List<CFUser>>> call, Throwable t) {
                buttonAddCFHandle.setEnabled(true);
                buttonAddCFHandle.setText("Add Handle & Sync Data");
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveHandleAndFetchData(String handle, CFUser user) {
        long result = databaseHelper.addPlatform(currentUserId, "Codeforces", handle, user.getRating(),
                user.getMaxRating());

        if (result != -1) {
            platformId = (int) result;
            currentHandle = handle;

            textViewCFHandle.setText("Handle: " + handle);
            textViewCFRating.setText("Contest rating: " + user.getRating() + " (max. " + user.getMaxRating() + ")");

            layoutAddHandle.setVisibility(View.GONE);
            layoutHandleInfo.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Handle saved!", Toast.LENGTH_SHORT).show();

            fetchRatingHistory(handle);
            fetchSolvedProblems(handle);
        }
    }

    private void syncData() {
        if (!currentHandle.isEmpty()) {
            fetchRatingHistory(currentHandle);
            fetchSolvedProblems(currentHandle);
            displayHeatmap();
        }
    }

    private void fetchRatingHistory(String handle) {
        ApiClient.getCodeforcesAPI().getUserRating(handle)
                .enqueue(new Callback<CodeforcesResponse<List<CFRatingChange>>>() {
                    @Override
                    public void onResponse(Call<CodeforcesResponse<List<CFRatingChange>>> call,
                            Response<CodeforcesResponse<List<CFRatingChange>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<CFRatingChange> ratingChanges = response.body().getResult();
                            if (ratingChanges != null && !ratingChanges.isEmpty()) {
                                displayRatingGraph(ratingChanges);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CodeforcesResponse<List<CFRatingChange>>> call, Throwable t) {
                    }
                });
    }

    private void fetchSolvedProblems(String handle) {
        Toast.makeText(this, "Fetching solved problems...", Toast.LENGTH_SHORT).show();

        ApiClient.getCodeforcesAPI().getUserSubmissions(handle, 1, 100000)
                .enqueue(new Callback<CodeforcesResponse<List<CFSubmission>>>() {
                    @Override
                    public void onResponse(Call<CodeforcesResponse<List<CFSubmission>>> call,
                            Response<CodeforcesResponse<List<CFSubmission>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<CFSubmission> submissions = response.body().getResult();
                            if (submissions != null) {
                                processSolvedProblems(submissions);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CodeforcesResponse<List<CFSubmission>>> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Failed to fetch problems", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processSolvedProblems(List<CFSubmission> submissions) {
        databaseHelper.clearSolvedProblems(platformId);

        Set<String> solvedProblems = new HashSet<>();
        int count = 0;

        for (CFSubmission submission : submissions) {
            if (submission.isAccepted()) {
                String problemCode = submission.getProblemCode();

                if (!solvedProblems.contains(problemCode)) {
                    solvedProblems.add(problemCode);

                    databaseHelper.addSolvedProblem(platformId, problemCode,
                            submission.getProblemName(),
                            submission.getProblemRating(),
                            submission.getCreationTimeSeconds(),
                            ""); // No contest name
                    count++;
                }
            }
        }

        textViewCFSolved.setText("Solved: " + count + " problems");
        textViewTotalSolved.setText("Total Problems Solved: " + count);
        Toast.makeText(this, "Synced " + count + " problems!", Toast.LENGTH_LONG).show();

        displayHeatmap();
    }

    private void displayRatingGraph(List<CFRatingChange> ratingChanges) {
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < ratingChanges.size(); i++) {
            entries.add(new Entry(i, ratingChanges.get(i).getNewRating()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Contest Rating");
        dataSet.setColor(getColor(R.color.primary));
        dataSet.setValueTextColor(getColor(R.color.text_primary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(getColor(R.color.accent));
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);

        chartRating.setData(lineData);
        chartRating.getDescription().setText("Contest Rating Progress");
        chartRating.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chartRating.setVisibility(View.VISIBLE);
        chartRating.invalidate();
    }

    private void displayHeatmap() {
        Cursor cursor = databaseHelper.getSolvedProblems(platformId);

        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null)
                cursor.close();
            return;
        }

        Map<String, Integer> activityMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        while (cursor.moveToNext()) {
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("solved_at"));
            String date = dateFormat.format(new Date(timestamp * 1000));
            activityMap.put(date, activityMap.getOrDefault(date, 0) + 1);
        }
        cursor.close();

        gridHeatmap.removeAllViews();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -364);

        int maxCount = activityMap.values().isEmpty() ? 1 : Collections.max(activityMap.values());

        for (int week = 0; week < 53; week++) {
            for (int day = 0; day < 7; day++) {
                String date = dateFormat.format(cal.getTime());
                int count = activityMap.getOrDefault(date, 0);

                View tile = new View(this);
                int size = (int) (12 * getResources().getDisplayMetrics().density);
                int margin = (int) (2 * getResources().getDisplayMetrics().density);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = size;
                params.height = size;
                params.setMargins(margin, margin, margin, margin);
                params.rowSpec = GridLayout.spec(day);
                params.columnSpec = GridLayout.spec(week);
                tile.setLayoutParams(params);

                int color = getHeatmapColor(count, maxCount);
                tile.setBackgroundColor(color);

                gridHeatmap.addView(tile);
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        cardHeatmap.setVisibility(View.VISIBLE);
    }

    private int getHeatmapColor(int count, int maxCount) {
        if (count == 0)
            return Color.parseColor("#EBEDF0");

        float intensity = (float) count / maxCount;
        if (intensity <= 0.25f)
            return Color.parseColor("#C6E48B");
        else if (intensity <= 0.50f)
            return Color.parseColor("#7BC96F");
        else if (intensity <= 0.75f)
            return Color.parseColor("#239A3B");
        else
            return Color.parseColor("#196127");
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

    private String getCodeforcesRank(int rating) {
        if (rating < 1200)
            return "Newbie";
        else if (rating < 1400)
            return "Pupil";
        else if (rating < 1600)
            return "Specialist";
        else if (rating < 1900)
            return "Expert";
        else if (rating < 2100)
            return "Candidate Master";
        else if (rating < 2300)
            return "Master";
        else if (rating < 2400)
            return "International Master";
        else if (rating < 2600)
            return "Grandmaster";
        else if (rating < 3000)
            return "International Grandmaster";
        else
            return "Legendary Grandmaster";
    }
}
