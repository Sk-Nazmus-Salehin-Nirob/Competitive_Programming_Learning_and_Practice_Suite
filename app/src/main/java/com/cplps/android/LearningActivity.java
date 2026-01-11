package com.cplps.android;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.models.LearningTopic;
import com.cplps.android.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class LearningActivity extends AppCompatActivity implements LearningTopicAdapter.OnTopicClickListener {

    private RecyclerView recyclerView;
    private LearningTopicAdapter adapter;
    private TextView tvEmpty;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        // Init
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        userId = dbHelper.getUserIdByUsername(sessionManager.getUsername());

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Learning");
        }

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_topic);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LearningTopicAdapter(this, this);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddTopicDialog());

        loadTopics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTopics();
    }

    private void loadTopics() {
        List<LearningTopic> topics = new ArrayList<>();
        Cursor cursor = dbHelper.getLearningTopics(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("topic_id"));
                int uId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));

                topics.add(new LearningTopic(id, uId, title, date));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.setTopics(topics);
        tvEmpty.setVisibility(topics.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddTopicDialog() {
        // Reuse generic input dialog or build simple one
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Topic");

        final EditText input = new EditText(this);
        input.setHint("Topic Name");
        input.setPadding(32, 32, 32, 32);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                dbHelper.addLearningTopic(userId, title);
                loadTopics();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onTopicClick(LearningTopic topic) {
        Intent intent = new Intent(this, LearningTopicActivity.class);
        intent.putExtra("TOPIC_ID", topic.getId());
        intent.putExtra("TOPIC_TITLE", topic.getTitle());
        startActivity(intent);
    }

    @Override
    public void onTopicLongClick(LearningTopic topic) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Topic")
                .setMessage("Delete '" + topic.getTitle() + "' and all contents?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteLearningTopic(topic.getId());
                    loadTopics();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
