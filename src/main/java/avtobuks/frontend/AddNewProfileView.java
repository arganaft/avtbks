package avtobuks.frontend;

import avtobuks.AccountList;
import avtobuks.db.botSettings.BotSettingsService;
import avtobuks.db.phone.Phone;
import avtobuks.db.phone.PhoneService;
import avtobuks.db.profile.Profile;
import avtobuks.db.profile.ProfileService;
import avtobuks.db.proxy.Proxy;
import avtobuks.db.proxy.ProxyManager;
import avtobuks.db.proxy.ProxyRepository;
import avtobuks.phoneManager.PhoneManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Route(value = "add-profile", layout = MainLayout.class)
public class AddNewProfileView extends VerticalLayout {

    private  ProfileService profileService;
    private final Grid<Profile> profileGrid = new Grid<>(Profile.class);
    private final Binder<Profile> binder = new Binder<>(Profile.class);
    private Set<String> nicknames;
    private ProxyManager proxyManager;
    private final Set<Proxy> noProfileProxySet = new HashSet<>();
    private final Set<Phone> noProfilePhoneSet = new HashSet<>();
    private ProxyRepository proxyRepository;
    private PhoneManager phoneManager;
    private BotSettingsService settings;
    private PhoneService phoneService;



    public AddNewProfileView(ProfileService profileService, AccountList accountList,
                             ProxyManager proxyManager, ProxyRepository proxyRepository,
                             PhoneService phoneService, PhoneManager phoneManager, BotSettingsService botSettingsService) {
        this.profileService = profileService;
        this.proxyManager = proxyManager;
        this.proxyRepository = proxyRepository;
        this.phoneManager = phoneManager;
        this.settings = botSettingsService;
        this.phoneService = phoneService;
        nicknames = new HashSet<>();
        noProfileProxySet.addAll(proxyRepository.findAll());
        noProfilePhoneSet.addAll(phoneService.findAll());
        for (Profile profile : profileService.findAll()) {
            nicknames.add(profile.getNickname());
            noProfileProxySet.remove(profile.getProxy());
            noProfilePhoneSet.remove(profile.getPhone());
        }

        add(new H2("Добавление новых профилей"));

        // Создаем поля ввода
        TextField nicknameField = new TextField("Nickname");
        DatePicker birthDateField = new DatePicker("Birth Date");
        ComboBox<String> genderComboBox = new ComboBox<>("Пол");
        genderComboBox.setItems("Мужской", "Женский");
        TextField nameField = new TextField("Name");
        TextField surnameField = new TextField("Surname");
        TextField patronymicField = new TextField("Patronymic");
        TextField vkUserUrlField = new TextField("VK User URL");
        TextArea accountPhotoField = new TextArea("Account Photo");



        // Кнопка для добавления нового профиля
        Button addButton = new Button("Add Profile", event -> {
            String nickname = nicknameField.getValue();

            if (nicknames.contains(nickname)) {
                Notification.show("Профиль с таким ником уже существует", 3000, Notification.Position.MIDDLE);
            } else {
                Proxy proxy = null;
                Phone phone = null;
                try {
                    phone = getPhone();
                    Iterator<Proxy> iterator = noProfileProxySet.iterator();
                    proxy = iterator.hasNext() ? iterator.next() : proxyManager.getNewProxy();
                    noProfileProxySet.remove(proxy);
                    Notification.show("Добавили не занятый прокси", 3000, Notification.Position.MIDDLE);
                } catch (InterruptedException | JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                if (proxy != null && phone != null) {
                    Profile profile = new Profile();

                    if (binder.writeBeanIfValid(profile)) {
                        profile.setProxy(proxy);
                        profile.setPhone(phone);
                        profile.setIsBlocked(false);
                        profile.setLoad(0);
                        profileService.saveProfile(profile);
                        accountList.addNewProfile(profile);
                        updateProfileGrid();
                        nicknames.add(profile.getNickname());
                        Notification.show("Добавлен новый профиль с новым прокси", 3000, Notification.Position.MIDDLE);
                    }
                } else {
                    Notification.show("Не удалось создать профиль, попробуйте позже", 3000, Notification.Position.MIDDLE);
                }


            }
        });

        // Биндим поля ввода с объектом Profile
        binder.forField(nicknameField).asRequired("Required").bind(Profile::getNickname, Profile::setNickname);
        binder.forField(birthDateField).asRequired("Required").bind(Profile::getBirthDate, Profile::setBirthDate);
        binder.forField(genderComboBox).asRequired("Required").bind(Profile::getGender, Profile::setGender);
        binder.forField(nameField).asRequired("Required").bind(Profile::getName, Profile::setName);
        binder.forField(surnameField).asRequired("Required").bind(Profile::getSurname, Profile::setSurname);
        binder.forField(patronymicField).asRequired("Required").bind(Profile::getPatronymic, Profile::setPatronymic);
        binder.forField(vkUserUrlField).asRequired("Required").bind(Profile::getVkUserUrl, Profile::setVkUserUrl);
        binder.forField(accountPhotoField).asRequired("Required").bind(Profile::getAccountPhoto, Profile::setAccountPhoto);

        // Сетка для отображения профилей с пустым сервером
        profileGrid.setColumns("nickname", "name", "surname", "birthDate", "vkUserUrl");
        updateProfileGrid();

        add(nicknameField, birthDateField, genderComboBox, nameField, surnameField, patronymicField, vkUserUrlField, accountPhotoField, addButton, profileGrid);
    }

    private void updateProfileGrid() {
        List<Profile> profiles = profileService.getAllNullServer();
        profileGrid.setItems(profiles);
    }

    private Phone getPhone() throws JsonProcessingException {
        Phone phone = null;
        Iterator<Phone> iterator = noProfilePhoneSet.iterator();
        if (iterator.hasNext()) {
            phone = iterator.next();
            noProfilePhoneSet.remove(phone);
            Notification.show("Добавили не занятый телефон", 3000, Notification.Position.MIDDLE);
        } else {
            long number = 0;
            int count = 0;
            while (count < 3) {
                number = phoneManager.searchNumberAPI();
                if (number == 0) {
                    Notification.show("Не удалось найти номер для покупки, попытка № " + count + 1, 3000, Notification.Position.MIDDLE);
                    count ++;
                } else {
                    Notification.show("Удалось получить номер для покупки", 3000, Notification.Position.MIDDLE);
                    count = 0;
                    break;
                }
            }
            boolean isReserved = false;
            if (number != 0) {
                while (count < 3) {
                    isReserved = phoneManager.reserveNumberAPI(number);
                    if (isReserved) {
                        Notification.show("Удалось зарезервировать номер для покупки", 3000, Notification.Position.MIDDLE);
                        count = 0;
                        break;
                    } else {
                        Notification.show("Не удалось зарезервировать номер для покупки, попытка № " + count + 1, 3000, Notification.Position.MIDDLE);
                        count ++;
                    }
                }
            }
            boolean isBought = false;
            if (isReserved) {
                while (count < 3) {
                    isBought = phoneManager.buyNumberAPI(number);
                    if (isBought) {
                        Notification.show("Удалось купить номер", 3000, Notification.Position.MIDDLE);
                        count = 0;
                        break;
                    } else {
                        Notification.show("Не удалось купить номер, попытка № " + count + 1, 3000, Notification.Position.MIDDLE);
                        count ++;
                    }
                }

            }
            phone = new Phone(number, LocalDate.now());

        }
        return phoneService.save(phone);
    }


}
