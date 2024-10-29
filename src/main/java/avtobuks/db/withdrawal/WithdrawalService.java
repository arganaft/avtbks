package avtobuks.db.withdrawal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WithdrawalService {
    private final WithdrawalRepository withdrawalRepository;

    @Autowired
    public WithdrawalService (WithdrawalRepository withdrawalRepository) {
        this.withdrawalRepository = withdrawalRepository;
    }
}
