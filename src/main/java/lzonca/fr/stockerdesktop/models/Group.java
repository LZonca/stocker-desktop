package lzonca.fr.stockerdesktop.models;

import java.util.List;

public class Group {
    private int id;
    private String nom;
    private int proprietaire_id;
    private List<Stock> stocks;

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

    public int getProprietaire_id() {
        return proprietaire_id;
    }

    public void setProprietaire_id(int proprietaire_id) {
        this.proprietaire_id = proprietaire_id;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }
}