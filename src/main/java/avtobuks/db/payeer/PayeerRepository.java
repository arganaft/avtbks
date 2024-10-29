package avtobuks.db.payeer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayeerRepository  extends JpaRepository<Payeer, String> {
}
