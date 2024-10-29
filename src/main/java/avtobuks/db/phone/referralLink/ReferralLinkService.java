package avtobuks.db.phone.referralLink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReferralLinkService {
    private final ReferralLinkRepository referralLinkRepository;

    @Autowired
    public ReferralLinkService(ReferralLinkRepository referralLinkRepository) {
        this.referralLinkRepository = referralLinkRepository;
    }
}
