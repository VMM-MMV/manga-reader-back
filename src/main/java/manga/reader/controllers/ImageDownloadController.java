package manga.reader.controllers;

import manga.reader.services.ImageDownloadService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageDownloadController {

    private final ImageDownloadService imageDownloadService;

    public ImageDownloadController(ImageDownloadService imageDownloadService) {
        this.imageDownloadService = imageDownloadService;
    }

    /**
     * Download an image from a given URL and return it as a resource
     *
     * @param imageUrl URL of the image to download
     * @param referer Optional referer header (defaults to "https://www.mangakakalot.gg/")
     * @return The image as a resource or an error response
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadImage(
            @RequestParam("url") String imageUrl,
            @RequestParam(value = "referer", defaultValue = "https://www.mangakakalot.gg/") String referer) {

        try {
            Resource imageResource = imageDownloadService.downloadImage(imageUrl, referer);

            // Determine content type based on image extension
            String contentType = determineContentType(imageUrl);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + extractFilename(imageUrl) + "\"")
                    .body(imageResource);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to download image: " + e.getMessage()));
        }
    }

    /**
     * Determines the content type based on the image URL extension
     */
    private String determineContentType(String imageUrl) {
        if (imageUrl.endsWith(".webp")) {
            return "image/webp";
        } else if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (imageUrl.endsWith(".png")) {
            return "image/png";
        } else if (imageUrl.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * Extracts the filename from the URL
     */
    private String extractFilename(String imageUrl) {
        int lastSlashIndex = imageUrl.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < imageUrl.length() - 1) {
            return imageUrl.substring(lastSlashIndex + 1);
        }
        return "image";
    }
}