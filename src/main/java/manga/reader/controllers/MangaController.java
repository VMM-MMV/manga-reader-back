package manga.reader.controllers;

import manga.reader.exception.BadRequestException;
import manga.reader.exception.ResourceNotFoundException;
import manga.reader.services.MangakakalotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/mangas")
public class MangaController {

    private final MangakakalotService mangakakalotService;

    public MangaController(MangakakalotService mangakakalotService) {
        this.mangakakalotService = mangakakalotService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getLatestManga(
            @RequestParam(value = "page", defaultValue = "1") int page) throws IOException {
        if (page < 1) {
            throw new BadRequestException("Page number must be greater than 0");
        }
        Map<String, Object> result = mangakakalotService.latestManga(page);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchManga(
            @RequestParam(value = "query") String query,
            @RequestParam(value = "page", defaultValue = "1") int page) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Search query cannot be empty");
        }
        if (page < 1) {
            throw new BadRequestException("Page number must be greater than 0");
        }
        
        Map<String, Object> result = mangakakalotService.search(query, page);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{mangaId}")
    public ResponseEntity<Map<String, Object>> getMangaInfo(@PathVariable String mangaId) throws IOException {
        if (mangaId == null || mangaId.trim().isEmpty()) {
            throw new BadRequestException("Manga ID cannot be empty");
        }
        
        Map<String, Object> result = mangakakalotService.mangaInfo(mangaId);
        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Manga not found with ID: " + mangaId);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chapter/{mangaId}/{chapterId}")
    public ResponseEntity<Map<String, Object>> getChapter(
            @PathVariable String mangaId,
            @PathVariable String chapterId) throws IOException {
        if (mangaId == null || mangaId.trim().isEmpty()) {
            throw new BadRequestException("Manga ID cannot be empty");
        }
        if (chapterId == null || chapterId.trim().isEmpty()) {
            throw new BadRequestException("Chapter ID cannot be empty");
        }
        
        Map<String, Object> result = mangakakalotService.fetchChapter(mangaId + "/" + chapterId);
        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Chapter not found for manga ID: " + mangaId + " and chapter ID: " + chapterId);
        }
        return ResponseEntity.ok(result);
    }
}