package avtobuks.db.buks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuksRepository extends JpaRepository<Buks, String> {
    Optional<Buks> findByBuksName(String buksName);
}
