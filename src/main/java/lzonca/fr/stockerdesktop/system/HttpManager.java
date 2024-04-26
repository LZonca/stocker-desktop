package lzonca.fr.stockerdesktop.system;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class HttpManager {
    private final String baseUrl = "https://stocker.lzonca.fr/api";
    /*private final String baseUrl = "http://localhost:8000/api";*/
    private final String token = TokenManager.getToken();

    private final HttpClient client;

    public HttpManager() {
        client = HttpClient.newHttpClient();
    }
    public HttpResponse<String> getUser() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/user"))
                .header("Authorization", "Bearer " + TokenManager.getToken())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status code: " + response.statusCode());
        System.out.println("Headers: " + response.headers());

        if (response.statusCode() == 401) {
            TokenManager.removeToken();
            throw new IOException("Token is expired or invalid");
        }

        return response;
    }

    public HttpResponse<String> addUserToGroup(int groupId, String email) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/groups/" + groupId + "/add"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + email + "\"}"))
                .timeout(Duration.of(5, SECONDS))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new IOException("Token is expired or invalid");
        }

        return response;
    }

    public HttpResponse<String> getUserGroups() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/user/groups"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> get(String url) throws URISyntaxException, IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + url))
                .version(HttpClient.Version.HTTP_2)
                .setHeader("Authorization", "Bearer " + token)
                .timeout(Duration.of(60, SECONDS))
                .GET()
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
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

    public HttpResponse<String> put(String url, String json) throws URISyntaxException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + url))
                .version(HttpClient.Version.HTTP_2)
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer " + token)
                .timeout(Duration.of(60, SECONDS))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public HttpResponse<String> patch(String url, String json) throws URISyntaxException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + url))
                .version(HttpClient.Version.HTTP_2)
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer " + token)
                .timeout(Duration.of(60, SECONDS))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public HttpResponse<String> delete(String url, String json) throws URISyntaxException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + url))
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer " + token)
                .timeout(Duration.of(60, SECONDS))
                .version(HttpClient.Version.HTTP_2)
                .DELETE()
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}