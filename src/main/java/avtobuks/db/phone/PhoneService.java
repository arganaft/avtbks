package avtobuks.db.phone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhoneService {
    private final PhoneRepository phoneRepository;



    @Autowired
    public PhoneService(PhoneRepository phoneRepository) {
        this.phoneRepository = phoneRepository;
    }

    public List<Phone> findAll() {
        return phoneRepository.findAll();
    }

    public Phone save(Phone phone) {
        return phoneRepository.save(phone);
    }

    public List<Phone> saveAll(List<Phone> phones) {
        return phoneRepository.saveAll(phones);
    }
}
