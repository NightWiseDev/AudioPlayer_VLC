package org.example.platforms;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.regex.*;
import com.google.gson.*;

public class SoundCloudDownloader {

    private final String clientId = "Ищите сами ахуели что ли?";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    public boolean isValidUrl(String url) {
        return url != null && url.matches("^https?://(www\\.)?soundcloud\\.com/[^/]+(/[^/?#]+)?");
    }

    public String getHtml(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String extractTitle(String html) {
        // Try to get title from JSON-LD data first
        Pattern jsonLdPattern = Pattern.compile("<script type=\"application/ld\\+json\">(.*?)</script>", Pattern.DOTALL);
        Matcher jsonLdMatcher = jsonLdPattern.matcher(html);
        if (jsonLdMatcher.find()) {
            try {
                JsonObject jsonObject = JsonParser.parseString(jsonLdMatcher.group(1)).getAsJsonObject();
                if (jsonObject.has("name")) {
                    return cleanTitle(jsonObject.get("name").getAsString());
                }
            } catch (JsonSyntaxException e) {
                // Fall through to next method
            }
        }
        Pattern metaPattern = Pattern.compile("<meta property=\"og:title\" content=\"([^\"]+)\"");
        Matcher metaMatcher = metaPattern.matcher(html);
        if (metaMatcher.find()) {
            return cleanTitle(metaMatcher.group(1));
        }
        Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");
        Matcher titleMatcher = titlePattern.matcher(html);
        if (titleMatcher.find()) {
            return cleanTitle(titleMatcher.group(1).replaceAll("\\|.*", "").trim());
        }

        return null;
    }

    public String extractSongId(String html) {
        Pattern jsonLdPattern = Pattern.compile("<script type=\"application/ld\\+json\">(.*?)</script>", Pattern.DOTALL);
        Matcher jsonLdMatcher = jsonLdPattern.matcher(html);
        if (jsonLdMatcher.find()) {
            try {
                JsonObject jsonObject = JsonParser.parseString(jsonLdMatcher.group(1)).getAsJsonObject();
                if (jsonObject.has("@id")) {
                    String url = jsonObject.get("@id").getAsString();
                    Pattern idPattern = Pattern.compile("tracks/(\\d+)");
                    Matcher idMatcher = idPattern.matcher(url);
                    if (idMatcher.find()) {
                        return idMatcher.group(1);
                    }
                }
            } catch (JsonSyntaxException e) {
                System.out.println("Пизда короче... -> " + e.getMessage());
            }
        }

        Pattern scriptPattern = Pattern.compile("\"soundcloud://sounds:(\\d+)\"");
        Matcher scriptMatcher = scriptPattern.matcher(html);
        if (scriptMatcher.find()) {
            return scriptMatcher.group(1);
        }

        Pattern urlPattern = Pattern.compile("\"url\":\"https://api-v2\\.soundcloud\\.com/tracks/(\\d+)\"");
        Matcher urlMatcher = urlPattern.matcher(html);
        if (urlMatcher.find()) {
            return urlMatcher.group(1);
        }

        return null;
    }

    public String getStreamUrl(String songId) throws IOException, InterruptedException {
        String apiUrl = "https://api-v2.soundcloud.com/tracks/" + songId + "?client_id=" + clientId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        if (jsonObject.has("media")) {
            JsonObject media = jsonObject.getAsJsonObject("media");
            if (media.has("transcodings")) {
                JsonArray transcodings = media.getAsJsonArray("transcodings");
                for (JsonElement element : transcodings) {
                    JsonObject transcoding = element.getAsJsonObject();
                    if (transcoding.has("format") &&
                            transcoding.getAsJsonObject("format").get("protocol").getAsString().equals("progressive")) {
                        String streamUrl = transcoding.get("url").getAsString();
                        return streamUrl + "?client_id=" + clientId;
                    }
                }
            }
        }

        throw new IOException("Could not find stream URL in API response");
    }

    public String resolveFinalUrl(String streamUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(streamUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        if (jsonObject.has("url")) {
            return jsonObject.get("url").getAsString();
        }

        throw new IOException("Could not resolve final stream URL");
    }

    public void downloadFile(String fileUrl, String outputFilename) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fileUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (InputStream in = response.body();
             OutputStream out = new FileOutputStream(outputFilename)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private String cleanTitle(String title) {
        return title.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    public void download(String url, String outputDir) throws IOException, InterruptedException {
        if (!isValidUrl(url)) {
            throw new IllegalArgumentException("Invalid SoundCloud URL");
        }

        String html = getHtml(url);
        String title = extractTitle(html);
        if (title == null) throw new IOException("Cannot extract title");

        String songId = extractSongId(html);
        if (songId == null) throw new IOException("Cannot extract song ID");

        String streamUrl = getStreamUrl(songId);
        String finalUrl = resolveFinalUrl(streamUrl);

        Path outputPath = Path.of(outputDir, title + ".mp3");
        System.out.println("Downloading '" + title + "' to " + outputPath.toAbsolutePath());

        downloadFile(finalUrl, outputPath.toString());

        System.out.println("Download finished.");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java SoundCloudDownloader <soundcloud_url> <output_directory>");
            return;
        }

        SoundCloudDownloader downloader = new SoundCloudDownloader();
        try {
            downloader.download(args[0], args[1]);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}