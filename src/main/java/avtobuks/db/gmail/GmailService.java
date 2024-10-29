package avtobuks.db.gmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GmailService {
    private final GmailRepository gmailRepository;

    @Autowired
    public GmailService(GmailRepository gmailRepository) {
        this.gmailRepository = gmailRepository;
    }
    public void save(Gmail gmail) {
        gmailRepository.save(gmail);
    }
}
