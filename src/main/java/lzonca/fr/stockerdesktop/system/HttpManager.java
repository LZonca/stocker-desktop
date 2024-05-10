package lzonca.fr.stockerdesktop.system;

import javafx.application.Platform;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.components.TokenExpiredDialog;
import lzonca.fr.stockerdesktop.models.User;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

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
    /*private final String baseUrl = "https://stocker.lzonca.fr/api";*/
    private final String baseUrl = "http://localhost:8000/api";
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
        /*System.out.println("Loading resource bundle for locale: " + locale);*/
        try {
            tokenLabels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.ErrorToken", locale);
            System.out.println("Successfully loaded resource bundle");
        } catch (MissingResourceException e) {
            System.out.println("Failed to load resource bundle: " + e.getMessage());
        }
    }

    public HttpResponse<String> deleteStock(int stockId) throws IOException, InterruptedException, URISyntaxException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(baseUrl + "/user/stocks/" + stockId))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept-Language", locale)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode() + response.body());
            if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409 && response.statusCode() != 204) {
                loadResourceBundle();
                if (response.statusCode() == 401) {
                    TokenManager.removeToken();
                    Platform.runLater(() -> {
                        TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                        dialog.showAndWait();
                    });
                }
            }
            return response;
        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HttpResponse<String> removeUserFromGroup(int groupId, User user) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/groups/" + groupId + "/users/" + user.getId()))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept-Language", locale)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.of(5, SECONDS))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409 && response.statusCode() != 403) {

            loadResourceBundle();
            if (response.statusCode() == 403) {
                Platform.runLater(() -> {
                    ErrorDialog dialog = new ErrorDialog(tokenLabels.getString("error"), tokenLabels.getString("cannotRemoveUserTitle"), tokenLabels.getString("cannotRemoveUserContent"), FontAwesomeSolid.EXCLAMATION_TRIANGLE);
                    dialog.showAndWait();
                });
            }
            if (response.statusCode() == 401) {
                TokenManager.removeToken();
                Platform.runLater(() -> {
                    TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                    dialog.showAndWait();
                });
            }
        }
        return response;
    }

    public HttpResponse<String> leaveGroup(int groupId) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/user/groups/" + groupId + "/leave"))
                .header("Authorization", "Bearer " + token)
                .header("Accept-Language", locale)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409 && response.statusCode() != 204) {

            loadResourceBundle();
            if (response.statusCode() == 401) {
                TokenManager.removeToken();
                Platform.runLater(() -> {
                    TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                    dialog.showAndWait();
                });
            }
        }
        return response;
    }

    public HttpResponse<String> updateProductQuantity(int stockId, int produitId, int quantite) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/user/stocks/" + stockId + "/produits/" + produitId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept-Language", locale)
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{\"quantite\":" + quantite + "}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409 && response.statusCode() != 204) {
            loadResourceBundle();
            if (response.statusCode() == 401) {
                TokenManager.removeToken();
                Platform.runLater(() -> {
                    TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                    dialog.showAndWait();
                });
            }
        }
        return response;
    }

    public HttpResponse<String> removeProduct(int stockId, int productId) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/user/stocks/" + stockId + "/produits/" + productId))
                .header("Authorization", "Bearer " + token)
                .header("Accept-Language", locale)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409 && response.statusCode() != 204) {
            System.out.println(response.body());
            loadResourceBundle();
            if (response.statusCode() == 401) {
                TokenManager.removeToken();
                Platform.runLater(() -> {
                    TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                    dialog.showAndWait();
                });
            }
        }
        return response;
    }

    public HttpResponse<String> createProduit(int stockId, String productName, String productCode, String productDesc/*,File productImage*/) throws IOException, InterruptedException, URISyntaxException {
        /*var multipartBuilder = new MultipartBodyPublisher();*/

        // Add JSON data
        String jsonData = "{\"nom\":\"" + productName + "\"";
        if (productCode != null && !productCode.isEmpty()) {
            jsonData += ",\"code\":\"" + productCode + "\"";
        }
        if (productDesc != null && !productDesc.isEmpty()) {
            jsonData += ",\"description\":\"" + productDesc + "\"";
        }
        jsonData += "}";
        System.out.println(jsonData);
        /*multipartBuilder.addPart("data", jsonData, "application/json");

        // Add image file
        if (productImage != null) {
            multipartBuilder.addPart("image", productImage.toPath(), "image/jpeg");
        }*/

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/user/stocks/" + stockId + "/produits"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept-Language", locale)
                .POST(HttpRequest.BodyPublishers.ofString(jsonData)/*multipartBuilder.build()*/)
                .build();
        System.out.println(request);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409) {
            loadResourceBundle();
            if (response.statusCode() == 401) {
                TokenManager.removeToken();
                Platform.runLater(() -> {
                    TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                    dialog.showAndWait();
                });
            }
        }
        return response;
    }

    public HttpResponse<String> deleteGroup(int groupId) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/groups/" + groupId))
                .header("Authorization", "Bearer " + token)
                .header("Accept-Language", locale)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409 && response.statusCode() != 204) {
            System.out.println(response.statusCode());
            loadResourceBundle();
            if (response.statusCode() == 401) {
                TokenManager.removeToken();
                Platform.runLater(() -> {
                    TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                    dialog.showAndWait();
                });
            }
        }
        return response;
    }

    public HttpResponse<String> getUserStocks() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/user/stocks"))
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

    public HttpResponse<String> createGroup(String name) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/user/groups"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept-Language", locale)
                .POST(HttpRequest.BodyPublishers.ofString("{\"nom\":\"" + name + "\"}"))
                .timeout(Duration.of(5, SECONDS))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409) {
            loadResourceBundle();
            if (response.statusCode() == 401) {
                TokenManager.removeToken();
                Platform.runLater(() -> {
                    TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                    dialog.showAndWait();
                });
            }

        }
        return response;
    }

    public HttpResponse<String> createUserStock(String name) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/user/stocks"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept-Language", locale)
                .POST(HttpRequest.BodyPublishers.ofString("{\"nom\":\"" + name + "\"}"))
                .timeout(Duration.of(5, SECONDS))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 409) {
            loadResourceBundle();
            if (response.statusCode() == 401) {
                TokenManager.removeToken();
                Platform.runLater(() -> {
                    TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                    dialog.showAndWait();
                });
            }

        }
        return response;
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
            loadResourceBundle();
            if (response.statusCode() == 401) {
                TokenManager.removeToken();
                Platform.runLater(() -> {
                    TokenExpiredDialog dialog = new TokenExpiredDialog(tokenLabels.getString("tokenExpiredTitle"), tokenLabels.getString("tokenExpiredHeader"), tokenLabels.getString("tokenExpiredContent"));
                    dialog.showAndWait();
                });
            }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}