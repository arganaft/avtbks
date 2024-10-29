package avtobuks.db.phone.referralLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralLinkRepository extends JpaRepository<ReferralLink, String> {
}
