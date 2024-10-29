package avtobuks.db.gmail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GmailRepository extends JpaRepository<Gmail, String> {
}
