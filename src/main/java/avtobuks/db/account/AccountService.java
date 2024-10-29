package avtobuks.db.account;

import avtobuks.db.profile.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final List<Account> accounts;
    private final Map<Profile, List<Account>> profileAccountMap;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.accounts = new LinkedList<>();
        this.profileAccountMap = new HashMap<>();
        for (Account account : accountRepository.findAll()) {
            accounts.add(account);
            Profile profile = account.getProfile();
            if(profileAccountMap.containsKey(profile)) {
                profileAccountMap.get(profile).add(account);
            } else {
                profileAccountMap.put(profile, new LinkedList<>());
                profileAccountMap.get(profile).add(account);
            }
        }
    }

    public List<Account> findAll() {
        return accounts;
    }

    public List<Account> findByProfile(Profile profile) {
        return profileAccountMap.get(profile);
    }

    public void save(Account account) {
        accountRepository.save(account);
        accounts.add(account);
        Profile profile = account.getProfile();
        if(profileAccountMap.containsKey(profile)) {
            profileAccountMap.get(profile).add(account);
        } else {
            profileAccountMap.put(profile, new LinkedList<>());
            profileAccountMap.get(profile).add(account);
        }
    }
}
