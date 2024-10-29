package avtobuks.frontend;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "delete", layout = MainLayout.class)
public class DeletionView extends VerticalLayout {
    public DeletionView() {
        add(new H2("Удаление"));

        // Добавьте содержимое представления
    }
}
