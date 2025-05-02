package com.example.spectaclepl;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class TicketBookingActivity extends AppCompatActivity {

    private EditText inputNom, inputEmail, inputTelephone;
    private TextView ticketCountText, totalPrix, totalTicket;
    private ImageButton btnPlus, btnMoins;
    private Button boutonPaiement;
    private int ticketCount = 1;
    private int ticketPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        ImageView imageSpectacle = findViewById(R.id.image_spectacle);
        TextView titreSpectacle = findViewById(R.id.titre_spectacle);
        TextView titree = findViewById(R.id.ticket_detailstitre);
        TextView placeSpectacle = findViewById(R.id.places_dispo);
        inputNom = findViewById(R.id.input_nom);
        inputEmail = findViewById(R.id.input_email);
        inputTelephone = findViewById(R.id.input_telephone);
        ticketCountText = findViewById(R.id.ticket_count_text);
        totalPrix = findViewById(R.id.total_price);
        totalTicket = findViewById(R.id.totalticket);
        btnPlus = findViewById(R.id.btn_plus);
        btnMoins = findViewById(R.id.btn_moins);
        boutonPaiement = findViewById(R.id.bouton_paiement);
        TextView heureSpectacle = findViewById(R.id.session_time);
        TextView lieuSpectacle = findViewById(R.id.theater_location);
        TextView dateSpectacle = findViewById(R.id.session_date);

        Intent intent = getIntent();
        String titre = intent.getStringExtra("titre");
        String date = intent.getStringExtra("date");
        String heure = intent.getStringExtra("heure");
        String lieu = intent.getStringExtra("lieu");
        String prix = intent.getStringExtra("prix");
        String imageUrl = intent.getStringExtra("image");
        int placeDispo = intent.getIntExtra("place_dispo", 0);
        String matricule = intent.getStringExtra("matricule");
        User user = (User) getIntent().getSerializableExtra("user");

        titreSpectacle.setText(titre);
        titree.setText(titre);

        if (date != null && date.contains("T")) {
            String[] parts = date.split("T");
            dateSpectacle.setText(parts[0]);
        } else {
            dateSpectacle.setText(date);
        }


        if (user != null) {
            inputNom.setText(user.getUsername());
            inputEmail.setText(user.getEmail());
            inputTelephone.setText(user.getPhone());

            // Désactiver l'édition si nécessaire
            inputNom.setEnabled(false);
            inputEmail.setEnabled(false);
            inputTelephone.setEnabled(false);

            checkFields();
        }

        heureSpectacle.setText(heure);
        lieuSpectacle.setText(lieu);
        placeSpectacle.setText("Places disponibles : " + placeDispo);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(imageSpectacle);

        if (prix != null && prix.contains("DT")) {
            prix = prix.replace("DT", "").trim();
        }

        try {
            ticketPrice = Integer.parseInt(prix);
        } catch (NumberFormatException e) {
            ticketPrice = 0;
            e.printStackTrace();
        }

        updatePrice();
        updateTicketCount();

        btnPlus.setOnClickListener(v -> {
            if (ticketCount < placeDispo) {
                ticketCount++;
                updatePrice();
                updateTicketCount();
            }
        });

        btnMoins.setOnClickListener(v -> {
            if (ticketCount > 1) {
                ticketCount--;
                updatePrice();
                updateTicketCount();
            }
        });

        // Vérification dynamique des champs
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        inputNom.addTextChangedListener(textWatcher);
        inputEmail.addTextChangedListener(textWatcher);
        inputTelephone.addTextChangedListener(textWatcher);

        boutonPaiement.setOnClickListener(v -> {
            String nom = inputNom.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String telephone = inputTelephone.getText().toString().trim();

            boolean isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
            boolean isPhoneValid = telephone.matches("\\d{8}");

            boolean allValid = true;

            if (nom.isEmpty()) {
                inputNom.setError("Champ requis");
                allValid = false;
            }

            if (email.isEmpty()) {
                inputEmail.setError("Champ requis");
                allValid = false;
            } else if (!isEmailValid) {
                inputEmail.setError("Email invalide");
                allValid = false;
            } else {
                inputEmail.setError(null);
            }

            if (telephone.isEmpty()) {
                inputTelephone.setError("Champ requis");
                allValid = false;
            } else if (!isPhoneValid) {
                inputTelephone.setError("Téléphone invalide (8 chiffres)");
                allValid = false;
            } else {
                inputTelephone.setError(null);
            }
            if (ticketCount > placeDispo) {
                Toast.makeText(this, "Nombre de tickets insuffisant", Toast.LENGTH_LONG).show();
                allValid = false;
            }

            if (!allValid) {

                return;
            }

            // Si tout est valide → continuer
            Intent bintent = new Intent(TicketBookingActivity.this, PaymentActivity.class);
            bintent.putExtra("nom", nom);
            bintent.putExtra("email", email);
            bintent.putExtra("telephone", telephone);
            bintent.putExtra("tickets", ticketCount);
            bintent.putExtra("placedisp", placeDispo);
            bintent.putExtra("prix", updatePrice());
            bintent.putExtra("titre", titre);
            bintent.putExtra("date", date);
            bintent.putExtra("heure", heure);
            bintent.putExtra("lieu", lieu);
            bintent.putExtra("matricule", matricule);
            startActivity(bintent);
        });

    }

    private int updatePrice() {
        int total = ticketCount * ticketPrice;
        String totalString = total + " DT";
        totalPrix.setText(totalString);
        return total;
    }

    private void updateTicketCount() {
        ticketCountText.setText(String.valueOf(ticketCount));
        totalTicket.setText(String.valueOf(ticketCount));
    }

    private void checkFields() {
        String nom = inputNom.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String telephone = inputTelephone.getText().toString().trim();

        boolean isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean isPhoneValid = telephone.matches("^[259]\\d{7}$");

        // Affichage dynamique des erreurs
        if (nom.isEmpty()) {
            inputNom.setError("Champ requis");
        } else {
            inputNom.setError(null);
        }

        if (email.isEmpty()) {
            inputEmail.setError("Champ requis");
        } else if (!isEmailValid) {
            inputEmail.setError("Email invalide");
        } else {
            inputEmail.setError(null);
        }

        if (telephone.isEmpty()) {
            inputTelephone.setError("Champ valise requis");
        } else if (!isPhoneValid) {
            inputTelephone.setError("Téléphone invalide (8 chiffres, doit commencer par 2, 5 ou 9)");
        }
        else if (!isPhoneValid) {
            inputTelephone.setError("Téléphone invalide (8 chiffres, doit commencer par 2, 5 ou 9)");
        }else {
            inputTelephone.setError(null);
        }

        boutonPaiement.setEnabled(!nom.isEmpty() && isEmailValid && isPhoneValid);
    }


}
