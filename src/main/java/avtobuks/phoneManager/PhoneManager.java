package avtobuks.phoneManager;

import avtobuks.db.botSettings.BotSettingsService;
import avtobuks.db.phone.Phone;
import avtobuks.db.phone.PhoneRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Service
public class PhoneManager {
    private final Map<String, LinkedList<SMS>> smsMap;
    private final RestTemplate restTemplate;
    private final PhoneRepository phoneRepository;
    private final Set<Long> phones;
    private final BotSettingsService settings;

    public PhoneManager(RestTemplate restTemplate, PhoneRepository phoneRepository, BotSettingsService settings) {
        this.smsMap = new ConcurrentHashMap<>();
        this.restTemplate = restTemplate;
        this.phoneRepository = phoneRepository;
        this.phones = new HashSet<>();
        this.settings = settings;
        for (Phone phone : phoneRepository.findAll()) {
            Long number = phone.getPhoneNumber();
            smsMap.put(number.toString(), new LinkedList<>());
            phones.add(number);
        }
    }

    /**
    Этот метод получает webhook (POST запросы номером и СМС на этот номер)
     */
    @PostMapping("/sms-webhook-listener")
    @Async
    public CompletableFuture<Void> smsWebhookListener(@RequestBody String json) {
        return CompletableFuture.runAsync(() -> {
            try {
                JSONObject jsonObj = new JSONObject(json);
                System.out.println(jsonObj.toString());
                String dateTimeString = jsonObj.getString("date");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
                LocalDate date = dateTime.toLocalDate();
                for (String str : smsMap.keySet()) {
                    System.out.println(str);
                }
                System.out.println("исходный номер - "+jsonObj.getString("dst_number"));

                smsMap.get(jsonObj.getString("dst_number").toString())
                        .addFirst(new SMS(date, jsonObj.getString("content")));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     Должен отдавать все СМС которые пришли позже указанной даты на указанный номер
     */
    @PostMapping("/getSMS")
    @Async
    public CompletableFuture<String> getSMS(@RequestBody String json) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JSONObject jsonObj = new JSONObject(json);
                System.out.println(jsonObj.toString());
                return "смс";
            } catch (Exception e) {
                e.printStackTrace();
                return "нет смс";
            }
        });
    }

    private static class SMS {
        private final LocalDate date;
        private final String message;

        private SMS(LocalDate date, String message) {
            this.date = date;
            this.message = message;
        }
    }

    //    В @Scheduled(cron = "0 0 12 * * ?"):
    //    0 - секунда
    //    0 - минута
    //    12 - час (по 24-часовому формату, т.е. 12:00 PM)
    //    * - любой день месяца
    //    * - любой месяц
    //    ? - любой день недели.
    //  Удаляет вчерашние смс
    @Scheduled(cron = "0 0 13 * * ?")
    public void cleenSMSList() {
        LocalDate date = LocalDate.now();
        for (String number : smsMap.keySet()) {
            for (SMS sms : smsMap.get(number)) {
                if (sms.date.isBefore(date)) {
                    smsMap.get(number).remove(sms);
                }
            }
        }
    }


    /**
     получает список всех доступных для покупки мобильных номеров,
     и возвращает номер который мы еще не использовали
     */
    public long searchNumberAPI() throws JsonProcessingException {
        String url = "https://restapi.plusofon.ru/api/v1/number/search?owner_type=personal&number_type=0&phone_type=def";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));
        headers.set("Client", "10553");
        headers.set("Authorization", "Bearer " + settings.get().getToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        long phone = 0L;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject phoneObject = jsonArray.getJSONObject(i);
            phone = phoneObject.getLong("number");
            if (new BigDecimal(phoneObject.getString("buy_price")).intValue() <= settings.get().getMaxPhonePrice() && phones.add(phone)) {
                break;
            }
        }

        return phone;
    }

    /**
     резервирует номер для покупки
     */
    public boolean reserveNumberAPI(long phoneNumber) {
        // URL для POST запроса
        String url = "https://restapi.plusofon.ru/api/v1/number/reserve";

        // Тело запроса (JSON)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("number", phoneNumber);
        requestBody.put("owner_type", "personal");
        requestBody.put("short", 1);
        System.out.println(requestBody);

        // Заголовки запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(settings.get().getToken());
        headers.set("Client", "10553");

        // Объединение тела и заголовков в HttpEntity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Выполнение POST запроса
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
        System.out.println(response);

        JSONObject jsonObject = new JSONObject(response.getBody());
        // Возвращение ответа как строки
        return jsonObject.getBoolean("success");
    }


    /**
     покупает зарезервированные для покупки номера
     */
    public boolean buyNumberAPI(long phoneNumber) {
        // URL для POST запроса
        String url = "https://restapi.plusofon.ru/api/v1/number/buy";

        // Тело запроса (JSON)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("numbers", Collections.singletonList(phoneNumber));
        requestBody.put("owner_type", "personal");

        // Заголовки запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(settings.get().getToken());
        headers.set("Client", "10553");

        // Объединение тела и заголовков в HttpEntity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Выполнение POST запроса
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());
        if (jsonObject.getBoolean("success")) {
            smsMap.put(String.format("7%d", phoneNumber), new LinkedList<SMS>());
            return true;
        }
        return false;
    }


    /**
     устанавливает для номера WebHook (адрес на который будет
     приходить POST запрос с СМС на данный номер)
     */
    public boolean addWebHookAPI(long phoneNumber) {
        if (phoneNumber < 70000000000L) {
            phoneNumber = 70000000000L + phoneNumber;
        }
        // URL для POST запроса
        String url = "https://restapi.plusofon.ru/api/v1/sms/url";

        // Тело запроса (JSON)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("number", String.valueOf(phoneNumber));
        requestBody.put("url", settings.get().getUrl() + "/sms-webhook-listener");

        // Заголовки запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(settings.get().getToken());
        headers.set("Client", "10553");

        // Объединение тела и заголовков в HttpEntity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Выполнение POST запроса
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());
        // Возвращение ответа как строки
        return jsonObject.getBoolean("success");
    }


    /**
     Метод позволяет получить список всех номеров
     с подключенной услугой обмена SMS-сообщениями.
     Обычно это мобильные номера
     */
    public JSONObject sendAllSMSNumberAPI() {
        String url = "https://restapi.plusofon.ru/api/v1/number/sms";

        // Установка заголовков
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set("Client", "10553");
        headers.setBearerAuth(settings.get().getToken());  // Добавляем Bearer токен

        // Объект запроса
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Отправка GET-запроса
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // Возвращаем тело ответа
        return new JSONObject(response.getBody());
    }

    /**
     Получает для указанного номера ID.
     Работает в связке с методом sendAllSMSNumberAPI()
     Это делается посредством получения полного списка купленных номеров,
     далее среди них находим объект с нашим номером, и у этого объекта
     получаем ID.
     */
    public boolean setPhoneID(Phone phone) {
        JSONArray jsonArray = sendAllSMSNumberAPI().getJSONArray("data");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject phoneObject = jsonArray.getJSONObject(i);
            if (phone.getPhoneNumber().toString().equals(phoneObject.getString("number"))) {
                phone.setId(phoneObject.getLong("id"));
                return true;
            }
        }
        return false;
    }




}
