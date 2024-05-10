package lzonca.fr.stockerdesktop.system;

import java.util.prefs.Preferences;

public class TokenManager {
    private static final String TOKEN_KEY = "BEARER_TOKEN";
    private static final Preferences prefs = Preferences.userNodeForPackage(TokenManager.class);

    public static void storeToken(String token) {
        prefs.put(TOKEN_KEY, token);
    }

    public static String getToken() {
        return prefs.get(TOKEN_KEY, null);
    }

    public static boolean hasToken() {
        return getToken() != null;
    }

    public static void removeToken() {
        prefs.remove(TOKEN_KEY);
    }
}