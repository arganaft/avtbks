package avtobuks.db.savedLinks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedLinksRepository extends JpaRepository<SavedLinks, String> {
}
