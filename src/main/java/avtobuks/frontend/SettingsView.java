package avtobuks.frontend;

import avtobuks.AccountList;
import avtobuks.db.botSettings.BotSettingsService;
import avtobuks.db.phone.Phone;
import avtobuks.db.phone.PhoneService;
import avtobuks.phoneManager.PhoneManager;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Route(value = "settings", layout = MainLayout.class)
public class SettingsView extends VerticalLayout {
    private final BotSettingsService settings;
    private final AccountList accountList;
    private PhoneManager phoneManager;
    private PhoneService phoneService;
    private Map<String, Phone> phoneMap;

    public SettingsView(BotSettingsService botSettingsService, AccountList accountList, PhoneManager phoneManager, PhoneService phoneService) {
        this.settings = botSettingsService;
        this.accountList = accountList;
        this.phoneManager = phoneManager;
        this.phoneService = phoneService;
        this.phoneMap = new HashMap<>();
        for (Phone phone : phoneService.findAll()) {
            String number = phone.getPhoneNumber().toString();
            if (!phoneMap.containsKey(number)) {
                phoneMap.put(number, phone);
            }
        }

        add(new H2("Параметры работы"));

        // Create form layout
        FormLayout formLayout = new FormLayout();

        // Add fields
        TextField tokenField = new TextField("Ключ доступа к API с сайта plusofon");
        tokenField.setValue(settings.get().getToken());
        tokenField.addValueChangeListener(e -> settings.get().setToken(e.getValue()));

        IntegerField maxPhonePriceField = new IntegerField("максимальная цена для покупки телефонного номера");
        maxPhonePriceField.setValue(settings.get().getMaxPhonePrice());
        maxPhonePriceField.setHelperText("можно узнать на сайте плюсофон");
        maxPhonePriceField.addValueChangeListener(e -> settings.get().setMaxPhonePrice(e.getValue()));

        TextField urlField = new TextField("URL адрес моего сервера в интернете");
        urlField.setValue(settings.get().getUrl());
        urlField.setHelperText("нужен для телефонного сервиса для получения WebHook с смс");
        urlField.addValueChangeListener(e -> settings.get().setUrl(e.getValue()));

        TextField workStartTimeField = new TextField("Время начала работы (в формате чч:мм)");
        workStartTimeField.setValue(settings.get().getWorkStartTime().toString());
        workStartTimeField.addValueChangeListener(e -> settings.get().setWorkStartTime(LocalTime.parse(e.getValue())));

        TextField creepjsRatingField = new TextField("Минимальный рейтинг Creepjs для фингерпринта в %");
        creepjsRatingField.setValue(settings.get().getCreepjsRating());
        creepjsRatingField.addValueChangeListener(e -> settings.get().setCreepjsRating(e.getValue()));

        TextField xevilField = new TextField("сервер XEvil");
        xevilField.setValue(settings.get().getXevilServer());
        xevilField.setHelperText("нужен роботам для решения капчи");
        xevilField.addValueChangeListener(e -> settings.get().setXevilServer(e.getValue()));

        IntegerField maxProfileOnServerField = new IntegerField("максимум профилей на сервер");
        maxProfileOnServerField.setValue(settings.get().getMaxProfileOnServer());
        maxProfileOnServerField.setHelperText("Рекомендуемая величина - 50");
        maxProfileOnServerField.addValueChangeListener(e -> settings.get().setMaxProfileOnServer(e.getValue()));

        IntegerField cookieSetDaysField = new IntegerField("дней для нагула cookies для профиля браузера");
        cookieSetDaysField.setValue(settings.get().getCookieSetDays());
        cookieSetDaysField.addValueChangeListener(e -> settings.get().setCookieSetDays(e.getValue()));

        IntegerField googleSitesCountField = new IntegerField("количество сайтов для нагула cookies google в день");
        googleSitesCountField.setValue(settings.get().getGoogleSitesCount());
        googleSitesCountField.addValueChangeListener(e -> settings.get().setGoogleSitesCount(e.getValue()));

        IntegerField yandexSitesCountField = new IntegerField("количество сайтов для нагула cookies yandex в день");
        yandexSitesCountField.setValue(settings.get().getYandexSitesCount());
        yandexSitesCountField.addValueChangeListener(e -> settings.get().setYandexSitesCount(e.getValue()));

        IntegerField accountWarmupDaysField = new IntegerField("дней на разогрев аккаунта");
        accountWarmupDaysField.setValue(settings.get().getAccountWarmupDays());
        accountWarmupDaysField.addValueChangeListener(e -> settings.get().setAccountWarmupDays(e.getValue()));

        IntegerField minRestTimeMinutesField = new IntegerField("минимальное время для отдыха после работы в минутах");
        minRestTimeMinutesField.setValue(settings.get().getMinRestTimeMinutes());
        minRestTimeMinutesField.addValueChangeListener(e -> settings.get().setMinRestTimeMinutes(e.getValue()));

        IntegerField maxRestTimeMinutesField = new IntegerField("максимальное время для отдыха после работы в минутах");
        maxRestTimeMinutesField.setValue(settings.get().getMaxRestTimeMinutes());
        maxRestTimeMinutesField.addValueChangeListener(e -> settings.get().setMaxRestTimeMinutes(e.getValue()));

        IntegerField maxDailyWorkMinsField = new IntegerField("максимум минут работы в день");
        maxDailyWorkMinsField.setValue(settings.get().getMaxDailyWorkMins());
        maxDailyWorkMinsField.addValueChangeListener(e -> settings.get().setMaxDailyWorkMins(e.getValue()));

        IntegerField maxSessionWorkMinsField = new IntegerField("максимальная длительность сессий в минутах");
        maxSessionWorkMinsField.setValue(settings.get().getMaxSessionWorkMins());
        maxSessionWorkMinsField.addValueChangeListener(e -> settings.get().setMaxSessionWorkMins(e.getValue()));

        IntegerField minSessionWorkMinsField = new IntegerField("минимальная длительность сессий в минутах");
        minSessionWorkMinsField.setValue(settings.get().getMinSessionWorkMins());
        minSessionWorkMinsField.addValueChangeListener(e -> settings.get().setMinSessionWorkMins(e.getValue()));

        IntegerField minSessionsPerDayField = new IntegerField("минимум сессий в день");
        minSessionsPerDayField.setValue(settings.get().getMinSessionsPerDay());
        minSessionsPerDayField.addValueChangeListener(e -> settings.get().setMinSessionsPerDay(e.getValue()));

        IntegerField maxSessionsPerDayField = new IntegerField("максимум сессий в день");
        maxSessionsPerDayField.setValue(settings.get().getMaxSessionsPerDay());
        maxSessionsPerDayField.addValueChangeListener(e -> settings.get().setMaxSessionsPerDay(e.getValue()));

        // Add more fields for other properties...

        formLayout.add(
                tokenField,
                maxPhonePriceField,
                urlField,
                workStartTimeField,
                creepjsRatingField,
                xevilField,
                maxProfileOnServerField,
                cookieSetDaysField,
                googleSitesCountField,
                yandexSitesCountField,
                accountWarmupDaysField,
                minRestTimeMinutesField,
                maxRestTimeMinutesField,
                maxDailyWorkMinsField,
                maxSessionWorkMinsField,
                minSessionWorkMinsField,
                minSessionsPerDayField,
                maxSessionsPerDayField
                // Add more fields here...
        );

        // Add save button
        Button saveButton = new Button("Сохранить", e -> {
            botSettingsService.saveSettings(settings.get());
            accountList.expandServers();
            Notification.show("Настройки сохранены");
        });

        // Получить все номера из сервиса plusofon
        Button getAllNumbersButton = new Button("Получить все номера", e -> {
            JSONObject allNumberObject =  phoneManager.sendAllSMSNumberAPI();
            JSONArray numbersArray = allNumberObject.getJSONArray("data");
            for (int i = 0; i < numbersArray.length(); i++) {
                JSONObject numberObject = numbersArray.getJSONObject(i);
                String number = numberObject.getString("number");
                if (!phoneMap.containsKey(number)) {
                    Phone phone = new Phone();
                    phone.setPhoneNumber(Long.valueOf(number));
                    phone.setId(numberObject.getLong("id"));
                    phone.setPurchaseDate(LocalDate.now());
                    phoneService.save(phone);
                    phoneMap.put(number, phone);
                } else {
                    Phone phone = phoneMap.get(number);
                    phone.setId(numberObject.getLong("id"));
                    phoneService.save(phone);
                }
            }
            Notification.show("Все номера обновлены");
        });

        // Обновляет WebHook для всех номеров
        Button updateWebHookButton = new Button("Обновить WebHook", e -> {
            int all = 0;
            int update = 0;
            int noUpdate = 0;
            for (String number : phoneMap.keySet()) {
                if (phoneManager.addWebHookAPI(Long.parseLong(number))) {
                    update ++;
                } else {
                    noUpdate ++;
                }
                all++;
            }
            String result = String.format("Успешно = %d, не успешно = %d, всего = %d", update, noUpdate, all);
            Notification.show(result, 10000, Notification.Position.MIDDLE);
        });

        add(saveButton, getAllNumbersButton, updateWebHookButton, formLayout);
    }
}
