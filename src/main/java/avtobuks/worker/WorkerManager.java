package avtobuks.worker;

import avtobuks.AccountList;
import avtobuks.db.account.Account;
import avtobuks.db.account.AccountService;
import avtobuks.db.botSettings.BotSettingsService;
import avtobuks.db.buks.Buks;
import avtobuks.db.buks.BuksService;
import avtobuks.db.profile.Profile;
import avtobuks.db.profile.ProfileService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@RestController
@Service
public class WorkerManager {
    private final AccountList accountList;
    private final BuksService buksService;
    private final BotSettingsService settings;
    private final Random random;
    private final AccountService accountService;
    private final ProfileService profileService;

    @Autowired
    public WorkerManager(AccountList accountList, BuksService buksService, BotSettingsService settings,
                         AccountService accountService, ProfileService profileService) {
        this.accountList = accountList;
        this.buksService = buksService;
        this.settings = settings;
        this.accountService = accountService;
        this.profileService = profileService;
        this.random = new Random();
    }


    /**
     API WorkerManager, этот бот координирует работу ботов буксов
     */
    @PostMapping("/worker_manager")
    @Async
    public CompletableFuture<String> workerManager(@RequestBody String json) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JSONObject jsonObject = new JSONObject(json);
                String jobStatus = jsonObject.getString("jobStatus");
                if ("getNewWork".equals(jobStatus)) {
                    return getNewWork(jsonObject);
                }
                if ("submitResults".equals(jobStatus)) {
                    return submitWork(jsonObject);
                }
                return "ответ";
            } catch (Exception e) {
                e.printStackTrace();
                return "Ошибка выполнения";
            }
        });
    }


    private String getNewWork(JSONObject source) {
        Long server = source.getLong("server");
        int load = Integer.MAX_VALUE;
        Account findAccount = null;
        for (Account account : accountList.getAccountsByBuksName(server, source.getString("buks"))) {
            if (!account.getActive()
                    && account.getUnlockTime().isBefore(LocalDateTime.now())
                    && account.getCredentials() != null) {
                if (account.getProfile().getLoad() == 0) {
                    load = 0;
                    findAccount = account;
                    break;
                }
                if (account.getProfile().getLoad() < load) {
                    load = account.getProfile().getLoad();
                    findAccount = account;
                }
            }
        }
        if (findAccount == null) {
            return "нет работы на данный момент";
        }
        findAccount.setActive(true);
        accountService.save(findAccount);
        Profile profile = findAccount.getProfile();
        profile.setLoad(load + 1);
        profileService.saveProfile(findAccount.getProfile());
        if (findAccount.getToday().isBefore(LocalDate.now()) && LocalDateTime.now().isAfter(LocalDate.now().atTime(settings.get().getWorkStartTime()))) {
            findAccount.setToday(LocalDate.now());
            findAccount.setMinutesWorkedToday(0);
            int minSession = settings.get().getMinSessionsPerDay();
            int maxSession = settings.get().getMaxSessionsPerDay();
            findAccount.setRandomeSessionToday(random.nextInt((maxSession - minSession) + 1) + minSession);
            if (findAccount.getAccountWarmupDays() < settings.get().getAccountWarmupDays()) {
                findAccount.setAccountWarmupDays(findAccount.getAccountWarmupDays() + 1);
            }
        }
        Buks buks = buksService.findByBuksName(source.getString("buks"));
        int minSessionWorkMins = settings.get().getMinSessionWorkMins();
        int maxSessionWorkMins = settings.get().getMaxSessionWorkMins();
        int randomSessionWorkMins = random.nextInt((maxSessionWorkMins - minSessionWorkMins) + 1) + minSessionWorkMins;
        if (findAccount.getAccountWarmupDays() < settings.get().getAccountWarmupDays()) {
            int warmupRatio = random.nextInt((3 - 2) + 1) + 2;
            randomSessionWorkMins = randomSessionWorkMins / warmupRatio;
        }
        int cashout = random.nextInt((buks.getCashoutMax() - buks.getCashoutMin()) + 1) + buks.getCashoutMin();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("xevil_server", settings.get().getXevilServer());
        jsonObject.put("sctg_xevil_key", settings.get().getSctgXevilKey());
        jsonObject.put("extensions", buks.getExtensions());
        jsonObject.put("cashout", cashout);
        jsonObject.put("work_mins", randomSessionWorkMins);
        jsonObject.put("account_id", findAccount.getId());
        jsonObject.put("credentials", findAccount.getCredentials());
        jsonObject.put("browser_profile", findAccount.getBrowserProfile());
        jsonObject.put("gmail", profile.getGmail().getGmailAddress());
        jsonObject.put("proxy_address", profile.getProxy().getProxyAddress());
        jsonObject.put("proxy_login", profile.getProxy().getLogin());
        jsonObject.put("proxy_password", profile.getProxy().getPassword());
        jsonObject.put("proxy_socks5_port", profile.getProxy().getSocks5Port());
        jsonObject.put("payeer", profile.getPayeer().getLogin());
        jsonObject.put("phone_number", profile.getPhone().getPhoneNumber());
        return jsonObject.toString();
    }

    private String submitWork(JSONObject source) {
        Account account = accountList.getAccountsByID(source.getLong("server"), source.getLong("account_id"));
        Profile profile = account.getProfile();
        profile.setLoad(profile.getLoad() - 1);
        account.setMinutesWorkedToday(account.getMinutesWorkedToday() + source.getInt("work_mins"));
        account.setFactSessionToday(account.getFactSessionToday() + 1);
        if (account.getMinutesWorkedToday() >= settings.get().getMaxDailyWorkMins()
                || account.getFactSessionToday() >= account.getRandomeSessionToday()) {
            if (account.getUnlockTime().toLocalDate().isEqual(LocalDate.now())) {
                account.setUnlockTime(LocalDate.now()
                        .plusDays(1)
                        .atTime(settings.get().getWorkStartTime()));
            }
            if (account.getUnlockTime().toLocalDate().isBefore(LocalDate.now())) {
                account.setUnlockTime(LocalDate.now()
                        .atTime(settings.get().getWorkStartTime()));
            }
        }
        if (source.getBoolean("is_blocked")) {
            account.setActive(true);
            account.setBlockedAt(LocalDate.now());
        } else {
            account.setActive(false);
        }
        return "work Submitted";
    }


}
