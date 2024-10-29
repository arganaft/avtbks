package avtobuks.addSite;

import avtobuks.db.botSettings.BotSettings;
import avtobuks.db.botSettings.BotSettingsService;
import avtobuks.db.cookieSites.CookieSites;
import avtobuks.db.cookieSites.CookieSitesService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@Service
public class SiteManager {
    CookieSitesService cookieSitesService;
    ArrayList <CookieSites> cookieSites;
    BotSettingsService settings;


    public SiteManager(CookieSitesService cookieSitesService, BotSettingsService botSettingsService) {
        this.cookieSitesService = cookieSitesService;
        this.settings = botSettingsService;
        cookieSites = cookieSitesService.getCookieSitesOrderedByLiRankDesc();
    }


    @Retryable(
            retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000))
    @GetMapping("/get-site")
    @Async
    public CompletableFuture<ResponseEntity<String>> getsite() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("sites", getcookieSites(settings.get().getGoogleSitesCount(), settings.get().getYandexSitesCount()));
            return  CompletableFuture.completedFuture(ResponseEntity.ok(jsonObj.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(ResponseEntity.status(500).body("Произошла ошибка"));
        }
    }

    @Retryable(
            retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000))
    @PostMapping("/add-site")
    @Async
    public CompletableFuture<ResponseEntity<String>> addsite(@RequestBody String json) {
        try {
            JSONObject jsonObj = new JSONObject(json);
            return CompletableFuture.completedFuture(ResponseEntity.ok(addSite(jsonObj)));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(ResponseEntity.status(500).body("Произошла ошибка"));
        }
    }

    @Transactional
    private String addSite(JSONObject jsonObj) {
        String liRankString = jsonObj.getString("li_rank");
        String liRankWithoutSpaces = liRankString.replaceAll("\\s+", "");
        Long liRank = Long.parseLong(liRankWithoutSpaces);
        if (!cookieSitesService.existsCookieSite(jsonObj.getString("url"))) {
            CookieSites site = new CookieSites(jsonObj.getString("url"),
                    liRank,
                    Boolean.valueOf(jsonObj.getString("google")),
                    Boolean.valueOf(jsonObj.getString("yandex")),
                    0L);
            cookieSitesService.addSite(site);
            return "Site added";
        }
        return "Site is exists";
    }

    @Transactional
    public Set<String> getcookieSites(int googleCount, int yandexCount) {
        Set<String> sites = new HashSet<>();
        Random random = new Random();
        int google = googleCount;
        int yandex = yandexCount;
        boolean isAdded = false;
        long startTime = System.currentTimeMillis();
        while (google > 0 || yandex > 0) {
            CookieSites site = cookieSites.get(random.nextInt(random.nextInt(1, random.nextInt(2, cookieSites.size()))));
            String url = site.getUrl();
            if (google > 0 && site.getGoogle() && !sites.contains(url)) {
                sites.add(url);
                google--;
                isAdded = true;
            }
            if (yandex > 0 && site.getYandex() && (isAdded || !sites.contains(url))) {
                if (!isAdded) {
                    sites.add(url);
                }
                yandex--;
            }
            isAdded = false;
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Время создания списка сайтов: " + duration + " миллисекунд");
        return sites;
    }

}
