package avtobuks.frontend;
import avtobuks.db.cookieSites.CookieSites;
import avtobuks.db.cookieSites.CookieSitesService;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.util.List;

@Route(value = "sites", layout = MainLayout.class)
public class CookieSitesView extends VerticalLayout {

    private final CookieSitesService cookieSitesService;
    Grid<CookieSites> grid;

    public CookieSitesView(CookieSitesService cookieSitesService) {
        this.cookieSitesService = cookieSitesService;
        this.grid = new Grid<>(CookieSites.class);

        add(new H2("Сайты для нагула cookies"));
        grid.setColumns("url", "li_rank", "google", "yandex", "useCount");
        List<CookieSites> cookieSites = cookieSitesService.getAllCookieSites();
        grid.setItems(cookieSites);

        grid.addComponentColumn(this::createDeleteButton)
                .setHeader("Удалить")
                .setFlexGrow(0)
                .setWidth("100px")
                .setTextAlign(ColumnTextAlign.CENTER);

        add(grid);
    }

    private Button createDeleteButton(CookieSites cookieSite) {
        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addClickListener(e -> {
            cookieSitesService.deleteCookieSite(cookieSite.getUrl());
            ((ListDataProvider<CookieSites>) grid.getDataProvider()).getItems().remove(cookieSite);
            grid.getDataProvider().refreshAll();
        });
        return deleteButton;
    }
}
