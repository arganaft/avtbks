package avtobuks.gmail_bot;

import avtobuks.db.profile.Profile;
import avtobuks.db.profile.ProfileService;
import avtobuks.db.proxy.Proxy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Service
public class GmailBot {
    private Map<Integer, Map<String, MailBox>> serverMailBoxMap;
    private ProfileService profileService;



    public GmailBot(ProfileService profileService) {
        this.serverMailBoxMap = new ConcurrentHashMap<>();
        this.profileService = profileService;
    }


    /**
     API GmailBot, этот бот координирует работу ботов по работе с почтой gmail
     */
    @PostMapping("/gmail_bot")
    @Async
    public CompletableFuture<String> gmailBot(@RequestBody String json) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JSONObject jsonObject = new JSONObject(json);
                String botName = jsonObject.getString("bot_name");
                if ("gmailBot".equals(botName)) {
                    return gmailBotExecute(jsonObject);
                }
                if ("worker".equals(botName)){
                    return workerExecute(jsonObject);
                }
                return "значение по ключу bot_name должно быть: gmailBot или worker" ;
            } catch (Exception e) {
                e.printStackTrace();
                return "Ошибка выполнения";
            }
        });
    }

    private String gmailBotExecute(JSONObject source) {
        Integer server = source.getInt("server");
        if (!serverMailBoxMap.containsKey(server)) {
            serverMailBoxMap.put(server, new HashMap<>());
        }
        Map<String, MailBox> mailBoxMap  = serverMailBoxMap.get(server);
        JSONObject result = new JSONObject();
        if ("getWork".equals(source.getString("status"))) {
            for (String gmail : mailBoxMap.keySet()) {
                MailBox mailBox = mailBoxMap.get(gmail);
                if (mailBox.hasWork()) {
                    Profile profile = profileService.getProfileByGmail(gmail);
                    result.put("work", mailBox.getWork());
                    mailBox.block(true);
                    Proxy proxy = profile.getProxy();
                    result.put("proxyAddress", proxy.getProxyAddress());
                    result.put("proxy_login", proxy.getLogin());
                    result.put("proxy_password", proxy.getPassword());
                    result.put("proxy_socks5port", proxy.getSocks5Port());
                    result.put("gmail", profile.getGmail().getGmailAddress());
                    result.put("gmail_password", profile.getGmail().getPassword());
                    result.put("profile", profile.getNickname());
                }
            }
        }
        if ("submitWork".equals(source.getString("status"))) {
            String gmail = source.getString("gmail");
            JSONArray resultArray = new JSONArray(source.getString("result_array"));
            mailBoxMap.get(gmail).submitWork(resultArray);
            mailBoxMap.get(gmail).block(false);
            return "work submitted";
        }
        if (result.isEmpty()) {
            return "нет работы";
        }
        return result.toString();
    }

    private String workerExecute(JSONObject source) {
        Integer server = source.getInt("server");
        if (!serverMailBoxMap.containsKey(server)) {
            serverMailBoxMap.put(server, new HashMap<>());
        }
        Map<String, MailBox> mailBoxMap  = serverMailBoxMap.get(server);
        String gmail = source.getString("gmail");
        if (!mailBoxMap.containsKey(gmail)){
            mailBoxMap.put(gmail, new MailBox());
        }
        String buks = source.getString("buks");
        String title = source.getString("title");
        String patternText = source.getString("patternText");
        return mailBoxMap.get(gmail).getMail(buks, title, patternText);
    }
}
