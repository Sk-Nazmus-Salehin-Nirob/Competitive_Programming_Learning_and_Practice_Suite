package com.cplps.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CPLPS.db";
    private static final int DATABASE_VERSION = 7; // Upgraded for Learning features

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Platforms table
    private static final String TABLE_PLATFORMS = "platforms";
    private static final String COLUMN_PLATFORM_ID = "platform_id";
    private static final String COLUMN_PLATFORM_NAME = "platform_name";
    private static final String COLUMN_HANDLE = "handle";
    private static final String COLUMN_LAST_SYNCED = "last_synced";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_MAX_RATING = "max_rating";

    // Solved problems table
    private static final String TABLE_SOLVED_PROBLEMS = "solved_problems";
    private static final String COLUMN_PROBLEM_ID = "problem_id";
    private static final String COLUMN_PROBLEM_CODE = "problem_code";
    private static final String COLUMN_PROBLEM_NAME = "problem_name";
    private static final String COLUMN_PROBLEM_RATING = "problem_rating";
    private static final String COLUMN_SOLVED_AT = "solved_at";
    private static final String COLUMN_PROBLEM_CONTEST = "contest_name";

    // Contests table
    private static final String TABLE_CONTESTS = "contests";
    private static final String COLUMN_CONTEST_ID = "contest_id";
    private static final String COLUMN_CONTEST_NAME = "contest_name";
    private static final String COLUMN_CONTEST_DATE = "contest_date";
    private static final String COLUMN_RATING_CHANGE = "rating_change";
    private static final String COLUMN_NEW_RATING = "new_rating";
    private static final String COLUMN_RANK = "rank";

    // Bookmarked problems table
    private static final String TABLE_BOOKMARKED_PROBLEMS = "bookmarked_problems";
    private static final String COLUMN_BOOKMARK_ID = "bookmark_id";
    private static final String COLUMN_BOOKMARK_USER_ID = "user_id";
    private static final String COLUMN_BOOKMARK_URL = "problem_url";
    private static final String COLUMN_BOOKMARK_PROBLEM_CODE = "problem_code";
    private static final String COLUMN_BOOKMARK_PROBLEM_NAME = "problem_name";
    private static final String COLUMN_BOOKMARK_PROBLEM_RATING = "problem_rating";
    private static final String COLUMN_BOOKMARK_CATEGORY = "category"; // Now stores category name
    private static final String COLUMN_BOOKMARK_ADDED_AT = "added_at";

    // Bookmark Categories table
    private static final String TABLE_BOOKMARK_CATEGORIES = "bookmark_categories";
    private static final String COLUMN_CATEGORY_ID = "category_id";
    private static final String COLUMN_CATEGORY_USER_ID = "user_id";
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_CATEGORY_CREATED_AT = "created_at";

    // Notes table
    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_NOTE_ID = "note_id";
    private static final String COLUMN_NOTE_USER_ID = "user_id";
    private static final String COLUMN_NOTE_CONTENT = "content";
    private static final String COLUMN_NOTE_CREATED_AT = "created_at";

    // Learning Topics table
    private static final String TABLE_LEARNING_TOPICS = "learning_topics";
    private static final String COLUMN_TOPIC_ID = "topic_id";
    private static final String COLUMN_TOPIC_USER_ID = "user_id";
    private static final String COLUMN_TOPIC_TITLE = "title";
    private static final String COLUMN_TOPIC_CREATED_AT = "created_at";

    // Learning Resources table
    private static final String TABLE_LEARNING_RESOURCES = "learning_resources";
    private static final String COLUMN_RESOURCE_ID = "resource_id";
    private static final String COLUMN_RESOURCE_TOPIC_ID = "topic_id";
    private static final String COLUMN_RESOURCE_TYPE = "type"; // "text", "file"
    private static final String COLUMN_RESOURCE_CONTENT = "content"; // text content or file path
    private static final String COLUMN_RESOURCE_NAME = "name"; // display name for files
    private static final String COLUMN_RESOURCE_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL,"
                + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Platforms table
        String CREATE_PLATFORMS_TABLE = "CREATE TABLE " + TABLE_PLATFORMS + "("
                + COLUMN_PLATFORM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_PLATFORM_NAME + " TEXT NOT NULL,"
                + COLUMN_HANDLE + " TEXT NOT NULL,"
                + COLUMN_LAST_SYNCED + " INTEGER DEFAULT 0,"
                + COLUMN_RATING + " INTEGER DEFAULT 0,"
                + COLUMN_MAX_RATING + " INTEGER DEFAULT 0,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "UNIQUE(" + COLUMN_USER_ID + "," + COLUMN_PLATFORM_NAME + ")"
                + ")";
        db.execSQL(CREATE_PLATFORMS_TABLE);

        // Solved problems table
        String CREATE_SOLVED_PROBLEMS_TABLE = "CREATE TABLE " + TABLE_SOLVED_PROBLEMS + "("
                + COLUMN_PROBLEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PLATFORM_ID + " INTEGER NOT NULL,"
                + COLUMN_PROBLEM_CODE + " TEXT NOT NULL,"
                + COLUMN_PROBLEM_NAME + " TEXT,"
                + COLUMN_PROBLEM_RATING + " INTEGER,"
                + COLUMN_PROBLEM_CONTEST + " TEXT,"
                + COLUMN_SOLVED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_PLATFORM_ID + ") REFERENCES " + TABLE_PLATFORMS + "(" + COLUMN_PLATFORM_ID
                + "),"
                + "UNIQUE(" + COLUMN_PLATFORM_ID + "," + COLUMN_PROBLEM_CODE + ")"
                + ")";
        db.execSQL(CREATE_SOLVED_PROBLEMS_TABLE);

        // Contests table
        String CREATE_CONTESTS_TABLE = "CREATE TABLE " + TABLE_CONTESTS + "("
                + COLUMN_CONTEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PLATFORM_ID + " INTEGER NOT NULL,"
                + COLUMN_CONTEST_NAME + " TEXT,"
                + COLUMN_CONTEST_DATE + " INTEGER NOT NULL,"
                + COLUMN_RATING_CHANGE + " INTEGER,"
                + COLUMN_NEW_RATING + " INTEGER,"
                + COLUMN_RANK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_PLATFORM_ID + ") REFERENCES " + TABLE_PLATFORMS + "(" + COLUMN_PLATFORM_ID
                + ")"
                + ")";
        db.execSQL(CREATE_CONTESTS_TABLE);

        String CREATE_BOOKMARKED_PROBLEMS_TABLE = "CREATE TABLE " + TABLE_BOOKMARKED_PROBLEMS + "("
                + COLUMN_BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BOOKMARK_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_BOOKMARK_URL + " TEXT NOT NULL,"
                + COLUMN_BOOKMARK_PROBLEM_CODE + " TEXT NOT NULL,"
                + COLUMN_BOOKMARK_PROBLEM_NAME + " TEXT,"
                + COLUMN_BOOKMARK_PROBLEM_RATING + " INTEGER,"
                + COLUMN_BOOKMARK_CATEGORY + " TEXT NOT NULL,"
                + COLUMN_BOOKMARK_ADDED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_BOOKMARK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "UNIQUE(" + COLUMN_BOOKMARK_USER_ID + "," + COLUMN_BOOKMARK_PROBLEM_CODE + ","
                + COLUMN_BOOKMARK_CATEGORY + ")"
                + ")";
        db.execSQL(CREATE_BOOKMARKED_PROBLEMS_TABLE);

        // Categories table
        String CREATE_BOOKMARK_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_BOOKMARK_CATEGORIES + "("
                + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CATEGORY_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
                + COLUMN_CATEGORY_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_CATEGORY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "UNIQUE(" + COLUMN_CATEGORY_USER_ID + "," + COLUMN_CATEGORY_NAME + ")"
                + ")";
        db.execSQL(CREATE_BOOKMARK_CATEGORIES_TABLE);

        // Notes table
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTE_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_NOTE_CONTENT + " TEXT NOT NULL,"
                + COLUMN_NOTE_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_NOTE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_NOTES_TABLE);

        // Learning tables
        String CREATE_TOPICS_TABLE = "CREATE TABLE " + TABLE_LEARNING_TOPICS + "("
                + COLUMN_TOPIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TOPIC_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_TOPIC_TITLE + " TEXT NOT NULL,"
                + COLUMN_TOPIC_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_TOPIC_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_TOPICS_TABLE);

        String CREATE_RESOURCES_TABLE = "CREATE TABLE " + TABLE_LEARNING_RESOURCES + "("
                + COLUMN_RESOURCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_RESOURCE_TOPIC_ID + " INTEGER NOT NULL,"
                + COLUMN_RESOURCE_TYPE + " TEXT NOT NULL,"
                + COLUMN_RESOURCE_CONTENT + " TEXT,"
                + COLUMN_RESOURCE_NAME + " TEXT,"
                + COLUMN_RESOURCE_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_RESOURCE_TOPIC_ID + ") REFERENCES " + TABLE_LEARNING_TOPICS + "("
                + COLUMN_TOPIC_ID + ")"
                + ")";
        db.execSQL(CREATE_RESOURCES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new tables for version 2
            String CREATE_PLATFORMS_TABLE = "CREATE TABLE " + TABLE_PLATFORMS + "("
                    + COLUMN_PLATFORM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USER_ID + " INTEGER NOT NULL,"
                    + COLUMN_PLATFORM_NAME + " TEXT NOT NULL,"
                    + COLUMN_HANDLE + " TEXT NOT NULL,"
                    + COLUMN_LAST_SYNCED + " INTEGER DEFAULT 0,"
                    + COLUMN_RATING + " INTEGER DEFAULT 0,"
                    + COLUMN_MAX_RATING + " INTEGER DEFAULT 0,"
                    + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                    + "UNIQUE(" + COLUMN_USER_ID + "," + COLUMN_PLATFORM_NAME + ")"
                    + ")";
            db.execSQL(CREATE_PLATFORMS_TABLE);

            String CREATE_SOLVED_PROBLEMS_TABLE = "CREATE TABLE " + TABLE_SOLVED_PROBLEMS + "("
                    + COLUMN_PROBLEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_PLATFORM_ID + " INTEGER NOT NULL,"
                    + COLUMN_PROBLEM_CODE + " TEXT NOT NULL,"
                    + COLUMN_PROBLEM_NAME + " TEXT,"
                    + COLUMN_PROBLEM_RATING + " INTEGER,"
                    + COLUMN_SOLVED_AT + " INTEGER NOT NULL,"
                    + "FOREIGN KEY(" + COLUMN_PLATFORM_ID + ") REFERENCES " + TABLE_PLATFORMS + "(" + COLUMN_PLATFORM_ID
                    + "),"
                    + "UNIQUE(" + COLUMN_PLATFORM_ID + "," + COLUMN_PROBLEM_CODE + ")"
                    + ")";
            db.execSQL(CREATE_SOLVED_PROBLEMS_TABLE);

            String CREATE_CONTESTS_TABLE = "CREATE TABLE " + TABLE_CONTESTS + "("
                    + COLUMN_CONTEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_PLATFORM_ID + " INTEGER NOT NULL,"
                    + COLUMN_CONTEST_NAME + " TEXT,"
                    + COLUMN_CONTEST_DATE + " INTEGER NOT NULL,"
                    + COLUMN_RATING_CHANGE + " INTEGER,"
                    + COLUMN_NEW_RATING + " INTEGER,"
                    + COLUMN_RANK + " INTEGER,"
                    + "FOREIGN KEY(" + COLUMN_PLATFORM_ID + ") REFERENCES " + TABLE_PLATFORMS + "(" + COLUMN_PLATFORM_ID
                    + ")"
                    + ")";
            db.execSQL(CREATE_CONTESTS_TABLE);
        }

        if (oldVersion < 3) {
            // Add contest_name column to solved_problems table
            db.execSQL("ALTER TABLE " + TABLE_SOLVED_PROBLEMS + " ADD COLUMN " + COLUMN_PROBLEM_CONTEST + " TEXT");
        }

        if (oldVersion < 4) {
            // Add bookmarked_problems table
            String CREATE_BOOKMARKED_PROBLEMS_TABLE = "CREATE TABLE " + TABLE_BOOKMARKED_PROBLEMS + "("
                    + COLUMN_BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_BOOKMARK_USER_ID + " INTEGER NOT NULL,"
                    + COLUMN_BOOKMARK_URL + " TEXT NOT NULL,"
                    + COLUMN_BOOKMARK_PROBLEM_CODE + " TEXT NOT NULL,"
                    + COLUMN_BOOKMARK_PROBLEM_NAME + " TEXT,"
                    + COLUMN_BOOKMARK_PROBLEM_RATING + " INTEGER,"
                    + COLUMN_BOOKMARK_CATEGORY + " TEXT NOT NULL,"
                    + COLUMN_BOOKMARK_ADDED_AT + " INTEGER NOT NULL,"
                    + "FOREIGN KEY(" + COLUMN_BOOKMARK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID
                    + "),"
                    + "UNIQUE(" + COLUMN_BOOKMARK_USER_ID + "," + COLUMN_BOOKMARK_PROBLEM_CODE + ","
                    + COLUMN_BOOKMARK_CATEGORY + ")"
                    + ")";
            db.execSQL(CREATE_BOOKMARKED_PROBLEMS_TABLE);
        }

        if (oldVersion < 5) {
            // Add categories table
            String CREATE_BOOKMARK_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_BOOKMARK_CATEGORIES + "("
                    + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CATEGORY_USER_ID + " INTEGER NOT NULL,"
                    + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
                    + COLUMN_CATEGORY_CREATED_AT + " INTEGER NOT NULL,"
                    + "FOREIGN KEY(" + COLUMN_CATEGORY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID
                    + "),"
                    + "UNIQUE(" + COLUMN_CATEGORY_USER_ID + "," + COLUMN_CATEGORY_NAME + ")"
                    + ")";
            db.execSQL(CREATE_BOOKMARK_CATEGORIES_TABLE);

            // Initialize default categories for existing users
            // Note: In a real app we'd iterate all users, but here let's assume active user
            // adds them on fetch if missing
        }

        if (oldVersion < 6) {
            // Add notes table
            String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                    + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NOTE_USER_ID + " INTEGER NOT NULL,"
                    + COLUMN_NOTE_CONTENT + " TEXT NOT NULL,"
                    + COLUMN_NOTE_CREATED_AT + " INTEGER NOT NULL,"
                    + "FOREIGN KEY(" + COLUMN_NOTE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                    + ")";
            db.execSQL(CREATE_NOTES_TABLE);
        }

        if (oldVersion < 7) {
            String CREATE_TOPICS_TABLE = "CREATE TABLE " + TABLE_LEARNING_TOPICS + "("
                    + COLUMN_TOPIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TOPIC_USER_ID + " INTEGER NOT NULL,"
                    + COLUMN_TOPIC_TITLE + " TEXT NOT NULL,"
                    + COLUMN_TOPIC_CREATED_AT + " INTEGER NOT NULL,"
                    + "FOREIGN KEY(" + COLUMN_TOPIC_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                    + ")";
            db.execSQL(CREATE_TOPICS_TABLE);

            String CREATE_RESOURCES_TABLE = "CREATE TABLE " + TABLE_LEARNING_RESOURCES + "("
                    + COLUMN_RESOURCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_RESOURCE_TOPIC_ID + " INTEGER NOT NULL,"
                    + COLUMN_RESOURCE_TYPE + " TEXT NOT NULL,"
                    + COLUMN_RESOURCE_CONTENT + " TEXT,"
                    + COLUMN_RESOURCE_NAME + " TEXT,"
                    + COLUMN_RESOURCE_CREATED_AT + " INTEGER NOT NULL,"
                    + "FOREIGN KEY(" + COLUMN_RESOURCE_TOPIC_ID + ") REFERENCES " + TABLE_LEARNING_TOPICS + "("
                    + COLUMN_TOPIC_ID + ")"
                    + ")";
            db.execSQL(CREATE_RESOURCES_TABLE);
        }
    }

    // Hash password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Add new user
    public boolean addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashPassword(password));

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        return result != -1;
    }

    // Check if user exists and password is correct
    public boolean checkUser(String emailOrUsername, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        String query = "SELECT * FROM " + TABLE_USERS + " WHERE ("
                + COLUMN_EMAIL + " = ? OR " + COLUMN_USERNAME + " = ?) AND "
                + COLUMN_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] { emailOrUsername, emailOrUsername, hashedPassword });
        boolean exists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return exists;
    }

    // Check if username already exists
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] { username });
        boolean exists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return exists;
    }

    // Check if email exists
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?",
                new String[] { email });
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Get user ID by username
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?",
                new String[] { username });
        int userId = 1; // Default to 1 if not found
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    // Get username from email or username
    public String getUsername(String emailOrUsername) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USERS
                + " WHERE " + COLUMN_EMAIL + " = ? OR " + COLUMN_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] { emailOrUsername, emailOrUsername });
        String username = null;

        if (cursor.moveToFirst()) {
            username = cursor.getString(0);
        }

        cursor.close();
        db.close();

        return username;
    }

    // Get all users (for debugging)
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT user_id, username, email, created_at FROM " + TABLE_USERS, null);
    }

    // Get total user count
    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // ===== PLATFORM METHODS =====

    // Add or update platform handle
    public long addPlatform(int userId, String platformName, String handle, int rating, int maxRating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_PLATFORM_NAME, platformName);
        values.put(COLUMN_HANDLE, handle);
        values.put(COLUMN_RATING, rating);
        values.put(COLUMN_MAX_RATING, maxRating);
        values.put(COLUMN_LAST_SYNCED, System.currentTimeMillis());

        long result = db.insertWithOnConflict(TABLE_PLATFORMS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        return result;
    }

    // Get platform by user ID and platform name
    public Cursor getPlatform(int userId, String platformName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PLATFORMS + " WHERE "
                + COLUMN_USER_ID + " = ? AND " + COLUMN_PLATFORM_NAME + " = ?";
        return db.rawQuery(query, new String[] { String.valueOf(userId), platformName });
    }

    // Delete platform
    public boolean deletePlatform(int userId, String platformName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PLATFORMS,
                COLUMN_USER_ID + " = ? AND " + COLUMN_PLATFORM_NAME + " = ?",
                new String[] { String.valueOf(userId), platformName });
        db.close();
        return result > 0;
    }

    // ===== SOLVED PROBLEMS METHODS =====

    // Add solved problem
    public long addSolvedProblem(int platformId, String problemCode, String problemName, int rating, long solvedAt,
            String contestName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_PLATFORM_ID, platformId);
        values.put(COLUMN_PROBLEM_CODE, problemCode);
        values.put(COLUMN_PROBLEM_NAME, problemName);
        values.put(COLUMN_PROBLEM_RATING, rating);
        values.put(COLUMN_SOLVED_AT, solvedAt);
        values.put(COLUMN_PROBLEM_CONTEST, contestName);

        long result = db.insertWithOnConflict(TABLE_SOLVED_PROBLEMS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();

        return result;
    }

    // Get all solved problems for a platform
    public Cursor getSolvedProblems(int platformId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SOLVED_PROBLEMS + " WHERE "
                + COLUMN_PLATFORM_ID + " = ? ORDER BY " + COLUMN_SOLVED_AT + " DESC";
        return db.rawQuery(query, new String[] { String.valueOf(platformId) });
    }

    // Get solved problems count for a platform
    public int getSolvedProblemsCount(int platformId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SOLVED_PROBLEMS
                + " WHERE " + COLUMN_PLATFORM_ID + " = ?",
                new String[] { String.valueOf(platformId) });
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // Clear all solved problems for a platform (for re-sync)
    public boolean clearSolvedProblems(int platformId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SOLVED_PROBLEMS, COLUMN_PLATFORM_ID + " = ?",
                new String[] { String.valueOf(platformId) });
        db.close();
        return result > 0;
    }

    // ===== CONTEST METHODS =====

    // Add contest
    public long addContest(int platformId, String contestName, long contestDate,
            int ratingChange, int newRating, int rank) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_PLATFORM_ID, platformId);
        values.put(COLUMN_CONTEST_NAME, contestName);
        values.put(COLUMN_CONTEST_DATE, contestDate);
        values.put(COLUMN_RATING_CHANGE, ratingChange);
        values.put(COLUMN_NEW_RATING, newRating);
        values.put(COLUMN_RANK, rank);

        long result = db.insert(TABLE_CONTESTS, null, values);
        db.close();

        return result;
    }

    // ===== BOOKMARK METHODS =====

    // Add bookmarked problem
    public long addBookmark(int userId, String problemUrl, String problemCode, String problemName,
            int problemRating, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_BOOKMARK_USER_ID, userId);
        values.put(COLUMN_BOOKMARK_URL, problemUrl);
        values.put(COLUMN_BOOKMARK_PROBLEM_CODE, problemCode);
        values.put(COLUMN_BOOKMARK_PROBLEM_NAME, problemName);
        values.put(COLUMN_BOOKMARK_PROBLEM_RATING, problemRating);
        values.put(COLUMN_BOOKMARK_CATEGORY, category);
        values.put(COLUMN_BOOKMARK_ADDED_AT, System.currentTimeMillis());

        long result = db.insertWithOnConflict(TABLE_BOOKMARKED_PROBLEMS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
        // db.close(); // Don't close

        return result;
    }

    // Get bookmarks by user ID and category
    public Cursor getBookmarks(int userId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BOOKMARKED_PROBLEMS + " WHERE "
                + COLUMN_BOOKMARK_USER_ID + " = ? AND " + COLUMN_BOOKMARK_CATEGORY + " = ? "
                + "ORDER BY " + COLUMN_BOOKMARK_ADDED_AT + " DESC";
        return db.rawQuery(query, new String[] { String.valueOf(userId), category });
    }

    // Get all bookmarks by user ID
    public Cursor getAllBookmarks(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BOOKMARKED_PROBLEMS + " WHERE "
                + COLUMN_BOOKMARK_USER_ID + " = ? ORDER BY " + COLUMN_BOOKMARK_ADDED_AT + " DESC";
        return db.rawQuery(query, new String[] { String.valueOf(userId) });
    }

    // Delete bookmark
    public boolean deleteBookmark(int bookmarkId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BOOKMARKED_PROBLEMS, COLUMN_BOOKMARK_ID + " = ?",
                new String[] { String.valueOf(bookmarkId) });
        // db.close();
        return result > 0;
    }

    // Check if problem is already solved
    public boolean isProblemSolved(int userId, String problemCode) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get platform ID for Codeforces
        String platformIdQuery = "SELECT " + COLUMN_PLATFORM_ID + " FROM " + TABLE_PLATFORMS
                + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_PLATFORM_NAME + " = 'Codeforces'";
        Cursor platformCursor = db.rawQuery(platformIdQuery, new String[] { String.valueOf(userId) });

        int platformId = -1;
        if (platformCursor.moveToFirst()) {
            platformId = platformCursor.getInt(0);
        }
        platformCursor.close();

        if (platformId == -1) {
            // db.close();
            return false;
        }

        // Check if problem exists in solved_problems
        String query = "SELECT COUNT(*) FROM " + TABLE_SOLVED_PROBLEMS + " WHERE "
                + COLUMN_PLATFORM_ID + " = ? AND " + COLUMN_PROBLEM_CODE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(platformId), problemCode });

        boolean solved = false;
        if (cursor.moveToFirst()) {
            solved = cursor.getInt(0) > 0;
        }

        cursor.close();
        // db.close();
        return solved;
    }

    // Get bookmark count by category
    public int getBookmarkCount(int userId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKMARKED_PROBLEMS
                + " WHERE " + COLUMN_BOOKMARK_USER_ID + " = ? AND " + COLUMN_BOOKMARK_CATEGORY + " = ?",
                new String[] { String.valueOf(userId), category });
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        // db.close();
        return count;
    }

    // ===== CATEGORY METHODS =====

    public long addCategory(int userId, String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_USER_ID, userId);
        values.put(COLUMN_CATEGORY_NAME, categoryName);
        values.put(COLUMN_CATEGORY_CREATED_AT, System.currentTimeMillis());

        long result = db.insertWithOnConflict(TABLE_BOOKMARK_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        // db.close();
        return result;
    }

    public Cursor getAllCategories(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BOOKMARK_CATEGORIES + " WHERE " + COLUMN_CATEGORY_USER_ID + " = ? " +
                "ORDER BY CASE " + COLUMN_CATEGORY_NAME +
                " WHEN 'Problems to solve' THEN 1 " +
                " WHEN 'Interesting Problems' THEN 2 " +
                " WHEN 'Hard Problems' THEN 3 " +
                " ELSE 4 END, " + COLUMN_CATEGORY_NAME + " ASC";
        return db.rawQuery(query, new String[] { String.valueOf(userId) });
    }

    public boolean deleteCategory(int userId, String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        // First delete all problems in this category
        db.delete(TABLE_BOOKMARKED_PROBLEMS,
                COLUMN_BOOKMARK_USER_ID + " = ? AND " + COLUMN_BOOKMARK_CATEGORY + " = ?",
                new String[] { String.valueOf(userId), categoryName });

        // Then delete the category
        int result = db.delete(TABLE_BOOKMARK_CATEGORIES,
                COLUMN_CATEGORY_USER_ID + " = ? AND " + COLUMN_CATEGORY_NAME + " = ?",
                new String[] { String.valueOf(userId), categoryName });
        // db.close();
        return result > 0;
    }

    public void ensureDefaultCategories(int userId) {
        // Only add if they don't exist
        addCategory(userId, "Problems to solve");
        addCategory(userId, "Interesting Problems");
        addCategory(userId, "Hard Problems");
    }

    // ===== NOTES METHODS =====

    public long addNote(int userId, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_USER_ID, userId);
        values.put(COLUMN_NOTE_CONTENT, content);
        values.put(COLUMN_NOTE_CREATED_AT, System.currentTimeMillis());

        long result = db.insert(TABLE_NOTES, null, values);
        // db.close();
        return result;
    }

    public Cursor getNotes(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_NOTES + " WHERE " + COLUMN_NOTE_USER_ID + " = ? ORDER BY "
                        + COLUMN_NOTE_CREATED_AT + " ASC",
                new String[] { String.valueOf(userId) });
    }

    public boolean deleteNote(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NOTES, COLUMN_NOTE_ID + " = ?", new String[] { String.valueOf(noteId) });
        // db.close();
        return result > 0;
    }

    // ===== LEARNING METHODS =====

    public long addLearningTopic(int userId, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TOPIC_USER_ID, userId);
        values.put(COLUMN_TOPIC_TITLE, title);
        values.put(COLUMN_TOPIC_CREATED_AT, System.currentTimeMillis());
        return db.insert(TABLE_LEARNING_TOPICS, null, values);
    }

    public Cursor getLearningTopics(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_LEARNING_TOPICS + " WHERE " + COLUMN_TOPIC_USER_ID + " = ? ORDER BY "
                        + COLUMN_TOPIC_CREATED_AT + " DESC",
                new String[] { String.valueOf(userId) });
    }

    public boolean deleteLearningTopic(int topicId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete resources first
        db.delete(TABLE_LEARNING_RESOURCES, COLUMN_RESOURCE_TOPIC_ID + " = ?",
                new String[] { String.valueOf(topicId) });
        // Delete topic
        return db.delete(TABLE_LEARNING_TOPICS, COLUMN_TOPIC_ID + " = ?", new String[] { String.valueOf(topicId) }) > 0;
    }

    public long addLearningResource(int topicId, String type, String content, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESOURCE_TOPIC_ID, topicId);
        values.put(COLUMN_RESOURCE_TYPE, type);
        values.put(COLUMN_RESOURCE_CONTENT, content);
        values.put(COLUMN_RESOURCE_NAME, name);
        values.put(COLUMN_RESOURCE_CREATED_AT, System.currentTimeMillis());
        return db.insert(TABLE_LEARNING_RESOURCES, null, values);
    }

    public Cursor getLearningResources(int topicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_LEARNING_RESOURCES + " WHERE " + COLUMN_RESOURCE_TOPIC_ID + " = ? ORDER BY "
                        + COLUMN_RESOURCE_CREATED_AT + " ASC",
                new String[] { String.valueOf(topicId) });
    }

    public boolean deleteLearningResource(int resourceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_LEARNING_RESOURCES, COLUMN_RESOURCE_ID + " = ?",
                new String[] { String.valueOf(resourceId) }) > 0;
    }
}
