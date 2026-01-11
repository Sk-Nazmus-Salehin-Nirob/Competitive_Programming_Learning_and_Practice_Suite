package com.cplps.android;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.models.BookmarkCategory;
import com.cplps.android.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private TextView tvEmpty;
    private CategoryAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        // Initialize
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        userId = dbHelper.getUserIdByUsername(sessionManager.getUsername());

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bookmarks");
        }

        // Ensure default categories exist
        dbHelper.ensureDefaultCategories(userId);

        // ViewPager and TabLayout are gone in the XML, but we are reusing
        // activity_bookmarks.xml
        // We need to update the activity_bookmarks.xml layout to match a simple list
        // structure first.
        // Or we can dynamically find views if the layout is updated.
        // Let's assume we update the XML next.

        recyclerView = findViewById(R.id.recycler_view); // Make sure this ID exists in layout
        fabAdd = findViewById(R.id.fab_add);
        tvEmpty = findViewById(R.id.tv_empty); // Need to add this to layout

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(this, this);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddCategoryDialog());

        loadCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        List<BookmarkCategory> categories = new ArrayList<>();
        Cursor cursor = dbHelper.getAllCategories(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("category_name"));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));

                BookmarkCategory cat = new BookmarkCategory(id, userId, name, date);

                // Get count
                int count = dbHelper.getBookmarkCount(userId, name);
                cat.setProblemCount(count);

                categories.add(cat);
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.setCategories(categories);

        if (categories.isEmpty()) {
            if (tvEmpty != null)
                tvEmpty.setVisibility(View.VISIBLE);
        } else {
            if (tvEmpty != null)
                tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showAddCategoryDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_category);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextInputEditText etName = dialog.findViewById(R.id.et_category_name);
        Button btnCreate = dialog.findViewById(R.id.btn_create);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Name required");
                return;
            }

            long res = dbHelper.addCategory(userId, name);
            if (res != -1) {
                loadCategories();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onCategoryClick(BookmarkCategory category) {
        Intent intent = new Intent(this, BookmarkProblemsActivity.class);
        intent.putExtra(BookmarkProblemsActivity.EXTRA_CATEGORY_NAME, category.getCategoryName());
        startActivity(intent);
    }

    @Override
    public void onCategoryLongClick(BookmarkCategory category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Delete '" + category.getCategoryName() + "' and all its bookmarks?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteCategory(userId, category.getCategoryName());
                    loadCategories();
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
