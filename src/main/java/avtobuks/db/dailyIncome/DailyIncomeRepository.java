package avtobuks.db.dailyIncome;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyIncomeRepository extends JpaRepository<DailyIncome, Long> {
}
