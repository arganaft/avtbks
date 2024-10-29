package avtobuks.db.cookieSites;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CookieSitesService {
    private final CookieSitesRepository cookieSitesRepository;

    @Autowired
    public CookieSitesService(CookieSitesRepository cookieSitesRepository) {
        this.cookieSitesRepository = cookieSitesRepository;
    }

    public void addSite(CookieSites site) {
        cookieSitesRepository.save(site);
        System.out.println("добавил сайт в базу");
    }

    public List<CookieSites> getAllCookieSites() {
        return cookieSitesRepository.findAll();
    }

    public boolean existsCookieSite(String url) {
        return cookieSitesRepository.existsById(url);
    }

    public void deleteCookieSite(String url) {
        cookieSitesRepository.deleteById(url);
    }

    public ArrayList<CookieSites> getCookieSitesOrderedByLiRankDesc() {
        List<CookieSites> allSites = cookieSitesRepository.findAll();

        // Сортировка списка по убыванию li_rank
        allSites.sort((site1, site2) -> Long.compare(site2.getLi_rank(), site1.getLi_rank()));

        // Преобразование List в ArrayList
        return new ArrayList<>(allSites);
    }

}
