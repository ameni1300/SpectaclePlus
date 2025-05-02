package com.example.spectaclepl;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail, etVerificationCode;
    private Button btnSendCode, btnVerifyCode;
    private TextView tvPassword;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configure window before content view
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setBackgroundDrawable(null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        etEmail = findViewById(R.id.et_email);
        etVerificationCode = findViewById(R.id.et_verification_code);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnVerifyCode = findViewById(R.id.btn_verify_code);
        tvPassword = findViewById(R.id.tv_password);

        apiService = RetrofitClient.getApiService();

        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        btnVerifyCode.setOnClickListener(v -> verifyCode());
    }

    private void sendVerificationCode() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email invalide");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSendCode.setEnabled(false);

        Call<ApiResponse> call = apiService.sendPasswordResetCode(email);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnSendCode.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        showVerificationUI();
                        // En développement, vous pouvez afficher le code dans un Toast pour tester
                        // À supprimer en production
                        if (response.body().getCode() != null) {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Code de test: " + response.body().getCode(),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        showError(response.body().getMessage());
                    }
                } else {
                    showError("Erreur: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSendCode.setEnabled(true);
                showError("Connexion échouée: " + t.getLocalizedMessage());
            }
        });
    }

    private void verifyCode() {
        String email = etEmail.getText().toString().trim();
        String enteredCode = etVerificationCode.getText().toString().trim();

        if (enteredCode.isEmpty()) {
            etVerificationCode.setError("Code requis");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnVerifyCode.setEnabled(false);

        // Créez un objet pour envoyer les données
        Map<String, String> fields = new HashMap<>();
        fields.put("email", email);
        fields.put("code", enteredCode);

        Call<ApiResponse> call = apiService.verifyResetCode(fields);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnVerifyCode.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        // Code vérifié avec succès, rediriger vers l'écran de nouveau mot de passe
                        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        showError(response.body().getMessage());
                    }
                } else {
                    showError("Code invalide ou expiré");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnVerifyCode.setEnabled(true);
                showError("Erreur: " + t.getLocalizedMessage());
            }
        });
    }

    private void showVerificationUI() {
        runOnUiThread(() -> {
            etVerificationCode.setVisibility(View.VISIBLE);
            btnVerifyCode.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Code envoyé à votre email", Toast.LENGTH_SHORT).show();
        });
    }

    private void showError(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        );
    }



    private void fetchCurrentPassword(String email) {
        progressBar.setVisibility(View.VISIBLE);
        btnVerifyCode.setEnabled(false);

        Call<PasswordResponse> call = apiService.getCurrentPassword(email);
        call.enqueue(new Callback<PasswordResponse>() {
            @Override
            public void onResponse(Call<PasswordResponse> call, Response<PasswordResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnVerifyCode.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    tvPassword.setText("Votre mot de passe actuel: " + response.body().getPassword());
                    tvPassword.setVisibility(View.VISIBLE);
                } else {
                    showError("Erreur lors de la récupération");
                }
            }

            @Override
            public void onFailure(Call<PasswordResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnVerifyCode.setEnabled(true);
                showError("Erreur: " + t.getLocalizedMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
}