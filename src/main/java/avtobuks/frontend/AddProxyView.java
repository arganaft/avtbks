package avtobuks.frontend;


import avtobuks.db.proxy.Proxy;
import avtobuks.db.proxy.ProxyRepository;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

//TODO доделать класс и добавить на главную страницу
//TODO добавить в при добавлении нового профиля проверять на наличие не занятых прокси и использовать их
@Route(value = "add-proxy", layout = MainLayout.class)
public class AddProxyView extends VerticalLayout {

    private final ProxyRepository proxyRepository;
    private final Set<Proxy> proxySet;

    private TextField proxyAddress = new TextField("Proxy Address");
    private TextField ipId = new TextField("IP Id");
    private TextField socks5Port = new TextField("Socks5 Port");
    private TextField login = new TextField("Login");
    private TextField password = new TextField("Password");
    private TextField country = new TextField("Country");
    private TextField dateStart = new TextField("Start Date (YYYY-MM-DD)");
    private TextField dateEnd = new TextField("End Date (YYYY-MM-DD)");

    private Button saveButton = new Button("Save");
    private Grid<Proxy> proxyGrid = new Grid<>(Proxy.class);

    @Autowired
    public AddProxyView(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
        proxySet = new HashSet<>();
        proxySet.addAll(proxyRepository.findAll());

        // Создание формы
        FormLayout formLayout = new FormLayout();
        formLayout.add(proxyAddress, ipId, socks5Port, login, password, country, dateStart, dateEnd, saveButton);

        // Настройка таблицы
        proxyGrid.setColumns("proxyAddress", "ipId", "socks5Port", "login", "password", "country", "dateStart", "dateEnd");
        proxyGrid.setItems(proxySet);

        // Обработка нажатия на кнопку "Save"
        saveButton.addClickListener(event -> {
            try {
                Proxy newProxy = new Proxy();
                newProxy.setProxyAddress(proxyAddress.getValue());
                newProxy.setIpId(ipId.getValue());
                newProxy.setSocks5Port(socks5Port.getValue());
                newProxy.setLogin(login.getValue());
                newProxy.setPassword(password.getValue());
                newProxy.setCountry(country.getValue());
                newProxy.setDateStart(LocalDate.parse(dateStart.getValue()));
                newProxy.setDateEnd(LocalDate.parse(dateEnd.getValue()));

                if (proxySet.contains(newProxy)) {
                    Notification.show("Этот прокси уже содержится  в базе данных");
                } else {
                    proxyRepository.save(newProxy);
                    proxyGrid.setItems(proxyRepository.findAll());
                    proxySet.add(newProxy);

                    Notification.show("Прокси добавлен в базу данных");
                }


            } catch (Exception e) {
                Notification.show("Error: " + e.getMessage());
            }
        });

        // Добавление компонентов на страницу
        add(formLayout, proxyGrid);
    }
}

