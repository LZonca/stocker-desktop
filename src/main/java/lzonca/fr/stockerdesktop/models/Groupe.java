package lzonca.fr.stockerdesktop.models;

import java.util.ArrayList;
import java.util.List;

public class Groupe {
    private int id;
    private String nom;
    private String image;
    private User proprietaire; // Updated from int to User
    private List<Stock> stocks;
    private List<User> members = new ArrayList<>();

    // getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public User getProprietaire() { // Updated return type to User
        return proprietaire;
    }

    public void setProprietaire(User proprietaire) { // Updated parameter type to User
        this.proprietaire = proprietaire;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public List<User> getMembers() {
        return members;
    }
}