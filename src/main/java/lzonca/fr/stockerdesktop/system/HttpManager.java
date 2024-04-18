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

    public HttpResponse<String> getUser() throws URISyntaxException {
        System.out.println("Token: " + TokenManager.getToken());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/user"))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
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