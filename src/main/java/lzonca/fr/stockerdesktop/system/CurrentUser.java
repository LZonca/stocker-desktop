package lzonca.fr.stockerdesktop.system;

import lzonca.fr.stockerdesktop.models.User;

public class CurrentUser {
    private static CurrentUser instance;
    private User user;

    private CurrentUser() {
    }

    public static CurrentUser getInstance() {
        if (instance == null) {
            instance = new CurrentUser();
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}