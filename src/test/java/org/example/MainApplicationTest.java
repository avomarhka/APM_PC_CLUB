package org.example;

import Dao.ClientsDao;
import Pojo.Clients;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class MainApplicationTest extends AppTestBase {
    private final ClientsDao dao = new ClientsDao();
    private final List<Long> idsToClean = new ArrayList<>();

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
    @DisplayName("Позитивный: Добавление клиента")
    void testAddClient() throws Exception {
        String surname = "Testov";
        String name = "Test";
        String phone = "7980546";

        clickOn("Клиенты");
        clickOn("#btnClientsAdd");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfSurname").tryQuery().isPresent());
        clickOn("#tfSurname").write(surname);
        clickOn("#tfName").write(name);
        clickOn("#tfNumberPhone").write(phone);
        DatePicker birthday = lookup("#dtBirthday").query();
        interact(() -> birthday.setValue(LocalDate.of(2000, 1, 2)));
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
    @DisplayName("Негативный: В телефон нельзя ввести буквы")
    void testPhoneMustBeDigits() throws Exception {
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


    private int indexOf(TableView<Clients> table, String surname) {
        for (int i = 0; i < table.getItems().size(); i++) {
            if (surname.equals(table.getItems().get(i).getSurname())) {
                return i;
            }
        }
        return -1;
    }

    private void closeClientDialog() {
        closeDialog("#tfSurname");
    }
}
