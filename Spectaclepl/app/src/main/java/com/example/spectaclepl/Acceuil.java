package com.example.spectaclepl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Acceuil extends AppCompatActivity {

    private EditText searchBar;
    private TextView welcomeText;
    private Spinner filterSpinner;
    private RecyclerView recyclerComedie, recyclerTheatre, recyclerMusical;
    private SpectacleAdapter adapterComedie, adapterTheatre, adapterMusical;
    private List<Spectacle> spectaclesList = new ArrayList<>();
    private List<Spectacle> comedieList = new ArrayList<>();
    private List<Spectacle> theatreList = new ArrayList<>();
    private List<Spectacle> musicalList = new ArrayList<>();
    private List<Spectacle> suggestedSpectacles = new ArrayList<>();
    private User currentUser;
    private ImageView userIcon;
    private PopupMenu userPopupMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceuil);

        initializeViews();
        setupUserWelcome();
        setupAdapters();
        setupSpinner();
        setupSearchBar();
        setupUserIconClickListener();
        userIcon = findViewById(R.id.user_icon);

        fetchSpectacles();
    }

    private void initializeViews() {
        searchBar = findViewById(R.id.search_bar);
        filterSpinner = findViewById(R.id.filter_spinner);
        welcomeText = findViewById(R.id.user);
        recyclerComedie = findViewById(R.id.recycler_Comédie);
        recyclerMusical = findViewById(R.id.recycler_mmusic);
        recyclerTheatre = findViewById(R.id.recycler_theatre);
        userIcon = findViewById(R.id.user_icon);
    }
    private void setupUserIconClickListener() {
        if (userIcon == null) {
            Log.e("Acceuil", "Erreur: userIcon est null");
            return;
        }

        userIcon.setOnClickListener(v -> {
            if (currentUser != null) {
                showUserPopupMenu();
            } else {
                Log.w("Acceuil", "Aucun utilisateur connecté");
            }
        });
    }

    private void showUserPopupMenu() {
        if (isFinishing() || isDestroyed()) {
            return; // Empêche les crashes si l'activité est en train de se fermer
        }

        try {
            userPopupMenu = new PopupMenu(this, userIcon);
            userPopupMenu.getMenuInflater().inflate(R.menu.user_menu, userPopupMenu.getMenu());



            userPopupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_manage_account) {
                    manageAccount();
                    return true;
                } else if (itemId == R.id.menu_logout) {
                    logout();
                    return true;
                }
                return false;
            });

            // Ajoutez un listener pour détecter les erreurs d'affichage
            userPopupMenu.setOnDismissListener(menu -> {
                Log.d("POPUP_MENU", "Menu dismissé");
            });

            userPopupMenu.show();
        } catch (Exception e) {
            Log.e("POPUP_MENU", "Erreur d'affichage du menu", e);
            // Fallback: Affichez un Toast ou un Dialog simple
            Toast.makeText(this, "Erreur d'affichage du menu", Toast.LENGTH_SHORT).show();
        }
    }

    private void manageAccount() {
        // Ouvrir l'activité de gestion de compte
        Intent intent = new Intent(this, ManageAccountActivity.class);
        intent.putExtra("user", currentUser);
        startActivity(intent);
    }

    private void logout() {
        // Supprimer le token et déconnecter
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().remove("token").apply();

        // Rediriger vers l'écran de login
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupUserWelcome() {
        String source = getIntent().getStringExtra("source");
        ImageView userIcon = findViewById(R.id.user_icon);
        if ("login".equals(source)) {
            currentUser = (User) getIntent().getSerializableExtra("user");
            if (currentUser != null) {
                welcomeText.setText(currentUser.getUsername() + "!");
                userIcon.setVisibility(View.VISIBLE);
                userIcon.setClickable(true);
            }
        } else {
            // Si c'est Explorer, on ne touche pas welcomeText
            welcomeText.setText(""); // ou un texte par défaut vide
            userIcon.setVisibility(View.GONE);
            userIcon.setClickable(false);
        }
    }


    private void checkTokenAndFetchUser() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        if (!token.isEmpty()) {
            ApiService apiService = RetrofitClient.getApiService();
            Call<User> call = apiService.getProfile(token);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body();
                        welcomeText.setText(  currentUser.getUsername() + "!");
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(Acceuil.this, "Erreur de chargement du profil", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupAdapters() {
        adapterComedie = new SpectacleAdapter(this, comedieList);
        adapterTheatre = new SpectacleAdapter(this, theatreList);
        adapterMusical = new SpectacleAdapter(this, musicalList);

        // Ajoutez les listeners pour chaque adapter
        adapterComedie.setOnItemClickListener(spectacle -> {
            openSpectacleDetails(spectacle);
        });

        adapterTheatre.setOnItemClickListener(spectacle -> {
            openSpectacleDetails(spectacle);
        });

        adapterMusical.setOnItemClickListener(spectacle -> {
            openSpectacleDetails(spectacle);
        });

        recyclerComedie.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerComedie.setAdapter(adapterComedie);

        recyclerMusical.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerMusical.setAdapter(adapterMusical);

        recyclerTheatre.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerTheatre.setAdapter(adapterTheatre);
    }

    private void openSpectacleDetails(Spectacle spectacle) {
        Intent intent = new Intent(Acceuil.this, DetailSpectacleActivity.class);

        // Passez la source et l'utilisateur si disponible
        String source = getIntent().getStringExtra("source");
        intent.putExtra("source", source);

        if ("login".equals(source) && currentUser != null) {
            intent.putExtra("user", currentUser);
        }

        // Ajoutez les données du spectacle
        intent.putExtra("titre", spectacle.getTitre());
        intent.putExtra("description", spectacle.getDescription());
        intent.putExtra("date", spectacle.getDate());
        intent.putExtra("heureDebut", spectacle.getHeureDebut());
        intent.putExtra("lieu", spectacle.getLieu());
        intent.putExtra("prix", spectacle.getPrixTicket());
        intent.putExtra("image", spectacle.getimageResId());
        intent.putExtra("place_dispo", spectacle.getplacedispo());
        intent.putExtra("matricule", spectacle.getMatricule());

        startActivity(intent);
    }

    private void setupSpinner() {
        String[] filters = {"Titre", "Date", "Lieu", "Localisation"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filters);
        filterSpinner.setAdapter(spinnerAdapter);
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString(), filterSpinner.getSelectedItem().toString());
            }
        });
    }

    private void fetchSpectacles() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Spectacle>> call = apiService.getSpectacles();

        call.enqueue(new Callback<List<Spectacle>>() {
            @Override
            public void onResponse(Call<List<Spectacle>> call, Response<List<Spectacle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spectaclesList = response.body();
                    getSuggestedSpectacles();
                    displaySuggestedSpectacles();
                } else {
                    Toast.makeText(Acceuil.this, "Erreur de chargement des spectacles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Spectacle>> call, Throwable t) {
                Toast.makeText(Acceuil.this, "Échec de la connexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSuggestedSpectacles() {
        suggestedSpectacles.clear();
        Set<String> spectacleNames = new HashSet<>();

        for (Spectacle spectacle : spectaclesList) {
            if (!spectacleNames.contains(spectacle.getTitre())) {
                suggestedSpectacles.add(spectacle);
                spectacleNames.add(spectacle.getTitre());
            }
        }
    }

    private void displaySuggestedSpectacles() {
        comedieList.clear();
        theatreList.clear();
        musicalList.clear();

        for (Spectacle spectacle : suggestedSpectacles) {
            switch (spectacle.getCategorie().toLowerCase()) {
                case "comédie":
                    comedieList.add(spectacle);
                    break;
                case "théâtre":
                    theatreList.add(spectacle);
                    break;
                case "musique":
                    musicalList.add(spectacle);
                    break;
            }
        }

        adapterComedie.updateList(comedieList);
        adapterTheatre.updateList(theatreList);
        adapterMusical.updateList(musicalList);
    }

    private void filterList(String query, String filterType) {
        if (query.isEmpty()) {
            displaySuggestedSpectacles();
            return;
        }

        List<Spectacle> filteredComedie = new ArrayList<>();
        List<Spectacle> filteredTheatre = new ArrayList<>();
        List<Spectacle> filteredMusical = new ArrayList<>();

        for (Spectacle spectacle : spectaclesList) {
            boolean match = false;
            String filterValue = "";

            switch (filterType) {
                case "Titre":
                    filterValue = spectacle.getTitre().toLowerCase();
                    break;
                case "Date":
                    filterValue = spectacle.getDate().toLowerCase();
                    break;
                case "Lieu":
                    filterValue = spectacle.getLieu().toLowerCase();
                    break;
                case "Localisation":
                    filterValue = spectacle.getlocalisation().toLowerCase();
                    break;
            }

            if (filterValue.contains(query.toLowerCase())) {
                switch (spectacle.getCategorie().toLowerCase()) {
                    case "comédie":
                        filteredComedie.add(spectacle);
                        break;
                    case "théâtre":
                        filteredTheatre.add(spectacle);
                        break;
                    case "musique":
                        filteredMusical.add(spectacle);
                        break;
                }
            }
        }

        adapterComedie.updateList(filteredComedie);
        adapterTheatre.updateList(filteredTheatre);
        adapterMusical.updateList(filteredMusical);
    }
}