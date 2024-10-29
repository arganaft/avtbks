package avtobuks.frontend;

import avtobuks.AccountList;
import avtobuks.db.account.Account;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Route(value = "accaunt_data", layout = MainLayout.class)
public class AccountDataView extends VerticalLayout {
    private final AccountList accountList;

    public AccountDataView(AccountList accountList) {
        this.accountList = accountList;

        // Создание полей ввода
        IntegerField serverField = new IntegerField("сервер");
        TextField buksField = new TextField("букс");

        // Кнопка для вызова функции
        Button executeButton = new Button("Сгенерировать");

        // Лейбл для отображения результата
        TextArea result = new TextArea("Result");
        result.setReadOnly(true);  // Чтобы нельзя было редактировать результат
        result.setWidthFull(); // Устанавливаем ширину на всю доступную область
        result.setHeight("200px"); // Задаём высоту для лучшего отображения

        // Добавление компонентов на страницу
        add(serverField, buksField, executeButton, result);

        // Обработчик кнопки
        executeButton.addClickListener(event -> {
            // Получение введённых строк
            Long server = serverField.getValue().longValue();
            String buks = buksField.getValue();

            // Вызов вашей функции из MyService
            String output = getAccounts(server, buks);

            // Отображение результата
            result.setValue(output);

            // Всплывающее уведомление (по желанию)
            Notification.show("Function executed successfully");
        });
    }

    /**
     * Возвращяет список аккаунов в виде:
     * login:password:ip:port:login_proxy:password_proxy
     * которые использую боты для работы
     *
     * @param server сервер в виде числа
     * @param buks название букса
     * @return список аккаунтов
     */
    private String getAccounts(Long server, String buks) {
        Set<Account> accountSet = accountList.getAccountsByBuksName(server, buks);
        StringBuilder result = new StringBuilder();
        for (Account account : accountSet) {
            if (account.getCredentials() != null) {
                JSONObject jsonObject = new JSONObject(account.getCredentials());
                String credentials = jsonObject.getString("login") + ":" +
                        jsonObject.getString("password") + ":" +
                        account.getProfile().getProxy().getProxyAddress() + ":51523:e1mararganaft:KRALcutaap\n";
                result.append(credentials);
            }

        }
        return result.toString();
    }
}
