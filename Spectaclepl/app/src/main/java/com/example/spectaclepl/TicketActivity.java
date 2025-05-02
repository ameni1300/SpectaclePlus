package com.example.spectaclepl;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TicketActivity extends AppCompatActivity {

    private Button btnRetourAccueilTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        // Récupération des vues
        TextView titreSpectacle = findViewById(R.id.ticket_detailstitre);
        TextView heureSpectacle = findViewById(R.id.session_time);
        TextView lieuSpectacle = findViewById(R.id.theater_location);
        TextView dateSpectacle = findViewById(R.id.session_date);
        TextView nbrticket = findViewById(R.id.totalticket);
        TextView totalPrice = findViewById(R.id.total_price);

        // Récupération des données envoyées par l'intent
        Intent intent = getIntent();
        String titre = intent.getStringExtra("titre");
        String date = intent.getStringExtra("date");
        String heure = intent.getStringExtra("heure");
        String lieu = intent.getStringExtra("lieu");
        int prix = intent.getIntExtra("total", 0);
        int ticket = intent.getIntExtra("tickets", 0);

        // Affichage des données dans les TextViews
        titreSpectacle.setText("Spectacle: " + titre);
        nbrticket.setText(String.valueOf(ticket));
        totalPrice.setText((prix) + " DT");

        if (date != null && date.contains("T")) {
            String[] parts = date.split("T");
            dateSpectacle.setText("Date: " + parts[0]);
        } else {
            dateSpectacle.setText("Date: " + date);
        }

        heureSpectacle.setText("Session: " + heure);
        lieuSpectacle.setText("Lieu: " + lieu);

        // Bouton retour à l'accueil
        btnRetourAccueilTicket = findViewById(R.id.RetourAccueilTicket);
        btnRetourAccueilTicket.setOnClickListener(v -> {
            Intent bintent = new Intent(this, MainActivity.class);
            bintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(bintent);
            finish();
        });
    }
}
