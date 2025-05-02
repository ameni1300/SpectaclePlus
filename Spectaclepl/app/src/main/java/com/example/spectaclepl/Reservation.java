package com.example.spectaclepl;

public class Reservation {
    private String nom;
    private String email;
    private String telephone;
    private int tickets;
    private int prix_total;
    private String titre_spectacle;
    private String date_spectacle;
    private String heure_spectacle;
    private String lieu_spectacle;

    // Constructeur
    public Reservation(String nom, String email, String telephone, int tickets,
                       int prix_total, String titre_spectacle, String date_spectacle,
                       String heure_spectacle, String lieu_spectacle) {
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.tickets = tickets;
        this.prix_total = prix_total;
        this.titre_spectacle = titre_spectacle;
        this.date_spectacle = date_spectacle;
        this.heure_spectacle = heure_spectacle;
        this.lieu_spectacle = lieu_spectacle;
    }

    // Getters (nécessaires pour la sérialisation JSON)
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public int getTickets() { return tickets; }
    public int getPrix_total() { return prix_total; }
    public String getTitre_spectacle() { return titre_spectacle; }
    public String getDate_spectacle() { return date_spectacle; }
    public String getHeure_spectacle() { return heure_spectacle; }
    public String getLieu_spectacle() { return lieu_spectacle; }

    public void setNom(String nom) { this.nom = nom; }
    public void setEmail(String email) { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setTickets(int tickets) { this.tickets = tickets; }
    public void setPrix_total(int prix_total) { this.prix_total = prix_total; }
    public void setTitre_spectacle(String titre_spectacle) { this.titre_spectacle = titre_spectacle; }
    public void setDate_spectacle(String date_spectacle) { this.date_spectacle = date_spectacle; }
    public void setHeure_spectacle(String heure_spectacle) { this.heure_spectacle = heure_spectacle; }
    public void setLieu_spectacle(String lieu_spectacle) { this.lieu_spectacle = lieu_spectacle; }
}
