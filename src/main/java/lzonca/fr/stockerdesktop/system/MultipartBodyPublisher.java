package lzonca.fr.stockerdesktop.system;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MultipartBodyPublisher {
    private final List<Part> parts = new ArrayList<>();
    private String boundary;

    public void addPart(String name, String value, String contentType) {
        parts.add(new Part(name, value.getBytes(), contentType));
    }

    public void addPart(String name, Path filePath, String contentType) throws IOException {
        byte[] value = Files.readAllBytes(filePath);
        parts.add(new Part(name, value, contentType));
    }

    public HttpRequest.BodyPublisher build() {
        if (parts.isEmpty()) {
            throw new IllegalStateException("Must have at least one part to build multipart message.");
        }
        boundary = UUID.randomUUID().toString();
        var byteArrays = new ArrayList<byte[]>();
        for (var part : parts) {
            var header = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + part.name + "\"\r\nContent-Type: " + part.contentType + "\r\n\r\n";
            byteArrays.add(header.getBytes());
            byteArrays.add(part.value);
            byteArrays.add("\r\n".getBytes());
        }
        byteArrays.add(("--" + boundary + "--").getBytes());
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    public String getContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    private static class Part {
        String name;
        byte[] value;
        String contentType;

        public Part(String name, byte[] value, String contentType) {
            this.name = name;
            this.value = value;
            this.contentType = contentType;
        }
    }
}