package manga.reader.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Service
public class ImageDownloadService {

    private final RestTemplate restTemplate;

    public ImageDownloadService() {
        this.restTemplate = new RestTemplate();
        // Set timeouts if needed
        // SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // requestFactory.setConnectTimeout(Duration.ofSeconds(2).toMillis());
        // requestFactory.setReadTimeout(Duration.ofSeconds(2).toMillis());
        // this.restTemplate = new RestTemplate(requestFactory);
    }

    /**
     * Downloads an image from the given URL
     *
     * @param imageUrl URL of the image to download
     * @param referer Referer header to use in the request
     * @return Resource containing the image data
     * @throws IOException If the image cannot be downloaded
     */
    public Resource downloadImage(String imageUrl, String referer) throws IOException {
        try {
            // Set up headers similar to the Python version
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "image/webp,image/apng,*/*;q=0.8");
            headers.set("Referer", referer);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the request
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    imageUrl,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            // Check if we got a successful response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return new ByteArrayResource(response.getBody());
            } else {
                throw new IOException("Failed to download image, status code: " + response.getStatusCodeValue());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IOException("HTTP error while downloading image: " + e.getStatusCode() + " - " + e.getStatusText());
        } catch (ResourceAccessException e) {
            throw new IOException("Network error while downloading image: " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("Unexpected error while downloading image: " + e.getMessage());
        }
    }

    /**
     * Cache-aware image download that can handle HTTP 304 responses
     * This would require implementing caching logic which is beyond the scope of a simple example
     */
    public Resource downloadImageWithCaching(String imageUrl, String referer, String eTag, String lastModified) throws IOException {
        // Implementation would handle 304 responses and caching
        // This is left as a future enhancement
        return downloadImage(imageUrl, referer);
    }
}