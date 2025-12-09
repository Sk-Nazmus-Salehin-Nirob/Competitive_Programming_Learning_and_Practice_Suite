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
    private static final int DATABASE_VERSION = 2;

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

    // Contests table
    private static final String TABLE_CONTESTS = "contests";
    private static final String COLUMN_CONTEST_ID = "contest_id";
    private static final String COLUMN_CONTEST_NAME = "contest_name";
    private static final String COLUMN_CONTEST_DATE = "contest_date";
    private static final String COLUMN_RATING_CHANGE = "rating_change";
    private static final String COLUMN_NEW_RATING = "new_rating";
    private static final String COLUMN_RANK = "rank";

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
    public long addSolvedProblem(int platformId, String problemCode, String problemName, int rating, long solvedAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_PLATFORM_ID, platformId);
        values.put(COLUMN_PROBLEM_CODE, problemCode);
        values.put(COLUMN_PROBLEM_NAME, problemName);
        values.put(COLUMN_PROBLEM_RATING, rating);
        values.put(COLUMN_SOLVED_AT, solvedAt);

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
}
