package avtobuks.frontend;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;

@CssImport("./styles/shared-styles.css")
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Мое приложение");
        logo.addClassNames("text-l", "m-m");

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        addToDrawer(new VerticalLayout(
                createNavigation()
        ));
    }

    private Component createNavigation() {
        // Vertical navigation items
        Tabs tabs = new Tabs();
        tabs.add(
                createTab(VaadinIcon.DASHBOARD, "Статистика", StatisticsView.class),
                createTab(VaadinIcon.OUTBOX, "Данные аккаунтов", AccountDataView.class),
                createTab(VaadinIcon.USER_CHECK, "Авто регистрация", AutoRegistrationView.class),
                createTab(VaadinIcon.USER_CHECK, "Добавить профиль", AddNewProfileView.class),
                createTab(VaadinIcon.FACTORY, "Буксы", BuksView.class),
                createTab(VaadinIcon.TRASH, "Удаление", DeletionView.class),
                createTab(VaadinIcon.COG, "Параметры работы", SettingsView.class),
                createTab(VaadinIcon.GLOBE, "Сайты для нагула cookies", CookieSitesView.class),
                createTab(VaadinIcon.SPARK_LINE, "Прокси", AddProxyView.class),
                createTab(VaadinIcon.WARNING, "Ошибки", ErrorsView.class)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        return tabs;
    }

    private Tab createTab(VaadinIcon viewIcon, String viewName, Class<? extends Component> viewClass) {
        Icon icon = viewIcon.create();
        icon.getStyle().set("box-sizing", "border-box")
                .set("margin-inline-end", "var(--lumo-space-m)")
                .set("margin-inline-start", "var(--lumo-space-xs)")
                .set("padding", "var(--lumo-space-xs)");

        RouterLink link = new RouterLink();
        link.add(icon, new Span(viewName));
        link.setRoute(viewClass);
        link.setTabIndex(-1);

        return new Tab(link);
    }
}
