package com.example.spectaclepl;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageAccountActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, currentPasswordInput, newPasswordInput, phoneInput;
    private Button saveButton;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        // Initialiser les vues
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email);
        currentPasswordInput = findViewById(R.id.password_actuel);
        newPasswordInput = findViewById(R.id.password);
        phoneInput = findViewById(R.id.name_input);
        saveButton = findViewById(R.id.btn_modif);

        // Récupérer l'utilisateur connecté
        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser != null) {
            // Pré-remplir les champs (sauf mot de passe)
            usernameInput.setText(currentUser.getUsername());
            emailInput.setText(currentUser.getEmail());
            phoneInput.setText(currentUser.getPhone());
        }

        saveButton.setOnClickListener(v -> updateUserProfile());
    }

    private void updateUserProfile() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Validation basique
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si l'utilisateur veut changer le mot de passe
        if (!newPassword.isEmpty() && currentPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre mot de passe actuel", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer le token
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        Log.d("AUTH", "Token: " + token);

        if (token.isEmpty()) {
            Toast.makeText(this, "Session expirée, veuillez vous reconnecter", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Créer l'objet de mise à jour
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                username,
                email,
                phone,
                currentPassword,
                newPassword.isEmpty() ? null : newPassword
        );
        // Ajouter des logs pour le débogage
        Log.d("API_CALL", "Envoi de la requête de mise à jour");
        ApiService apiService = RetrofitClient.getApiService();
        String tokenn = "Bearer " + prefs.getString("token", "");
        Log.d("AUTH", "Token envoyé: " + tokenn);
        Call<User> call = apiService.updateUserProfile( tokenn, updateRequest);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageAccountActivity.this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    currentUser = response.body();
                } else {

                    Log.e("API_ERROR", "Code: " + response.code() + " - " + response.message());
                    try {
                        Log.e("API_ERROR", "Erreur body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ManageAccountActivity.this, "Erreur: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ManageAccountActivity.this, "Échec de la connexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}