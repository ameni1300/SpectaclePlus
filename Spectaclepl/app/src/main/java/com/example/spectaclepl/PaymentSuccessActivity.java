package com.example.spectaclepl;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentSuccessActivity extends AppCompatActivity {

    private Button btnVoirTicket, btnRetourAccueil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        btnVoirTicket = findViewById(R.id.btnVoirTicket);
        btnRetourAccueil = findViewById(R.id.btnRetourAccueil);
        Intent intent = getIntent();
        String titre = intent.getStringExtra("titre");
        String date = intent.getStringExtra("date");
        String heure = intent.getStringExtra("heure");
        String lieu = intent.getStringExtra("lieu");
        int prix = intent.getIntExtra("total", 0);
        String nom = intent.getStringExtra("nom");
        String email = intent.getStringExtra("email");
        int ticket = intent.getIntExtra("tickets",0);
        String matricule = intent.getStringExtra("matricule");



        btnVoirTicket.setOnClickListener(v -> {
            // Redirige vers l'écran des tickets
            Intent bintent = new Intent(this, TicketActivity.class);
            bintent.putExtra("nom", nom);
            bintent.putExtra("email", email);
            bintent.putExtra("tickets", ticket);
            bintent.putExtra("total", prix);
            bintent.putExtra("titre", titre);
            bintent.putExtra("date", date);
            bintent.putExtra("heure", heure);
            bintent.putExtra("lieu", lieu);
            bintent.putExtra("matricule", matricule);


            startActivity(bintent);
        });

        btnRetourAccueil.setOnClickListener(v -> {
            // Retour à l'accueil
            Intent bintent = new Intent(this, MainActivity.class);
            bintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(bintent);
            finish();
        });
    }
}
