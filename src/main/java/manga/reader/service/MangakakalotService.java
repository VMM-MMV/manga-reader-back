package manga.reader.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MangakakalotService {
    private final String url;

    public MangakakalotService() {
        this.url = "https://www.mangakakalot.gg";
    }

    /**
     * Get the latest manga releases
     * @param page Page number
     * @return Map containing results
     * @throws IOException if connection fails
     */
    public Map<String, Object> latestManga(int page) throws IOException {
        Document doc = Jsoup.connect(String.format("%s/manga-list/latest-manga?page=%d", this.url, page)).get();
        List<Map<String, Object>> results = new ArrayList<>();

        Elements mangaItems = doc.select(".truyen-list .list-truyen-item-wrap");
        for (Element item : mangaItems) {
            Map<String, Object> manga = new HashMap<>();

            String mangaId = item.select("a").first().attr("href").replace("/manga/", "").replace(this.url, "");
            String img = item.select("a img").first().attr("data-src");
            String title = item.select("a").first().attr("title");

            Element latestChapterTag = item.select("a").size() > 2 ? item.select("a").get(2) : null;
            String latestChapter = latestChapterTag != null && latestChapterTag.hasAttr("title") ?
                    latestChapterTag.attr("title") : "N/A";
            String chapterId = latestChapterTag != null && latestChapterTag.hasAttr("href") ?
                    latestChapterTag.attr("href").replace("/chapter/", "") : "N/A";

            String view = item.select(".aye_icon").text().trim();
            Element descriptionTag = item.select("p").first();
            String description = descriptionTag != null ? descriptionTag.text().trim() : "N/A";

            manga.put("mangaID", mangaId);
            manga.put("img", img);
            manga.put("title", title);
            manga.put("latestChapter", latestChapter);
            manga.put("chapterID", chapterId);
            manga.put("view", view);
            manga.put("description", description);

            results.add(manga);
        }

        // Get pagination info
        String currentPage = doc.select(".panel_page_number .group_page .page_select").text().trim();
        String totalPageHref = doc.select(".panel_page_number .group_page .page_last").last().attr("href");
        Pattern pagePattern = Pattern.compile("page=(\\d+)");
        Matcher pageMatcher = pagePattern.matcher(totalPageHref);
        String pageNumber = pageMatcher.find() ? pageMatcher.group(1) : "1";

        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("page", currentPage);
        pageInfo.put("totalPage", pageNumber);
        results.add(pageInfo);

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        return response;
    }

    /**
     * Search for manga
     * @param query Search query
     * @param page Page number
     * @return Map containing results
     * @throws IOException if connection fails
     */
    public Map<String, Object> search(String query, int page) throws IOException {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("Missing query!");
        }

        page = Math.max(page, 1);
        Document doc = Jsoup.connect(String.format("%s/search/story/%s?page=%d", this.url, query, page)).get();

        List<Map<String, Object>> mangaList = new ArrayList<>();
        Elements items = doc.select(".daily-update .panel_story_list .story_item");

        for (Element item : items) {
            Map<String, Object> mangaInfo = new HashMap<>();

            String fullUrl = item.select("a").first().attr("href");
            String mangaId = fullUrl.split("/manga/")[1];
            String thumbnail = item.select("a img").first().attr("src");
            String title = item.select(".story_name a").text().trim();

            String author = item.select(".story_item_right span:contains(Author)").text().replace("Author(s) : ", "");
            String update = item.select(".story_item_right span:contains(Updated)").text().replace("Updated : ", "");
            String view = item.select(".story_item_right span:contains(View)").text().replace("View : ", "");

            mangaInfo.put("id", mangaId);
            mangaInfo.put("img", thumbnail);
            mangaInfo.put("title", title);
            mangaInfo.put("author", author);
            mangaInfo.put("update", update);
            mangaInfo.put("view", view);

            mangaList.add(mangaInfo);
        }

        // Get pagination info
        String currentPage = doc.select(".panel_page_number .group_page .page_select").text().trim();
        String totalPageHref = doc.select(".panel_page_number .group_page .page_last").last().attr("href");
        Pattern pagePattern = Pattern.compile("page=(\\d+)");
        Matcher pageMatcher = pagePattern.matcher(totalPageHref);
        String pageNumber = pageMatcher.find() ? pageMatcher.group(1) : "1";

        List<Map<String, Object>> pages = new ArrayList<>();
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("page", currentPage);
        pageInfo.put("totalPage", pageNumber);
        pageInfo.put("searchKey", query);
        pages.add(pageInfo);

        Map<String, Object> response = new HashMap<>();
        response.put("results", mangaList);
        response.put("pages", pages);

        return response;
    }

    /**
     * Get detailed information about a manga
     * @param mangaId Manga ID
     * @return Map containing results
     * @throws IOException if connection fails
     */
    public Map<String, Object> mangaInfo(String mangaId) throws IOException {
        if (mangaId == null || mangaId.isEmpty()) {
            throw new IllegalArgumentException("Missing id!");
        }

        Document doc = Jsoup.connect(String.format("%s/manga/%s", this.url, mangaId)).get();

        Map<String, Object> mangaData = new HashMap<>();

        String thumbnail = doc.select(".manga-info-top .manga-info-pic img").attr("src");
        String title = doc.select(".manga-info-top .manga-info-text li:nth-of-type(1) h1").text().trim();

        List<String> authors = new ArrayList<>();
        Elements authorElements = doc.select(".manga-info-top .manga-info-text li:contains(Author) a");
        for (Element author : authorElements) {
            authors.add(author.text().trim());
        }

        String status = doc.select(".manga-info-top .manga-info-text li:nth-of-type(2)").text().replace("Status : ", "");
        String lastUpdate = doc.select(".manga-info-top .manga-info-text li:nth-of-type(3)").text().replace("Last updated : ", "");
        String view = doc.select(".manga-info-top .manga-info-text li:nth-of-type(5)").text().replace("View : ", "");

        List<String> genres = new ArrayList<>();
        Elements genreElements = doc.select(".manga-info-top .manga-info-text li:contains(Genres) a");
        for (Element genre : genreElements) {
            genres.add(genre.text().trim());
        }

        // Parse rating
        Map<String, Object> rating = new HashMap<>();
        rating.put("score", 0.0);
        rating.put("outOf", 5);
        rating.put("votes", 0);

        Element jsonLdTag = doc.select("script[type=application/ld+json]").first();
        if (jsonLdTag != null) {
            try {
                JSONObject ratingData = new JSONObject(jsonLdTag.data());
                rating.put("score", ratingData.optDouble("ratingValue", 0.0));
                rating.put("votes", ratingData.optInt("ratingCount", 0));
            } catch (JSONException e) {
                // Ignore parsing errors
            }
        }

        String summary = doc.select("#contentBox").text().trim();

        List<Map<String, Object>> chapters = new ArrayList<>();
        Elements rows = doc.select(".chapter .manga-info-chapter .chapter-list .row");
        for (Element row : rows) {
            Map<String, Object> chapter = new HashMap<>();

            String chapterName = row.select("a").text().trim();
            String chapterIdUrl = row.select("a").attr("href");
            String chapterId = chapterIdUrl.split("/manga/")[1];
            String views = row.select("span:nth-of-type(2)").text().trim();
            String timeUploaded = row.select("span:nth-of-type(3)").text().trim();

            chapter.put("chapterName", chapterName);
            chapter.put("chapterID", chapterId);
            chapter.put("views", views);
            chapter.put("timeUploaded", timeUploaded);

            chapters.add(chapter);
        }

        mangaData.put("img", thumbnail);
        mangaData.put("title", title);
        mangaData.put("authors", authors);
        mangaData.put("status", status);
        mangaData.put("lastUpdate", lastUpdate);
        mangaData.put("view", view);
        mangaData.put("genres", genres);
        mangaData.put("rating", rating);
        mangaData.put("summary", summary);
        mangaData.put("chapters", chapters);

        Map<String, Object> response = new HashMap<>();
        response.put("results", mangaData);

        return response;
    }

    /**
     * Fetch a specific chapter of a manga
     * @param chapterPath Chapter path
     * @return Map containing results
     * @throws IOException if connection fails
     */
    public Map<String, Object> fetchChapter(String chapterPath) throws IOException {
        String[] parts = chapterPath.replace("chapter-", "").split("/");
        String mangaId = parts[0];
        String chapterId = parts.length > 1 ? parts[1] : "";

        String fullUrl = String.format("%s/manga/%s", this.url, chapterPath);
        Document doc = Jsoup.connect(fullUrl).get();

        String title = "";
        Elements headings = doc.select("h1, h2");
        for (Element heading : headings) {
            if (heading.text().contains(mangaId)) {
                title = heading.text().trim();
                break;
            }
        }

        List<String> primary = new ArrayList<>();
        List<String> secondary = new ArrayList<>();

        Elements images = doc.select(".container-chapter-reader img");
        for (Element img : images) {
            String primarySrc = img.attr("src");
            String fallbackSrc = null;

            String onerror = img.attr("onerror");
            if (!onerror.isEmpty()) {
                Pattern pattern = Pattern.compile("this\\.src='([^']+)'");
                Matcher matcher = pattern.matcher(onerror);
                if (matcher.find()) {
                    fallbackSrc = matcher.group(1);
                }
            }

            if (primarySrc != null && !primarySrc.isEmpty() && !primary.contains(primarySrc)) {
                primary.add(primarySrc);
            }

            if (fallbackSrc != null && !secondary.contains(fallbackSrc)) {
                secondary.add(fallbackSrc);
            }
        }

        List<String> chapters = new ArrayList<>();
        Elements chapterElements = doc.select("select option, .chapter-selection a");

        for (Element element : chapterElements) {
            String chapterText = element.text().trim();
            if (chapterText.toLowerCase().contains("chapter")) {
                Pattern pattern = Pattern.compile("Chapter\\s+(\\d+(\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(chapterText);
                if (matcher.find()) {
                    String chapterNum = matcher.group(1);
                    if (!chapters.contains(chapterNum)) {
                        chapters.add(chapterNum);
                    }
                }
            }
        }

        // Sort chapters in descending order
        Collections.sort(chapters, (a, b) -> Float.compare(Float.parseFloat(b), Float.parseFloat(a)));

        Map<String, Object> chapterData = new HashMap<>();
        chapterData.put("title", title);
        chapterData.put("primary_imgs", primary);
        chapterData.put("secondary_imgs", secondary);
        chapterData.put("chapters", chapters);
        chapterData.put("currentChapter", chapterId);

        Map<String, Object> response = new HashMap<>();
        response.put("results", chapterData);

        return response;
    }

    public static void main(String[] args) {
        MangakakalotService mangakakalot = new MangakakalotService();

        try {
            Map<String, Object> res = mangakakalot.fetchChapter("a-high-school-boy-reincarnates-as-the-villainous-daughter-in-an-otome-game/chapter-3");
            System.out.println(new JSONObject(res).toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
