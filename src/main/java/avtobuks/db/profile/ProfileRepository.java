package avtobuks.db.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
    List<Profile> findByServer(Long server);

    List<Profile> findByServerIsNull();

}
