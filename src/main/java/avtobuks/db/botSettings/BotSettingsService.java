package avtobuks.db.botSettings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;


@Service
public class BotSettingsService {
    private final BotSettingsRepository botSettingsRepository;
    private BotSettings settings;

    @Autowired
    public BotSettingsService(BotSettingsRepository botSettingsRepository) {
        this.botSettingsRepository = botSettingsRepository;
        this.settings = initSettings();
    }

    public BotSettings get() {
        return settings;
    }

    private BotSettings initSettings() {
        return botSettingsRepository.findAll().stream().findFirst().orElseGet(() -> {
            BotSettings defaultSettings = new BotSettings(1, "9d4Bk0vbdnPpn98bZJNLm1qPhlgOnrRocPg8",
                    180, "https://avtobuks.net", LocalTime.of(6, 10),
                    "63.5", "127.0.0.1:80", "XvaiLGUwq0xNxIbbbZaIs0vBAWVjZj20",
                    6,3, 100, 20,
                    3, 30, 50, 480,
                    120, 60, 3, 8);
            // Set default values here
            return botSettingsRepository.save(defaultSettings);
        });
    }

    @Transactional
    public void saveSettings(BotSettings settings) {
        if (settings.getId() == null) {
            botSettingsRepository.deleteAll();
        }
        botSettingsRepository.save(settings);
        this.settings = settings;
    }
}
