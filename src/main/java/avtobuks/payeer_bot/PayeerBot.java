package avtobuks.payeer_bot;

import avtobuks.db.payeer.Payeer;
import avtobuks.db.payeer.PayeerService;
import avtobuks.db.profile.Profile;
import avtobuks.db.profile.ProfileService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@Service
public class PayeerBot {
    private final ProfileService profileService;
    private final PayeerService payeerService;

    @Autowired
    public PayeerBot(ProfileService profileService, PayeerService payeerService) {
        this.profileService = profileService;
        this.payeerService = payeerService;
    }


    /**
     API PayeerBot, координирует создание новых аккаунтов payeer
     и лучше запускать бота только в одном потоке, иначе будут блокировки
     */
    @PostMapping("/payeer_bot")
    @Async
    public CompletableFuture<String> payeerBot(@RequestBody String json) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JSONObject jsonObject = new JSONObject(json);
                String status = jsonObject.getString("status");
                if ("get_new_work".equals(status)) {
                    return getNewWork();
                }
                if ("create_new_payeer".equals(status)) {
                    return createNewPayeer(jsonObject);
                }

                return "какой то текст" ;
            } catch (Exception e) {
                e.printStackTrace();
                return "Ошибка выполнения";
            }
        });
    }

    private String getNewWork() {
        JSONObject jsonObject = new JSONObject();
        for (Profile profile : profileService.findAll()) {
            if (profile.getPayeer() == null && profile.getGmail() != null) {
                jsonObject.put("gmail", profile.getGmail().getGmailAddress());
                jsonObject.put("server", profile.getServer().toString());
                return jsonObject.toString();
            }
        }
        return "нет работы";
    }

    private String createNewPayeer(JSONObject jsonObject) {
        String login = jsonObject.getString("login");
        String password = jsonObject.getString("password");
        String secretCode = jsonObject.getString("secret_code");
        String masterKey = jsonObject.getString("master_key");
        Payeer payeer = new Payeer(login, password, secretCode, masterKey);
        payeerService.save(payeer);
        String gmail = jsonObject.getString("gmail");
        Profile profile = profileService.getProfileByGmail(gmail);
        profile.setPayeer(payeer);
        profileService.saveProfile(profile);


        return "work Submitted";
    }

}
