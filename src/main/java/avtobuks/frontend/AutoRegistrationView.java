package avtobuks.frontend;

import avtobuks.AccountList;
import avtobuks.db.account.Account;
import avtobuks.db.account.AccountService;
import avtobuks.db.botSettings.BotSettingsService;
import avtobuks.db.buks.Buks;
import avtobuks.db.buks.BuksService;
import avtobuks.db.profile.Profile;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Route(value = "auto-registration", layout = MainLayout.class)
public class AutoRegistrationView extends VerticalLayout {

    private BotSettingsService settings;

    @Autowired
    public AutoRegistrationView(AccountService accountService, BotSettingsService settings) {
        this.settings = settings;
        add(new H2("Авто регистрация"));

        HashMap<Profile, List<Account>> autoRegMap = new HashMap<>();

        for (Account account : accountService.findAll()) {
            if (account.getAccountWarmupDays() < settings.get().getAccountWarmupDays()) {
                Profile profile = account.getProfile();
                if (autoRegMap.containsKey(profile)) {
                    autoRegMap.get(profile).add(account);
                } else {
                    autoRegMap.put(profile, new LinkedList<>());
                    autoRegMap.get(profile).add(account);
                }
            }
        }

        for (Profile profile : autoRegMap.keySet()) {
            VerticalLayout profileContainer = new VerticalLayout();
            profileContainer.add(new H3("Профиль: " + profile.getNickname() + " Сервер: " + profile.getServer()));

            for (Account account : autoRegMap.get(profile)) {
                HorizontalLayout accountContainer = new HorizontalLayout();
                accountContainer.add(new Span("Букс: " + account.getBuks().getBuksName()));
                accountContainer.add(new Span("Этап: " + calculateStage(account)));
                profileContainer.add(accountContainer);
            }
            add(profileContainer);
        }

    }

    private String calculateStage(Account account) {
        if (account.getBrowserFarmDays() < settings.get().getCookieSetDays()) {
            return "1) Нагул cookies: " + account.getBrowserFarmDays() + " из " + settings.get().getCookieSetDays();
        } else if (account.getCredentials() == null) {
            return "2) Предстоит регистрация аккаунта: ";
        } else if (account.getAccountWarmupDays() < settings.get().getAccountWarmupDays()) {
            return "3) Разогрев аккаунта: " + account.getAccountWarmupDays() + " из " + settings.get().getAccountWarmupDays();
        }
        return "Все этапы пройдены";
    }
}
