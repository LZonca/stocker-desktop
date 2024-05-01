package lzonca.fr.stockerdesktop.system;

import javafx.application.Platform;
import lzonca.fr.stockerdesktop.components.TokenExpiredDialog;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static java.time.temporal.ChronoUnit.SECONDS;

public class HttpManager {
    private final String baseUrl = "https://stocker.lzonca.fr/api";
    /*private final String baseUrl = "http://localhost:8000/api";*/
    private final String token = TokenManager.getToken();

    private ResourceBundle tokenLabels;
    private final String locale = LanguageManager.getLanguage();

    private final HttpClient client;

    public HttpManager() {
        client = HttpClient.newHttpClient();
    }
    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        System.out.println("Loading resource bundle for locale: " + locale);
        try {
            tokenLabels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.ErrorToken", locale);
            System.out.println("Successfully loaded resource bundle");
        } catch (MissingResourceException e) {
            System.out.println("Failed to load resource bundle: " + e.getMessage());
        }
    }

    public HttpResponse<String> getUser() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/user"))
                .header("Authorization", "Bearer " + TokenManager.getToken())
                .header("Accept-Language", locale)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status code: " + response.statusCode());
        System.out.println("Headers: " + response.headers());

        if (response.statusCode() == 401) {
            System.out.println("Token expired");
            TokenManager.removeToken();
            loadResourceBundle();

        }

        return response;
    }

    public HttpResponse<String> addUserToGroup(int groupId, String email) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/groups/" + groupId + "/add"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept-Language", locale)
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + email + "\"}"))
                .timeout(Duration.of(5, SECONDS))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409) {

            TokenManager.removeToken();
            loadResourceBundle();
            Platform.runLater(() -> {
                TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                dialog.showAndWait();
            });
        }
        return response;
    }

    public HttpResponse<String> getUserGroups() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/user/groups"))
                .header("Authorization", "Bearer " + token)
                .header("Accept-Language", locale)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200) {

            TokenManager.removeToken();
            loadResourceBundle();
            Platform.runLater(() -> {
                TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                dialog.showAndWait();
            });
        }
        return response;
    }

    public HttpResponse<String> post(String url, String json) throws URISyntaxException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + url))
                .version(HttpClient.Version.HTTP_2)
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer " + token)
                .timeout(Duration.of(60, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}