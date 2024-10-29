package avtobuks.db.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final List<Profile> profiles;
    private final Map<Long,List<Profile>> serverProfileMap;
    private final Map<String, Profile> nickNameProfileMap;
    private final Map<String, Profile> gmailProfileMap;

    @Autowired
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        this.profiles = new LinkedList<>();
        this.serverProfileMap = new HashMap<>();
        this.nickNameProfileMap = new HashMap<>();
        this.gmailProfileMap = new HashMap<>();
        serverProfileMap.put(null, new LinkedList<>());
        for (Profile profile : profileRepository.findAll()) {
            profiles.add(profile);
            String nickname = profile.getNickname();
            nickNameProfileMap.put(profile.getNickname(), profile);
            gmailProfileMap.put(profile.getGmail().getGmailAddress(), profile);
            Long server = profile.getServer();
            if (serverProfileMap.containsKey(server)) {
                serverProfileMap.get(server).add(profile);
            } else {
                serverProfileMap.put(server, new LinkedList<>());
                serverProfileMap.get(server).add(profile);
            }
        }
    }

    public List<Profile> findAll() {
        return profiles;
    }

    public Profile getProfilesByNickName(String nickName) {
        return nickNameProfileMap.get(nickName);
    }

    public Profile getProfileByGmail(String gmail) {
        return gmailProfileMap.get(gmail);
    }

    public List<Profile> getProfilesByServer(Long server) {
        return serverProfileMap.get(server);
    }

    public List<Profile> getAllNullServer() {
        return serverProfileMap.get(null);
    }


    public void saveProfile(Profile profile) {
        profileRepository.save(profile);
        profiles.add(profile);
        nickNameProfileMap.put(profile.getNickname(), profile);
        gmailProfileMap.put(profile.getGmail().getGmailAddress(), profile);
        Long server = profile.getServer();
        if (serverProfileMap.containsKey(server)) {
            serverProfileMap.get(server).add(profile);
        } else {
            serverProfileMap.put(server, new LinkedList<>());
            serverProfileMap.get(server).add(profile);
        }
    }
}
