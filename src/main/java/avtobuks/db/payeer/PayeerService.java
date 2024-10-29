package avtobuks.db.payeer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayeerService {
    private final PayeerRepository payeerRepository;

    @Autowired
    public PayeerService(PayeerRepository payeerRepository) {
        this.payeerRepository = payeerRepository;
    }

    public void save(Payeer payeer) {
        payeerRepository.save(payeer);
    }
}
