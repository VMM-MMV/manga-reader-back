package manga.reader.controllers;

import manga.reader.exception.BadRequestException;
import manga.reader.services.ImageDownloadService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageDownloadController {

    private final ImageDownloadService imageDownloadService;

    public ImageDownloadController(ImageDownloadService imageDownloadService) {
        this.imageDownloadService = imageDownloadService;
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadImage(
            @RequestParam("url") String imageUrl,
            @RequestParam(value = "referer", defaultValue = "https://www.mangakakalot.gg/") String referer) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new BadRequestException("Image URL cannot be empty");
        }

        Resource imageResource = imageDownloadService.downloadImage(imageUrl, referer);
        String contentType = determineContentType(imageUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + extractFilename(imageUrl) + "\"")
                .body(imageResource);
    }

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

    private String extractFilename(String imageUrl) {
        int lastSlashIndex = imageUrl.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < imageUrl.length() - 1) {
            return imageUrl.substring(lastSlashIndex + 1);
        }
        return "image";
    }
}