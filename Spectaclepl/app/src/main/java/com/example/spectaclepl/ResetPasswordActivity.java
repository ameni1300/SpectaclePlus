package com.example.spectaclepl;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private ApiService apiService;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize views
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitClient.getApiService();

        // Get email from intent
        userEmail = getIntent().getStringExtra("email");

        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (newPassword.isEmpty()) {
            etNewPassword.setError("Nouveau mot de passe requis");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Le mot de passe doit contenir au moins 6 caractères");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Les mots de passe ne correspondent pas");
            etConfirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setEnabled(false);

        // Create request body
        Map<String, String> fields = new HashMap<>();
        fields.put("email", userEmail);
        fields.put("newPassword", newPassword);

        Call<ApiResponse> call = apiService.resetPassword(fields);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnResetPassword.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(ResetPasswordActivity.this,
                                "Mot de passe réinitialisé avec succès",
                                Toast.LENGTH_LONG).show();
                        finish(); // Return to login activity
                    } else {
                        Toast.makeText(ResetPasswordActivity.this,
                                response.body().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this,
                            "Erreur lors de la réinitialisation",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnResetPassword.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this,
                        "Erreur: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}