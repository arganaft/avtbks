package avtobuks.autoreger;

import avtobuks.AccountList;
import avtobuks.db.account.Account;
import avtobuks.db.account.AccountService;
import avtobuks.db.botSettings.BotSettingsService;
import avtobuks.db.buks.Buks;
import avtobuks.db.buks.BuksService;
import avtobuks.db.fingerprint.Fingerprint;
import avtobuks.db.fingerprint.FingerprintRepository;
import avtobuks.db.gmail.Gmail;
import avtobuks.db.gmail.GmailService;
import avtobuks.db.profile.Profile;
import avtobuks.db.profile.ProfileService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AutoReger {

    private final AccountList accountList;
    private final BuksService buksService;
    private final AccountService accountService;
    private final BotSettingsService settings;
    private final FingerprintRepository fprepository;
    private final Buks serfEarnerBuks;
    private final ProfileService profileService;
    private final GmailService gmailService;

    @Autowired
    public AutoReger(AccountList accountList, BuksService buksService,
                     AccountService accountService, BotSettingsService settings,
                     FingerprintRepository fprepository, ProfileService profileService,
                     GmailService gmailService) {
        this.accountList = accountList;
        this.buksService = buksService;
        this.accountService = accountService;
        this.settings = settings;
        this.fprepository = fprepository;
        this.serfEarnerBuks = buksService.findByBuksName("surfearner");
        this.profileService = profileService;
        this.gmailService = gmailService;
    }

    public String execute(JSONObject jsonObj) {
        if ("CookieBot".equals(jsonObj.getString("name"))) {
            if ("submitResults".equals(jsonObj.getString("jobStatus"))) {
                return cookieBotSubmitResults(jsonObj);
            }
            if ("getNewWork".equals(jsonObj.getString("jobStatus"))) {
                return cookieBotGetNewWork(jsonObj);
            }
        }

        if ("GmailReger".equals(jsonObj.getString("name"))) {
            if ("submitResults".equals(jsonObj.getString("jobStatus"))) {
                return GmailRegerSubmitResults(jsonObj);
            }
            if ("getNewWork".equals(jsonObj.getString("jobStatus"))) {
                return GmailRegerGetNewWork(jsonObj);
            }
        }
        if ("WorkerReger".equals(jsonObj.getString("name"))) {
            if ("submitResults".equals(jsonObj.getString("jobStatus"))) {
                return workerRegerSubmitResults(jsonObj);
            }
            if ("getNewWork".equals(jsonObj.getString("jobStatus"))) {
                return workerRegerGetNewWork(jsonObj);
            }
        }

        return "Указанно не правильное имя бота";
    }

    private String workerRegerGetNewWork(JSONObject jsonObj) {
        Buks buks = buksService.findByBuksName(jsonObj.getString("buks"));
        LocalDate localDate = LocalDate.now();
        if (buks.getToday().isBefore(localDate)) {
            buks.setToday(localDate);
            buks.setAccountToday(0);
        }
        if (buks.getAccountToday() > buks.getMaxAccount()) {
            return "превышен лимит регистраций на сегодня";
        }
        JSONObject json = new JSONObject();
        boolean isWorkFind = false;
        if (buks == null) {
            return "указанно не правильное имя букса";
        }
        for (Account account : accountList.getAccountsByBuksName(jsonObj.getLong("server"), buks.getBuksName())) {
            if (account.getCredentials() == null && !account.getActive() && account.getProfile().getPayeer() != null) {
                json.put("browser_profile", account.getBrowserProfile());
                json.put("extensions", buks.getExtensions());
                json.put("name", account.getProfile().getName());
                json.put("surname", account.getProfile().getSurname());
                json.put("xevil_server", settings.get().getXevilServer());
                json.put("sctg_xevil_key", settings.get().getSctgXevilKey());
                json.put("patronymic", account.getProfile().getPatronymic());
                json.put("gender", account.getProfile().getGender());
                json.put("image", account.getProfile().getAccountPhoto());
                json.put("birth_date", account.getProfile().getBirthDate().toString());
                json.put("gmail", account.getProfile().getGmail().getGmailAddress());
                json.put("phone", account.getProfile().getPhone().getPhoneNumber());
                json.put("payeer", account.getProfile().getPayeer().getLogin());
                json.put("account_id", account.getId());
                addAllProperties(json, buks, account);
                account.setActive(true);
                accountService.save(account);
                isWorkFind = true;
                buks.setAccountToday(buks.getAccountToday() + 1);
                buksService.save(buks);
                break;
            }
        }
        return isWorkFind? json.toString() : "нет работы";
    }

    private String workerRegerSubmitResults(JSONObject jsonObj) {
        Long accountID = jsonObj.getLong("account_id");
        Long server = jsonObj.getLong("server");
        Account account = accountList.getAccountsByID(server, accountID);
        account.setCredentials(jsonObj.getString("credentials"));
        account.setActive(false);
        accountService.save(account);
        return "work Submitted";
    }

    private boolean isReady(Account account) {
        return !account.getActive() && account.getUnlockTime().isBefore(LocalDateTime.now());
    }

    //TODO тут нужно использовать метод getAccountsByID из AccountList
    private String cookieBotSubmitResults(JSONObject jsonObj) {
        for (Account account : accountList.getAccountsByBuksName(jsonObj.getLong("server"), jsonObj.getString("buks"))) {
            if (jsonObj.getString("profile").equals(account.getProfile().getNickname())) {
                if (jsonObj.getBoolean("isNew")) {
                    account.setBrowserProfile(jsonObj.getString("browser-profile"));
                    account.setActive(false);
                    account.setBrowserFarmDays(1);
                    LocalDate tomorrow = LocalDate.now().plusDays(1);// Получаем завтрашнюю дату
                    LocalDateTime tomorrowAtTime = LocalDateTime.of(tomorrow, LocalTime.of(0, 1));// Устанавливаем время на 00:01
                    account.setUnlockTime(tomorrowAtTime);

                    // добавляем ссылку на фингер принт
                    Fingerprint fingerprint = new Fingerprint();
                    fingerprint.setFingerprint(jsonObj.getString("fingerprint"));
                    fprepository.save(fingerprint);
                    account.setFingerprint(fingerprint);
                    accountService.save(account);

                    return "work Submitted";
                } else {
                    account.setActive(false);
                    account.setBrowserFarmDays(account.getBrowserFarmDays() + 1);
                    LocalDate tomorrow = LocalDate.now().plusDays(1);// Получаем завтрашнюю дату
                    LocalDateTime tomorrowAtTime = LocalDateTime.of(tomorrow, LocalTime.of(0, 1));// Устанавливаем время на 00:01
                    account.setUnlockTime(tomorrowAtTime);
                    accountService.save(account);
                    return "work Submitted";
                }
            }
        }
        return "buks not found";
    }

    //TODO еще нужно добавить механизм при котором будет глобальное ограничение на количество создаваемых профилей в день
    // для этого в класс Букс нужно добавить 2 поля дата и количество буксов за сегодня, сначала проверяется сегодняшняя ли дата,
    // если нет то ставим на сегодня, потом смотрим на количество буксов, если меньше указанного количества то начинаем
    // регистрировать аккаунт

    private String cookieBotGetNewWork(JSONObject jsonObj) {
        Long server = jsonObj.getLong("server");
        JSONObject json = new JSONObject();
        boolean isWorkFind = false;
        for (Buks buks : buksService.findAll()) {
            for (Account account : accountList.getAccountsByBuksName(server, buks.getBuksName())) {
                // Если новый аккаунт
                if (isReady(account) && account.getBrowserProfile() == null) {
                    String browserProfile = String.format("%s_%s_%s", server, account.getProfile().getNickname(), account.getBuks().getBuksName());
                    json.put("isNew", true);
                    json.put("perfect-canvas", buks.getPerfect_canvas());
                    json.put("creepjsRating", settings.get().getCreepjsRating());
                    json.put("browser-profile", browserProfile);
                    addAllProperties(json, buks, account);
                    account.setActive(true);
                    accountService.save(account);
                    isWorkFind = true;
                    break;
                }
                // Если для аккаунта уже создан профиль браузера и фингер принт
                Integer browserFarmDays = account.getBrowserFarmDays();
                if (isReady(account) && browserFarmDays < settings.get().getCookieSetDays()) {
                    json.put("isNew", false);
                    json.put("browser-profile", account.getBrowserProfile());
                    addAllProperties(json, buks, account);
                    account.setBrowserFarmDays(browserFarmDays + 1);
                    accountService.save(account);
                    isWorkFind = true;
                    break;
                }
            }
            if (isWorkFind) {
                break;
            }
        }
        return isWorkFind? json.toString() : "нет работы";
    }

    private void addAllProperties(JSONObject json, Buks buks, Account account) {
        json.put("buks", account.getBuks().getBuksName());
        json.put("profile", account.getProfile().getNickname());
        json.put("proxyAddress", account.getProfile().getProxy().getProxyAddress());
        json.put("login", account.getProfile().getProxy().getLogin());
        json.put("password", account.getProfile().getProxy().getPassword());
        json.put("socks5Port", account.getProfile().getProxy().getSocks5Port());
    }

    private String GmailRegerGetNewWork(JSONObject jsonObj) {
        Long server = jsonObj.getLong("server");
        JSONObject json = new JSONObject();
        boolean isWorkFind = false;
        if (serfEarnerBuks != null) {
            for (Account account : accountList.getAccountsByBuksName(server, serfEarnerBuks.getBuksName())) {
                if (isReady(account) && account.getProfile().getGmail() == null) {
                    json.put("browser-profile", account.getBrowserProfile());
                    json.put("name", account.getProfile().getName());
                    json.put("surname", account.getProfile().getSurname());
                    json.put("patronymic", account.getProfile().getPatronymic());
                    json.put("gender", account.getProfile().getGender());
                    json.put("birth_date", account.getProfile().getBirthDate().toString());
                    addAllProperties(json, serfEarnerBuks, account);
                    account.setActive(true);
                    accountService.save(account);
                    isWorkFind = true;
                    break;
                }
            }
        }
        return isWorkFind? json.toString() : "нет работы";
    }
    private String GmailRegerSubmitResults(JSONObject jsonObj) {
        Profile profile = profileService.getProfilesByNickName(jsonObj.getString("profile"));
        Long server = jsonObj.getLong("server");
        if (profile == null) {
            return String.format("Профиль с никнеймом %s не найден", jsonObj.getString("profile"));
        }
        for (Account account : accountList.getAccountsByBuksName(server, serfEarnerBuks.getBuksName())) {
            if (account.getProfile().equals(profile)) {
                account.setActive(false);
                accountService.save(account);
                break;
            }
        }
        Gmail gmail = new Gmail(jsonObj.getString("gmail_address"), jsonObj.getString("gmail_password"));
        gmailService.save(gmail);
        profile.setGmail(gmail);
        profileService.saveProfile(profile);
        return "work Submitted";
    }

}
