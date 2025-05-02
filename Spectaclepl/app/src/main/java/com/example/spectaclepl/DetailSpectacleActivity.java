package com.example.spectaclepl;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailSpectacleActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private String lieu; // Store location as class variable
    private List<Spectacle> spectaclesSimilaires;
    private Spinner spinnerDate, spinnerHeure, spinnerLieu;
    private String currentMatricule;
    private Spectacle currentSpectacle; // Ajoutez cette variable de classe
    private List<Spectacle> spectacleList;
    private TextView placeSpectacle;
    private int currentPlaceDispo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_spectacle);

        // Initialize views
        ImageView imageSpectacle = findViewById(R.id.image_spectacle);
        ImageView imagelieu = findViewById(R.id.lieu_icon);
        imagelieu.setOnClickListener(v -> {
            Log.d("MAPS", "Tentative d'ouverture de Maps pour: " + lieu);
            if (lieu == null || lieu.isEmpty()) {
                Toast.makeText(this, "Adresse non disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            if (checkLocationPermission()) {
                openGoogleMaps();
            }
        });

        TextView titreSpectacle = findViewById(R.id.titre_spectacle);
        TextView descriptionSpectacle = findViewById(R.id.description_spectacle);
        spinnerDate = findViewById(R.id.spinner_date);
        spinnerHeure = findViewById(R.id.spinner_heure);
        spinnerLieu = findViewById(R.id.spinner_lieu);
        TextView prixSpectacle = findViewById(R.id.prix_spectacle);
        Button boutonAchat = findViewById(R.id.bouton_achat);
        placeSpectacle = findViewById(R.id.places_dispo);


        // Get data from intent
        Intent intent = getIntent();
        String titre = intent.getStringExtra("titre");
        String description = intent.getStringExtra("description");
        String date = intent.getStringExtra("date");
        String heure = intent.getStringExtra("heureDebut");
        lieu = intent.getStringExtra("lieu"); // Store location in class variable
        String prix = intent.getStringExtra("prix");
        String imageUrl = intent.getStringExtra("image");
        int placeDispo = intent.getIntExtra("place_dispo", 0);
        String matricule = intent.getStringExtra("matricule");



        spectacleList = new ArrayList<>();
        spectaclesSimilaires = new ArrayList<>();

        fetchSpectacles();
        // Display data
        titreSpectacle.setText(titre);
        descriptionSpectacle.setText(description);


        prixSpectacle.setText(prix + " DT");
        placeSpectacle.setText(getString(R.string.places_disponibles, placeDispo));
        // Dans votre méthode onCreate(), après avoir initialisé les autres vues
        ImageView gifView = findViewById(R.id.gif_animation);

// Charger le GIF depuis les ressources (si vous avez ajouté le GIF dans res/drawable)
        Glide.with(this)
                .asGif()
                .load(R.drawable.giphy_4_1) // Remplacez par le nom de votre fichier GIF
                .into(gifView);


        // Load image
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(imageSpectacle);

        // Set click listeners
        setupClickListeners(titre, heure, prix, lieu, date, placeDispo, imageUrl,matricule);
    }



    private void fetchSpectacles() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Spectacle>> call = apiService.getSpectacles();

        call.enqueue(new Callback<List<Spectacle>>() {
            @Override
            public void onResponse(Call<List<Spectacle>> call, Response<List<Spectacle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spectacleList = response.body();
                    // Filtrer les spectacles similaires après chargement
                    filtrerSpectaclesSimilaires(getIntent().getStringExtra("titre"));
                }
            }

            @Override
            public void onFailure(Call<List<Spectacle>> call, Throwable t) {
                Toast.makeText(DetailSpectacleActivity.this,
                        "Échec de la connexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void filtrerSpectaclesSimilaires(String titre) {
        spectaclesSimilaires.clear();

        for (Spectacle spectacle : spectacleList) {
            if (spectacle.getTitre().equalsIgnoreCase(titre)) {
                spectaclesSimilaires.add(spectacle);
            }
        }

        if (!spectaclesSimilaires.isEmpty()) {
            configurerSpinners();
            selectionnerValeursActuelles();
        }
    }


    private void setupClickListeners(String titre, String heure, String prix,
                                     String lieu, String date, int placeDispo,
                                     String imageUrl, String matricule) {
        findViewById(R.id.bouton_achat).setOnClickListener(v -> {
            // Récupérer la source de l'intent
            String source = getIntent().getStringExtra("source");

            String selectedDate = spinnerDate.getSelectedItem().toString();
            String selectedHeure = spinnerHeure.getSelectedItem().toString();
            String selectedLieu = spinnerLieu.getSelectedItem().toString();
            currentSpectacle = findSelectedSpectacle(selectedDate, selectedHeure, selectedLieu);

            // Créer l'intent de réservation
            Intent bookingIntent = new Intent(DetailSpectacleActivity.this, TicketBookingActivity.class);
            String completeImageUrl = imageUrl.startsWith("http") ? imageUrl
                    : "http://172.18.2.58:3000/public/images/" + imageUrl;            // Ajouter les données du spectacle
            bookingIntent.putExtra("titre", currentSpectacle.getTitre());
            bookingIntent.putExtra("heure", currentSpectacle.getHeureDebut());
            bookingIntent.putExtra("prix", currentSpectacle.getPrixTicket());
            bookingIntent.putExtra("lieu", currentSpectacle.getLieu());
            bookingIntent.putExtra("date", currentSpectacle.getDate());
            bookingIntent.putExtra("place_dispo", currentSpectacle.getplacedispo());
            bookingIntent.putExtra("image", completeImageUrl);
            bookingIntent.putExtra("matricule", currentSpectacle.getMatricule());
            bookingIntent.putExtra("source", getIntent().getStringExtra("source")); // Transmettre la source

            // Si l'utilisateur vient du login, essayer de récupérer ses infos
            if ("login".equals(source)) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String token = prefs.getString("token", "");

                if (!token.isEmpty()) {
                    ApiService apiService = RetrofitClient.getApiService();
                    Call<User> call = apiService.getProfile("Bearer " + token);
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                bookingIntent.putExtra("user", response.body());
                            }
                            startActivity(bookingIntent); // Déplacez ici
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            startActivity(bookingIntent);
                        }
                    });
                    return; // Important pour ne pas exécuter le startActivity ci-dessous
                }
            }

            // Pour le mode explorer ou si le token est vide
            startActivity(bookingIntent);
        });
    }
    private Spectacle findSelectedSpectacle(String date, String heure, String lieu) {
        for (Spectacle spectacle : spectaclesSimilaires) {
            String spectacleDate = spectacle.getDate().contains("T")
                    ? spectacle.getDate().split("T")[0]
                    : spectacle.getDate();

            if (spectacleDate.equals(date) &&
                    spectacle.getHeureDebut().equals(heure) &&
                    spectacle.getLieu().equals(lieu)) {
                return spectacle;
            }
        }
        return null;
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return false;
        }
        return true;
    }

    private void configurerSpinners() {
        List<String> dates = new ArrayList<>();
        List<String> heures = new ArrayList<>();
        List<String> lieux = new ArrayList<>();

        for (Spectacle spectacle : spectaclesSimilaires) {
            String date = spectacle.getDate().contains("T") ?
                    spectacle.getDate().split("T")[0] : spectacle.getDate();
            if (!dates.contains(date)) dates.add(date);
            if (!heures.contains(spectacle.getHeureDebut())) heures.add(spectacle.getHeureDebut());
            if (!lieux.contains(spectacle.getLieu())) lieux.add(spectacle.getLieu());
        }

        // Configurer les adaptateurs
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_white, dates)
        {
            @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            ((TextView) view).setTextColor(Color.WHITE);
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            ((TextView) view).setTextColor(Color.WHITE);
            view.setBackgroundColor(Color.parseColor("#2A2A2A"));
            return view;
        }
    };
    dateAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white);
        spinnerDate.setAdapter(dateAdapter);

        ArrayAdapter<String> heureAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_white, heures){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.parseColor("#2A2A2A"));
                return view;
            }
        };
        heureAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white);
        spinnerHeure.setAdapter(heureAdapter);

        ArrayAdapter<String> lieuAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_white, lieux){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.parseColor("#2A2A2A"));
                return view;
            }
        };
        lieuAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white);

        spinnerLieu.setAdapter(lieuAdapter);

        // Configurer les listeners
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mettreAJourDetailsSpectacle();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerDate.setOnItemSelectedListener(spinnerListener);
        spinnerHeure.setOnItemSelectedListener(spinnerListener);
        spinnerLieu.setOnItemSelectedListener(spinnerListener);
    }

    private void mettreAJourDetailsSpectacle() {
        String selectedDate = spinnerDate.getSelectedItem().toString();
        String selectedHeure = spinnerHeure.getSelectedItem().toString();
        String selectedLieu = spinnerLieu.getSelectedItem().toString();

        for (Spectacle spectacle : spectaclesSimilaires) {
            String date = spectacle.getDate().contains("T") ?
                    spectacle.getDate().split("T")[0] : spectacle.getDate();

            if (date.equals(selectedDate) &&
                    spectacle.getHeureDebut().equals(selectedHeure) &&
                    spectacle.getLieu().equals(selectedLieu)) {

                // Mettre à jour l'interface avec les nouvelles données
                updateUIWithSpectacleDetails(spectacle);
                break;
            }
        }
    }


    private void updateUIWithSpectacleDetails(Spectacle spectacle) {
        // Mettez à jour toutes les vues avec les nouvelles données
        TextView descriptionSpectacle = findViewById(R.id.description_spectacle);
        TextView prixSpectacle = findViewById(R.id.prix_spectacle);
        TextView placeSpectacle = findViewById(R.id.places_dispo);
        ImageView imageSpectacle = findViewById(R.id.image_spectacle);

        descriptionSpectacle.setText(spectacle.getDescription());
        prixSpectacle.setText(spectacle.getPrixTicket() + " DT");
        placeSpectacle.setText(getString(R.string.places_disponibles, spectacle.getplacedispo()));

        Glide.with(this)
                .load("http://172.18.2.58:3000/public/images/" + spectacle.getimageResId())
                .placeholder(R.drawable.placeholder)
                .into(imageSpectacle);

        // Stocker les valeurs actuelles
        currentMatricule = spectacle.getMatricule();
        currentPlaceDispo = spectacle.getplacedispo();
        lieu = spectacle.getLieu();
    }

    private void selectionnerValeursActuelles() {
        String currentDate = getIntent().getStringExtra("date");
        String currentHeure = getIntent().getStringExtra("heureDebut");
        String currentLieu = getIntent().getStringExtra("lieu");

        if (currentDate != null && currentDate.contains("T")) {
            currentDate = currentDate.split("T")[0];
        }

        // Sélectionner les valeurs dans les spinners
        setSpinnerSelection(spinnerDate, currentDate);
        setSpinnerSelection(spinnerHeure, currentHeure);
        setSpinnerSelection(spinnerLieu, currentLieu);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void openGoogleMaps() {
        Log.d("MAPS_DEBUG", "Lieu: " + lieu);
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(lieu));
            Log.d("MAPS_DEBUG", "URI générée: " + gmmIntentUri);

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Log.e("MAPS_DEBUG", "Google Maps non installé");
                // Fallback au navigateur
                Uri webpage = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(lieu));
                startActivity(new Intent(Intent.ACTION_VIEW, webpage));
            }
        } catch (Exception e) {
            Log.e("MAPS_DEBUG", "Erreur: " + e.getMessage());
            Toast.makeText(this, "Erreur d'ouverture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGoogleMaps();
            } else {
                Toast.makeText(this,
                        getString(R.string.location_permission_required),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}