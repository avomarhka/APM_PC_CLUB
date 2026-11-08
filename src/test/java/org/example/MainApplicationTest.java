package org.example;

import Dao.ClientsDao;
import Models.Clients;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import utils.DBHelper;
import utils.Texts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class MainApplicationTest extends ApplicationTest {
    private final ClientsDao dao = new ClientsDao();
    private final List<Long> idsToClean = new ArrayList<>();

    @Override
    public void start(Stage stage) throws Exception {
        Texts.apply(new Locale("ru", "RU"));
        DBHelper.initConnection("appuser", "pass");
        Parent root = Texts.loader("/mainView.fxml").load();
        stage.setScene(new Scene(root, 1100, 700));
        stage.show();
    }

    @AfterEach
    void cleanUp() {
        for (Long id : new ArrayList<>(idsToClean)) {
            try {
                dao.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        idsToClean.clear();
    }

    @Test
    @DisplayName("Добавление клиента")
    void testAddClient() throws Exception {
        String surname = "Testov" + System.currentTimeMillis();
        String name = "Test";
        String phone = String.valueOf(System.currentTimeMillis() % 10000000000L);

        clickOn("Клиенты");
        clickOn("#btnClientsAdd");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfSurname").tryQuery().isPresent());
        clickOn("#tfSurname").write(surname);
        clickOn("#tfName").write(name);
        clickOn("#tfNumberPhone").write(phone);
        clickOn("#btnSave");
        sleep(400, TimeUnit.MILLISECONDS);

        TableView<Clients> table = lookup("#tblClients").query();
        boolean found = false;
        for (Clients client : table.getItems()) {
            if (surname.equals(client.getSurname())
                    && name.equals(client.getName())
                    && phone.equals(client.getNumberPhone())) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();

        Clients created = dao.search(surname).stream()
                .filter(client -> surname.equals(client.getSurname()) && name.equals(client.getName()))
                .findFirst()
                .orElse(null);
        assertThat(created).isNotNull();
        idsToClean.add(created.getClientId());
    }

    @Test
    @DisplayName("Поиск клиента по фамилии")
    void testFindClient() throws Exception {
        long stamp = System.currentTimeMillis();
        String target = "Findov" + stamp;
        String other = "Otherov" + stamp;
        Clients first = dao.save(new Clients(0, target, "Alex", "", "79001110001", null));
        Clients second = dao.save(new Clients(0, other, "Petr", "", "79001110002", null));
        idsToClean.add(first.getClientId());
        idsToClean.add(second.getClientId());

        clickOn("Клиенты");
        clickOn("#btnClientsClear");
        sleep(300, TimeUnit.MILLISECONDS);
        clickOn("#tfClientsSearch").write(target);
        clickOn("#btnClientsSearch");
        sleep(400, TimeUnit.MILLISECONDS);

        TableView<Clients> table = lookup("#tblClients").query();
        boolean foundTarget = false;
        boolean foundOther = false;
        for (Clients client : table.getItems()) {
            if (target.equals(client.getSurname())) {
                foundTarget = true;
            }
            if (other.equals(client.getSurname())) {
                foundOther = true;
            }
        }
        assertThat(foundTarget).isTrue();
        assertThat(foundOther).isFalse();
    }

    @Test
    @DisplayName("Нельзя сохранить клиента без фамилии")
    void testEmptySurname() {
        clickOn("Клиенты");
        clickOn("#btnClientsAdd");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfName").tryQuery().isPresent());
        clickOn("#tfName").write("Ivan");
        clickOn("#tfNumberPhone").write("79001234567");
        clickOn("#btnSave");

        waitFor(3, TimeUnit.SECONDS, () -> lookup(".alert").tryQuery().isPresent());
        DialogPane pane = lookup(".dialog-pane").query();
        assertThat(pane.getContentText()).contains("фамилию");
        clickOn(lookup(".alert .button").queryButton());
        assertThat(lookup("#tfSurname").tryQuery()).isPresent();
        closeClientDialog();
    }

    @Test
    @DisplayName("В телефон нельзя ввести буквы")
    void testPhoneMustBeDigits() {
        clickOn("Клиенты");
        clickOn("#btnClientsAdd");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfSurname").tryQuery().isPresent());
        clickOn("#tfSurname").write("Ivanov");
        clickOn("#tfName").write("Ivan");
        clickOn("#tfNumberPhone").write("abc");
        clickOn("#btnSave");

        waitFor(3, TimeUnit.SECONDS, () -> lookup(".alert").tryQuery().isPresent());
        DialogPane pane = lookup(".dialog-pane").query();
        assertThat(pane.getContentText()).contains("цифры");
        Button ok = lookup(".alert .button").queryButton();
        clickOn(ok);
        assertThat(lookup("#tfNumberPhone").tryQuery()).isPresent();
        closeClientDialog();
    }

    private void closeClientDialog() {
        interact(() -> ((Stage) lookup("#tfSurname").query().getScene().getWindow()).close());
    }
}
