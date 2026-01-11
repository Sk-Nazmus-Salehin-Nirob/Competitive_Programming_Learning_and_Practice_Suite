package com.cplps.android;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.models.Note;
import com.cplps.android.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity implements NotesAdapter.OnNoteLongClickListener {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Init
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        userId = dbHelper.getUserIdByUsername(sessionManager.getUsername());

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notes");
        }

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        fabAdd = findViewById(R.id.fab_add_note);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true); // Chat style, start from bottom
        recyclerView.setLayoutManager(lm);

        adapter = new NotesAdapter(this, this);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddNoteDialog());

        loadNotes();
    }

    private void loadNotes() {
        List<Note> noteList = new ArrayList<>();
        Cursor cursor = dbHelper.getNotes(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("note_id"));
                int uId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));

                noteList.add(new Note(id, uId, content, date));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.setNotes(noteList);

        if (noteList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            // Scroll to bottom
            recyclerView.scrollToPosition(noteList.size() - 1);
        }
    }

    private void showAddNoteDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_category); // Reusing simple input dialog layout?
        // Actually, let's create a better one multiline or reuse and tweak.
        // Reusing might be confusing with titles. Let's create a simple multiline input
        // layout programmatically or reuse.
        // Let's create a new layout `dialog_add_note.xml` quickly.
        dialog.dismiss(); // Cancel this

        showNoteInput();
    }

    // Using a custom dialog layout for Note Input
    private void showNoteInput() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_note);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        EditText etContent = dialog.findViewById(R.id.et_note_content);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (content.isEmpty())
                return;

            dbHelper.addNote(userId, content);
            loadNotes();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onNoteLongClick(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteNote(note.getNoteId());
                    loadNotes();
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
