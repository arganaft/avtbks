package avtobuks;

import avtobuks.db.account.Account;
import avtobuks.db.account.AccountService;
import avtobuks.db.botSettings.BotSettingsService;
import avtobuks.db.buks.Buks;
import avtobuks.db.buks.BuksService;
import avtobuks.db.profile.Profile;
import avtobuks.db.profile.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AccountList {

    private Map<Long, ServerAccounts> serverAccounts;
    private Map<Long, Set<Profile>> serverProfiles;
    private Set<Profile> nullServerProfiles;
    private final BotSettingsService settings;
    private final ProfileService profileService;
    private final BuksService buksService;
    private final AccountService accountService;

    @Autowired
    public AccountList(BotSettingsService botSettingsService, ProfileService profileService, BuksService buksService, AccountService accountService) {
        this.settings = botSettingsService;
        this.profileService = profileService;
        this.buksService = buksService;
        this.accountService = accountService;
        init();
    }

    private void init() {
        serverAccounts = new ConcurrentHashMap<>();
        serverProfiles = new HashMap<>();
        nullServerProfiles = new HashSet<>();
        for (Profile profile : profileService.findAll()) {
            Long server = profile.getServer();
            if (server == null) {
                nullServerProfiles.add(profile);
            } else {
                if (serverProfiles.containsKey(server)) {
                    serverProfiles.get(server).add(profile);
                } else {
                    serverProfiles.put(server, new HashSet<>());
                    serverProfiles.get(server).add(profile);
                }
            }
        }

        for (Account account : accountService.findAll()) {
            Long server = account.getProfile().getServer();
            if (!serverAccounts.containsKey(server)) {
                serverAccounts.put(server, new ServerAccounts());
            }
            serverAccounts.get(server).addAccount(account);
        }
    }


    public Set<Account> getAccountsByBuksName(Long serverId, String buks) {
        return serverAccounts.computeIfAbsent(serverId, this::addServer).getAccountsByBuks(buks);
    }

    public Account getAccountsByID(Long serverId, Long id) {
        return serverAccounts.computeIfAbsent(serverId, this::addServer).getAccountsByID(id);
    }

    private ServerAccounts addServer(Long serverId) {
        ServerAccounts newServerAccounts = new ServerAccounts();
        serverProfiles.put(serverId, new HashSet<>());
        for (Profile profile : nullServerProfiles.stream().limit(settings.get().getMaxProfileOnServer()).toList() ) {
            profile.setServer(serverId);
            profileService.saveProfile(profile);
            serverProfiles.get(serverId).add(profile);
            nullServerProfiles.remove(profile);
        }
        createAccaunts(newServerAccounts, serverProfiles.get(serverId));
        return newServerAccounts;
    }


    private void createAccaunts(ServerAccounts serverAccounts, Set<Profile> profiles) {
        for (Profile profile : profiles) {
            for (Buks buks : buksService.findAll()) {
                Account newAccount = new Account(profile, buks, LocalDate.now());
                accountService.save(newAccount);
                serverAccounts.addAccount(newAccount);
            }
        }
    }

    public void expandServers() {
        for (Profile profile : nullServerProfiles) {
            addNewProfile(profile);
        }
    }



    public void addNewProfile(Profile profile) {
        for (Long server : serverProfiles.keySet()) {
            if (serverProfiles.get(server).size() < settings.get().getMaxProfileOnServer()) {
                profile.setServer(server);
                profileService.saveProfile(profile);
                serverProfiles.get(server).add(profile);
                for (Buks buks : buksService.findAll()) {
                    Account account = new Account(profile, buks, LocalDate.now());
                    accountService.save(account);
                    serverAccounts.get(server).addAccount(account);
                }
                break;
            }
        }
        if (profile.getServer() == null) {
            nullServerProfiles.add(profile);
        }
    }


    public void addNewBuks(Buks buks) {
        for (Long server : serverProfiles.keySet()) {
            for (Profile profile : serverProfiles.get(server)) {
                Account account = new Account(profile, buks, LocalDate.now());
                accountService.save(account);
                if (!serverAccounts.containsKey(server)) {
                    serverAccounts.put(server, new ServerAccounts());
                }
                serverAccounts.get(server).addAccount(account);
            }
        }

    }


    private static class ServerAccounts {
        private HashMap<String, Set<Account>> buksAccountMap;
        private HashMap<Long, Account> idAccountsMap;
        private Lock lock;

        public ServerAccounts() {
            this.buksAccountMap = new HashMap<>();
            this.idAccountsMap = new HashMap<>();
            this.lock = new ReentrantLock();
        }

        public Set<Account> getAccountsByBuks(String buks) {
            lock.lock();
            try {
                return buksAccountMap.get(buks);
            } finally {
                lock.unlock();
            }
        }

        public Account getAccountsByID(Long id) {
            lock.lock();
            try {
                return idAccountsMap.get(id);
            } finally {
                lock.unlock();
            }
        }

        public int getCount() {
            return buksAccountMap.size();
        }

        public void addAccount(Account account) {
            lock.lock();
            try {
                String buks = account.getBuks().getBuksName();
                Long id = account.getId();
                if (!buksAccountMap.containsKey(buks)) {
                    buksAccountMap.put(buks, new HashSet<>());
                }
                if (!idAccountsMap.containsKey(id)) {
                    idAccountsMap.put(id, account);
                }
                buksAccountMap.get(buks).add(account);
            } finally {
                lock.unlock();
            }
        }
    }
}
