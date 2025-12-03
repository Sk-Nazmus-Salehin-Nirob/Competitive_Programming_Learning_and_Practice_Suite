package com.cplps.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.cplps.android.database.DatabaseHelper;
import com.cplps.android.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmailUsername, editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textViewSignUp;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        editTextEmailUsername = findViewById(R.id.editTextEmailUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.textViewSignUp);

        // Initialize database and session
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Login button click
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Sign up link click
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }

    private void loginUser() {
        String emailOrUsername = editTextEmailUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(emailOrUsername)) {
            editTextEmailUsername.setError(getString(R.string.error_empty_email));
            editTextEmailUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.error_empty_password));
            editTextPassword.requestFocus();
            return;
        }

        // Check credentials
        if (databaseHelper.checkUser(emailOrUsername, password)) {
            // Get username
            String username = databaseHelper.getUsername(emailOrUsername);

            // Create session
            sessionManager.createLoginSession(username, emailOrUsername);

            Toast.makeText(this, getString(R.string.success_login), Toast.LENGTH_SHORT).show();

            // Go to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
        }
    }
}
