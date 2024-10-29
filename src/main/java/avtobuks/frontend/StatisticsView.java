package avtobuks.frontend;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "statistics", layout = MainLayout.class)
public class StatisticsView extends VerticalLayout {
    public StatisticsView() {
        add(new H2("Статистика"));
        // Добавьте содержимое представления
    }
}
