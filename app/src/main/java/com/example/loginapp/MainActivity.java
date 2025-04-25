package com.example.loginapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loginapp.api.ApiService;
import com.example.loginapp.models.LoginRequest;
import com.example.loginapp.models.LoginResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private View progressBar;
    private ApiService apiService;
    private static final String BASE_URL = "https://dev-otp.yacorpt.com/";
    private static final String TOKEN = "your-secret-token-here";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRetrofit();
        setupLoginButton();
    }

    private void initViews() {
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInput(username, password)) {
            performLogin(username, password);
        }
    }

    private boolean validateInput(String username, String password) {
        boolean isValid = true;

        if (username.isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            isValid = false;
        } else {
            usernameLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password cannot be empty");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }

    private void performLogin(String username, String password) {
        setLoading(true);

        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<LoginResponse> call = apiService.login("login", TOKEN, loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginResponse(response.body());
                } else {
                    showError("Login failed. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void handleLoginResponse(LoginResponse response) {
        if (response.isSuccess()) {
            // Navigate to Home Activity
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        } else {
            showError(response.getMessage());
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setLoading(boolean isLoading) {
        loginButton.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
