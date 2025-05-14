package manga.reader.controllers;

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

    /**
     * Get the latest manga releases
     *
     * @param page Page number (defaults to 1)
     * @return JSON response with latest manga
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getLatestManga(
            @RequestParam(value = "page", defaultValue = "1") int page) {
        try {
            Map<String, Object> result = mangakakalotService.latestManga(page);
            System.out.println(result);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch latest manga: " + e.getMessage()));
        }
    }

    /**
     * Search for manga
     *
     * @param query Search query
     * @param page Page number (defaults to 1)
     * @return JSON response with search results
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchManga(
            @RequestParam(value = "query") String query,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        try {
            Map<String, Object> result = mangakakalotService.search(query, page);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to search manga: " + e.getMessage()));
        }
    }

    /**
     * Get detailed information about a manga
     *
     * @param mangaId Manga ID
     * @return JSON response with manga details
     */
    @GetMapping("/{mangaId}")
    public ResponseEntity<Map<String, Object>> getMangaInfo(@PathVariable String mangaId) {
        try {
            Map<String, Object> result = mangakakalotService.mangaInfo(mangaId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch manga info: " + e.getMessage()));
        }
    }

    /**
     * Fetch a specific chapter of a manga
     *
//     * @param chapterPath Chapter path
     * @return JSON response with chapter data
     */
    @GetMapping("/chapter/{mangaId}/{chapterId}")
    public ResponseEntity<Map<String, Object>> getChapter(
            @PathVariable String mangaId,
            @PathVariable(required = false) String chapterId) {
        try {
            Map<String, Object> result = mangakakalotService.fetchChapter(mangaId+"/"+chapterId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch chapter: " + e.getMessage()));
        }
    }
}
