package lzonca.fr.stockerdesktop.views;

import lzonca.fr.stockerdesktop.models.Stock;
import lzonca.fr.stockerdesktop.models.User;

import java.util.Collection;

public class StocksView {
    private User user;


    public void setUser(User user) {
        this.user = user;
    }

    public Collection<Stock> getStocks() {
        return null;
    }
}
