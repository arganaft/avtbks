package avtobuks.db.proxy;

import avtobuks.db.profile.Profile;
import avtobuks.db.profile.ProfileRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ProxyManager {
    //TODO купить новое прокси, проверить есть ли он в базе ранее купленных

    //TODO продлить прокси если аккаунт не заблокирован

    //TODO public методы getNewProxy

    private final RestTemplate restTemplate;
    private final ProxyRepository proxyRepository;
    private final HashMap<String, Integer> alfa3Code;
    private final Set<String> proxySet;
    private  final ProfileRepository profileRepository;

    public ProxyManager(ProxyRepository proxyRepository, ProfileRepository profileRepository) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // Тайм-аут на подключение
        factory.setReadTimeout(5000); // Тайм-аут на чтение
        this.restTemplate = new RestTemplate(factory);
        this.proxyRepository = proxyRepository;
        this.profileRepository = profileRepository;
        proxySet = new HashSet<>();
        this.alfa3Code = new HashMap<>();
        alfa3Code.put("AUT", 0);
        alfa3Code.put("GBR", 0);
        alfa3Code.put("DEU", 0);
        alfa3Code.put("ESP", 0);
        alfa3Code.put("ITA", 0);
        alfa3Code.put("LVA", 0);
        alfa3Code.put("NLD", 0);
        alfa3Code.put("POL", 0);
        alfa3Code.put("ROU", 0);
        alfa3Code.put("FRA", 0);
        for (Proxy proxy : proxyRepository.findAll()) {
            proxySet.add(proxy.getProxyAddress());
            String country = proxy.getCountry();
            if (alfa3Code.containsKey(country)) {
                alfa3Code.put(country, alfa3Code.get(country) + 1);
            }
        }
    }

//    В @Scheduled(cron = "0 0 12 * * ?"):
//    0 - секунда
//    0 - минута
//    12 - час (по 24-часовому формату, т.е. 12:00 PM)
//    * - любой день месяца
//    * - любой месяц
//    ? - любой день недели
    @Scheduled(cron = "0 0 12 * * ?")
    void extendProxy() {
        System.out.println("Проверка и продление прокси для активных профилей каждый день в 12:00");
        updateProxyDateEnd(); // Обновляем значения прокси в таблице по данным поставщика прокси
        LocalDate today = LocalDate.now();
        Map<String, Proxy> proxyIDMap = new HashMap<>();
        for (Profile profile : profileRepository.findAll()) {
            Proxy proxy = profile.getProxy();
            if (!profile.getIsBlocked() && proxy.getDateEnd().isBefore(today.plusDays(3))) {
                proxyIDMap.put(proxy.getIpId(), proxy);
                break;
            }
        }
        JSONObject jsonObject = new JSONObject(extendProxyAPI(proxyIDMap.keySet()));
        if (!jsonObject.getBoolean("success")) {
            //TODO нужно создать механизм оповещений в таких ситуациях, в нашем случае
            // когда не удалось продлить прокси
        }
    }

    private void updateProxyDateEnd() {
        Map<String, Proxy> proxyMap = new HashMap<>();
        for (Proxy proxy : proxyRepository.findAll()) {
            proxyMap.put(proxy.getIpId(), proxy);
        }
        JSONObject jsonObj = new JSONObject(getAllProxyAPI());
        JSONArray proxiesArray = jsonObj.getJSONArray("isp");
        for (int i = 0; i < proxiesArray.length(); i++) {
            JSONObject proxyObject = proxiesArray.getJSONObject(i);
            String id = proxyObject.getString("id");
            if (proxyMap.containsKey(id)) {
                String dateTimeStringEnd = proxyObject.getString("dateEnd");
                ZonedDateTime zonedDateTimeEnd = ZonedDateTime.parse(dateTimeStringEnd, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                LocalDate dateEnd = zonedDateTimeEnd.toLocalDate();
                proxyMap.get(id).setDateEnd(dateEnd);
                proxyRepository.save(proxyMap.get(id));
            }
        }
        System.out.println("обновили значения в таблице прокси");
    }

    @Retryable(
            retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000))
    private String extendProxyAPI(Set<String> proxyIDList) {
        JSONArray jsonArray = new JSONArray(proxyIDList);
        String url = "https://proxy-ipv4.com/client-api/v1/rXXMcrcffBVq/extend";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        String requestBody = String.format("""
                {
                    "proxyType":"isp",
                    "days":30,
                    "IpId":
                    %s
                }
                """, jsonArray) ;
        System.out.println(requestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();
        } catch (RestClientException e) {
            // Обработка ошибки соединения
            System.err.println("Ошибка при отправке запроса: " + e.getMessage());
            return "Service Unavailable";
        }
    }




    public Proxy getNewProxy() throws InterruptedException {
        String order = getNewProxyAPI();
        String allProxy = getAllProxyAPI();
        int sec = 1;
        Proxy proxy = parseProxy(order, allProxy);
        while (proxy == null) {
            System.out.println("Жду секунду номер = " + sec++);
            Thread.sleep(1000);
            allProxy = getAllProxyAPI();
            proxy = parseProxy(order, allProxy);
        }
        return proxy;
    }

    private Proxy parseProxy(String order, String allProxy) {
        if (!"Service Unavailable".equals(order) && !"Service Unavailable".equals(allProxy)) {
            JSONObject jsonObj = new JSONObject(allProxy);
            JSONArray proxiesArray = jsonObj.getJSONArray("isp");
            for (int i = 0; i < proxiesArray.length(); i++) {
                JSONObject proxyObject = proxiesArray.getJSONObject(i);
                String ip = proxyObject.getString("ip");
                if (!proxySet.contains(ip)) {
                    proxySet.add(ip);
                    String dateTimeStringStart = proxyObject.getString("dateStart");
                    ZonedDateTime zonedDateTimeStart = ZonedDateTime.parse(dateTimeStringStart, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    LocalDate dateStart = zonedDateTimeStart.toLocalDate();
                    String dateTimeStringEnd = proxyObject.getString("dateEnd");
                    ZonedDateTime zonedDateTimeEnd = ZonedDateTime.parse(dateTimeStringEnd, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    LocalDate dateEnd = zonedDateTimeEnd.toLocalDate();
                    Proxy proxy = new Proxy(
                            ip,
                            proxyObject.getString("id"),
                            proxyObject.getString("socks5Port"),
                            proxyObject.getJSONObject("authInfo").getString("login"),
                            proxyObject.getJSONObject("authInfo").getString("password"),
                            proxyObject.getString("country"),
                            dateStart,
                            dateEnd
                    );
                    proxyRepository.save(proxy);
                    return proxy;
                }
            }
        }
        return null;
    }

    @Retryable(
            retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000))
    private String getAllProxyAPI() {
        String url = "https://proxy-ipv4.com/client-api/v1/rXXMcrcffBVq/get/proxies";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            // Обработка ошибки соединения
            System.err.println("Ошибка при отправке запроса: " + e.getMessage());
            return "Service Unavailable";
        }
    }

    @Retryable(
            retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000))
    private String getNewProxyAPI() {
        String url = "https://proxy-ipv4.com/client-api/v1/rXXMcrcffBVq/order";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        String requestBody = String.format("""
                {
                    "proxyType": "isp",
                    "days": 30,
                    "goal": "Google",
                    "country": "%s",
                    "count": 1,
                    "authType": "login"
                }
                """, getAlfa3Code()) ;
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();
        } catch (RestClientException e) {
            // Обработка ошибки соединения
            System.err.println("Ошибка при отправке запроса: " + e.getMessage());
            return "Service Unavailable";
        }
    }

    private String getAlfa3Code() {
        String minKey = null;
        int minValue = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : alfa3Code.entrySet()) {
            if (entry.getValue() < minValue) {
                minValue = entry.getValue();
                minKey = entry.getKey();
            }
        }
        return  minKey;
    }



}
