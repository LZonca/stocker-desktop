package lzonca.fr.stockerdesktop.system;

import java.util.prefs.Preferences;

public class LanguageManager {

    private static final String LANGUAGE = "APP_LOCALE";
    private static final Preferences prefs = Preferences.userNodeForPackage(LanguageManager.class);

    public static void setLanguage(String language) {
        prefs.put(LANGUAGE, language);
    }

    public static String getLanguage() {
        return prefs.get(LANGUAGE, null);
    }
}
