package com.cplps.android;

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
    private MaterialButton buttonAddCFHandle, buttonSyncCF;
    private LinearLayout layoutAddHandle, layoutHandleInfo;
    private LineChart chartRating;
    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize
        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initViews();

        // Load user data
        loadUserData();

        // Check if Codeforces handle exists
        checkCodeForcesHandle();

        // Setup click listeners
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
        // TODO: Check database for existing Codeforces handle
        // For now, showing add handle form
        layoutAddHandle.setVisibility(View.VISIBLE);
        layoutHandleInfo.setVisibility(View.GONE);
    }

    private void setupListeners() {
        buttonAddCFHandle.setOnClickListener(v -> addCodeforcesHandle());
        buttonSyncCF.setOnClickListener(v -> syncCodeforcesData());
    }

    private void addCodeforcesHandle() {
        String handle = editTextCFHandle.getText().toString().trim();

        if (handle.isEmpty()) {
            editTextCFHandle.setError("Please enter a handle");
            return;
        }

        // Show loading
        buttonAddCFHandle.setEnabled(false);
        buttonAddCFHandle.setText("Verifying...");

        // Verify handle exists and fetch data
        verifyAndAddHandle(handle);
    }

    private void verifyAndAddHandle(String handle) {
        // Call Codeforces API to verify user exists
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
                        // Save to database and fetch more data
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
        // TODO: Save to database

        // Update UI
        textViewCFHandle.setText("Handle: " + handle);
        textViewCFRating.setText("Rating: " + user.getRating() + " (Max: " + user.getMaxRating() + ")");

        layoutAddHandle.setVisibility(View.GONE);
        layoutHandleInfo.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Handle added successfully!", Toast.LENGTH_SHORT).show();

        // Fetch rating history
        fetchRatingHistory(handle);
    }

    private void syncCodeforcesData() {
        String handle = textViewCFHandle.getText().toString().replace("Handle: ", "");
        fetchRatingHistory(handle);
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
                                Toast.makeText(ProfileActivity.this, "Rating data synced!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to fetch rating history", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CodeforcesResponse<List<CFRatingChange>>> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Failed to sync: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void displayRatingGraph(List<CFRatingChange> ratingChanges) {
        // Prepare data for chart
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < ratingChanges.size(); i++) {
            CFRatingChange change = ratingChanges.get(i);
            entries.add(new Entry(i, change.getNewRating()));
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
