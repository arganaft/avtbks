package avtobuks.frontend;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "errors", layout = MainLayout.class)
public class ErrorsView extends VerticalLayout {
    public ErrorsView() {
        add(new H2("Ошибки"));

        // Добавьте содержимое представления
    }
}
