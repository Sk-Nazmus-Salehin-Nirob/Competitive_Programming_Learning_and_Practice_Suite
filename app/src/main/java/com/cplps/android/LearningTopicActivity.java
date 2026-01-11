package com.cplps.android;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.models.LearningResource;
import com.cplps.android.models.LearningTopic;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class LearningTopicActivity extends AppCompatActivity
        implements LearningResourceAdapter.OnResourceClickListener {

    private static final String TAG = "LearningTopicActivity";
    private int topicId;
    private String topicTitle;
    private RecyclerView recyclerView;
    private LearningResourceAdapter adapter;
    private TextView tvEmpty;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAddText, fabAddFile;

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    importFile(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_detail);

        topicId = getIntent().getIntExtra("TOPIC_ID", -1);
        topicTitle = getIntent().getStringExtra("TOPIC_TITLE");

        if (topicId == -1) {
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(topicTitle);
        }

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        fabAddText = findViewById(R.id.fab_add_text);
        fabAddFile = findViewById(R.id.fab_add_file);

        // Simple logic to show/hide file fab? Or just use one fab to show
        // dialog/options?
        // User said: "+ sign ... write note OR add pdf".
        // Let's make main FAB show a dialog or bottom sheet to choose type.
        // For now, I have two fabs in the layout. Let's make the text FAB the "Main"
        // one that expands?
        // Actually, let's keep it simple: Click FAB -> Dialog: [Add Note] [Add File]

        // Hide secondary FAB logic for now and just use main FAB to prompt options?
        // Or implement standard FAB expansion.
        // Let's just make the "+" button show a generic "Add Item" dialog.

        fabAddFile.setVisibility(View.GONE); // Hide initially or replace logic

        fabAddText.setOnClickListener(v -> showAddOptionsDialog());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LearningResourceAdapter(this, this);
        recyclerView.setAdapter(adapter);

        loadResources();
    }

    private void showAddOptionsDialog() {
        String[] options = { "Write Note", "Attach File (PDF/DOC)" };
        new AlertDialog.Builder(this)
                .setTitle("Add to Topic")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showNoteDialog();
                    } else {
                        pickFile();
                    }
                })
                .show();
    }

    private void showNoteDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_note);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        EditText etContent = dialog.findViewById(R.id.et_note_content);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        // Correcting button text for Context
        btnSave.setText("Save Note");

        btnSave.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (!content.isEmpty()) {
                dbHelper.addLearningResource(topicId, LearningResource.TYPE_TEXT, content, null);
                loadResources();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void pickFile() {
        // MIME types: PDF and Word docs
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        filePickerLauncher.launch(mimeTypes);
    }

    private void importFile(Uri sourceUri) {
        try {
            String fileName = getFileName(sourceUri);
            File destFile = new File(getExternalFilesDir(null),
                    "learning_" + System.currentTimeMillis() + "_" + fileName);

            try (InputStream is = getContentResolver().openInputStream(sourceUri);
                    OutputStream os = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }

            // Save DB entry
            // Store absolute path as current storage
            dbHelper.addLearningResource(topicId, LearningResource.TYPE_FILE, destFile.getAbsolutePath(), fileName);
            loadResources();
            Toast.makeText(this, "File attached: " + fileName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Import file failed", e);
            Toast.makeText(this, "Failed to attach file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0)
                        result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut + 1);
        }
        return result;
    }

    private void loadResources() {
        List<LearningResource> list = new ArrayList<>();
        Cursor cursor = dbHelper.getLearningResources(topicId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("resource_id"));
                int tId = cursor.getInt(cursor.getColumnIndexOrThrow("topic_id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));

                list.add(new LearningResource(id, tId, type, content, name, date));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.setResources(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResourceClick(LearningResource resource) {
        if (LearningResource.TYPE_FILE.equals(resource.getType())) {
            openFile(resource);
        } else {
            // View note details if needed, or edit
            new AlertDialog.Builder(this)
                    .setTitle("Note")
                    .setMessage(resource.getContent())
                    .setPositiveButton("Close", null)
                    .show();
        }
    }

    private void openFile(LearningResource resource) {
        File file = new File(resource.getContent());
        if (!file.exists()) {
            Toast.makeText(this, "File not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, getMimeType(file.getAbsolutePath()));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        if (type == null)
            type = "*/*";
        return type;
    }

    @Override
    public void onResourceLongClick(LearningResource resource) {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteLearningResource(resource.getId());
                    loadResources();
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
