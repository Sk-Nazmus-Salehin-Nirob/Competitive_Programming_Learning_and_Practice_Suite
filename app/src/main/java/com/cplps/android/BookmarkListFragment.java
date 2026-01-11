package com.cplps.android;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.models.BookmarkedProblem;
import com.cplps.android.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class BookmarkListFragment extends Fragment implements BookmarkAdapter.OnBookmarkActionListener {

    private static final String ARG_CATEGORY = "category";

    private String category;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private BookmarkAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    public static BookmarkListFragment newInstance(String category) {
        BookmarkListFragment fragment = new BookmarkListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmpty = view.findViewById(R.id.tv_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookmarkAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);

        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(() -> {
            loadBookmarks();
            swipeRefresh.setRefreshing(false);
        });

        loadBookmarks();
    }

    public void loadBookmarks() {
        List<BookmarkedProblem> bookmarkList = new ArrayList<>();
        String username = sessionManager.getUsername();
        int userId = dbHelper.getUserIdByUsername(username);

        Cursor cursor = dbHelper.getBookmarks(userId, category);

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
                bookmarkList.add(bookmark);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (bookmarkList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.setBookmarks(bookmarkList);

        // Check for solved problems and remove them if category is "to_solve"
        if (category.equals("to_solve")) {
            checkAndRemoveSolvedProblems(bookmarkList, userId);
        }
    }

    private void checkAndRemoveSolvedProblems(List<BookmarkedProblem> bookmarks, int userId) {
        new Thread(() -> {
            for (BookmarkedProblem bookmark : bookmarks) {
                if (dbHelper.isProblemSolved(userId, bookmark.getProblemCode())) {
                    // Remove from database
                    dbHelper.deleteBookmark(bookmark.getBookmarkId());

                    // Update UI on main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapter.removeBookmark(bookmark);
                            Toast.makeText(requireContext(),
                                    "Problem " + bookmark.getProblemCode() + " was solved and removed!",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDeleteBookmark(BookmarkedProblem bookmark) {
        boolean deleted = dbHelper.deleteBookmark(bookmark.getBookmarkId());
        if (deleted) {
            adapter.removeBookmark(bookmark);
            Toast.makeText(requireContext(), "Bookmark deleted", Toast.LENGTH_SHORT).show();

            // Update empty state
            if (adapter.getItemCount() == 0) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(requireContext(), "Failed to delete bookmark", Toast.LENGTH_SHORT).show();
        }
    }
}
