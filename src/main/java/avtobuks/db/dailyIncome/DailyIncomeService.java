package avtobuks.db.dailyIncome;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DailyIncomeService {
    private final DailyIncomeRepository dailyIncomeRepository;

    @Autowired
    public DailyIncomeService(DailyIncomeRepository dailyIncomeRepository) {
        this.dailyIncomeRepository = dailyIncomeRepository;
    }
}
