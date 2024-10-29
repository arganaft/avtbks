package avtobuks.frontend;

import avtobuks.AccountList;
import avtobuks.db.buks.Buks;
import avtobuks.db.buks.BuksService;
import avtobuks.db.profile.Profile;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Route(value = "buks", layout = MainLayout.class)
public class BuksView extends VerticalLayout {

    private final Grid<Buks> buksGrid;
    private final BuksService buksService;
    private AccountList accountList;
    private Set<String> buksNames;

    public BuksView(BuksService buksService, AccountList accountList) {
        this.buksService = buksService;
        this.accountList = accountList;
        buksNames = new HashSet<>();
        for (Buks buks : buksService.findAll()) {
            buksNames.add(buks.getBuksName());
        }

        add(new H2("Буксы"));

        buksGrid = new Grid<>(Buks.class);
        buksGrid.setItems(buksService.findAll()); // Загрузить все записи из базы данных
        buksGrid.setColumns("buksName", "channelLoadMbps", "maxAccount", "cashoutMin", "cashoutMax", "extensions", "perfect_canvas");
        buksGrid.getColumnByKey("perfect_canvas").setFlexGrow(1).setWidth("400px"); // Увеличить размер столбца

        // Добавьте форму для добавления новых записей
        add(createForm());

        add(buksGrid);


    }

    private Component createForm() {
        TextField buksName = new TextField("Buks Name");
        IntegerField channelLoadMbps = new IntegerField("Channel Load (Mbps)");
        IntegerField maxAccount = new IntegerField("maxAccount");
        maxAccount.setHelperText("Максимальное количество аккаунтов которое можно регистрировать в день");
        IntegerField cashoutMin = new IntegerField("Cash out Min");
        IntegerField cashoutMax = new IntegerField("Cash out Max");
        TextArea perfectCanvas = new TextArea("Perfect Canvas");
        TextArea extensions = new TextArea("extensions");
        extensions.setHelperText("Можно добавлять несколько ID расширений через разделитель - Enter (новая строка)");

        Button saveButton = new Button("Save", click -> {
            String Name = buksName.getValue();
            if (buksNames.contains(buksName)) {
                Notification.show("Букс с таким именем уже существует", 3000, Notification.Position.MIDDLE);
            } else {
                Buks newBuks = new Buks(
                        buksName.getValue(),
                        channelLoadMbps.getValue(),
                        maxAccount.getValue(),
                        LocalDate.now(),
                        0,
                        cashoutMin.getValue(),
                        cashoutMax.getValue(),
                        perfectCanvas.getValue(),
                        extensions.getValue());
                buksService.save(newBuks);
                accountList.addNewBuks(newBuks);
                buksGrid.setItems(buksService.findAll()); // Обновить данные в Grid
                }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(buksName, channelLoadMbps, maxAccount, cashoutMin, cashoutMax, perfectCanvas, extensions, saveButton);

        return formLayout;
    }
}
