package com.example.spectaclepl;


public class Spectacle {
    private String titre;
    private String date;
    private String heureDebut;
    private String lieu;
    private String localisation;
    private String nomArtiste;
    private int place_dispo;
    private String  image;
    private String description;
    private String prixTicket;
    private String matricule;
    private String categorie;

    public Spectacle(String titre, String date, String nomArtiste, String heureDebut, String lieu, int place_dispo, String localisation, String image, String description, String prixTicket, String matricule, String categorie) {
        this.titre = titre;
        this.date = date;
        this.heureDebut = heureDebut;
        this.lieu = lieu;
        this.place_dispo = place_dispo;
        this.localisation = localisation;
        this.image = image;
        this.nomArtiste = nomArtiste;
        this.description = description;
        this.prixTicket = prixTicket;
        this.matricule = matricule;
        this.categorie = categorie;
    }

    public String getTitre() {
        return titre;
    }

    public String getDate() {
        return date;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public String getLieu() {
        return lieu;
    }

    public String getlocalisation() {
        return localisation;
    }

    public String getNomArtiste() {
        return nomArtiste;
    }
    public String getPrixTicket() {
        return prixTicket;
    }
    public String  getimageResId() {
        return image;
    }
    public String  getDescription() {
        return description;
    }
    public int getplacedispo() {
        return place_dispo;
    }
    public void setImageResId(String image) {
        this.image = image;
    }
    public String getMatricule() {
        return matricule;
    }
    public String getCategorie() {
        return categorie;
    }

}
