package avtobuks.db.savedLinks;

import avtobuks.db.profile.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavedLinksService {
    private final SavedLinksRepository savedLinksRepository;

    @Autowired
    public SavedLinksService(SavedLinksRepository savedLinksRepository) {
        this.savedLinksRepository = savedLinksRepository;
    }
}
