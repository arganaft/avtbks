package avtobuks.db.buks;

import avtobuks.gmail_bot.GmailBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BuksService  {
    private final BuksRepository buksRepository;
    List<Buks> buksList;
    Map<String, Buks> buksMap;

    @Autowired
    public BuksService(BuksRepository buksRepository, GmailBot gmailBot) {
        this.buksRepository = buksRepository;
        this.buksList = new LinkedList<>();
        buksMap = new HashMap<>();
        for (Buks buks : buksRepository.findAll()) {
            buksList.add(buks);
            buksMap.put(buks.getBuksName(), buks);
        }
    }

    public List<Buks> findAll() {
        return buksList;
    }

    public void save(Buks newBuks) {
        buksRepository.save(newBuks);
        buksList.add(newBuks);
        buksMap.put(newBuks.getBuksName(), newBuks);
    }

    public Buks findByBuksName(String buksName) {
        return buksMap.get(buksName);
    }
}
