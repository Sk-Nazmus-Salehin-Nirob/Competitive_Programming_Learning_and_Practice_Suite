package com.cplps.android;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewUsername, textViewCFHandle, textViewCFRating, textViewCFSolved, textViewTotalSolved;
    private TextInputEditText editTextCFHandle;
    private MaterialButton buttonAddCFHandle, buttonSyncCF, buttonViewSolved;
    private LinearLayout layoutAddHandle, layoutHandleInfo;
    private LineChart chartRating;
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
        layoutAddHandle = findViewById(R.id.layoutAddHandle);
        layoutHandleInfo = findViewById(R.id.layoutHandleInfo);
        chartRating = findViewById(R.id.chartRating);
    }

    private void loadUserData() {
        String username = sessionManager.getUsername();
        if (username != null) {
            textViewUsername.setText(username);
        }
    }

    private void checkCodeForcesHandle() {
        Cursor cursor = databaseHelper.getPlatform(currentUserId, "Codeforces");

        if (cursor != null && cursor.moveToFirst()) {
            platformId = cursor.getInt(cursor.getColumnIndexOrThrow("platform_id"));
            currentHandle = cursor.getString(cursor.getColumnIndexOrThrow("handle"));
            int rating = cursor.getInt(cursor.getColumnIndexOrThrow("rating"));
            int maxRating = cursor.getInt(cursor.getColumnIndexOrThrow("max_rating"));

            textViewCFHandle.setText("Handle: " + currentHandle);
            textViewCFRating.setText("Rating: " + rating + " (Max: " + maxRating + ")");

            int solvedCount = databaseHelper.getSolvedProblemsCount(platformId);
            textViewCFSolved.setText("Solved: " + solvedCount + " problems");
            textViewTotalSolved.setText("Total Problems Solved: " + solvedCount);

            layoutAddHandle.setVisibility(View.GONE);
            layoutHandleInfo.setVisibility(View.VISIBLE);

            fetchRatingHistory(currentHandle);
            cursor.close();
        } else {
            layoutAddHandle.setVisibility(View.VISIBLE);
            layoutHandleInfo.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        buttonAddCFHandle.setOnClickListener(v -> addCodeforcesHandle());
        buttonSyncCF.setOnClickListener(v -> syncData());
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
            textViewCFRating.setText("Rating: " + user.getRating() + " (Max: " + user.getMaxRating() + ")");

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
                            submission.getCreationTimeSeconds());
                    count++;
                }
            }
        }

        textViewCFSolved.setText("Solved: " + count + " problems");
        textViewTotalSolved.setText("Total Problems Solved: " + count);
        Toast.makeText(this, "Synced " + count + " problems!", Toast.LENGTH_LONG).show();
    }

    private void displayRatingGraph(List<CFRatingChange> ratingChanges) {
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < ratingChanges.size(); i++) {
            entries.add(new Entry(i, ratingChanges.get(i).getNewRating()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Rating");
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
        chartRating.animateX(1000);
        chartRating.setVisibility(View.VISIBLE);
        chartRating.invalidate();
    }
}
