package org.example;

import Dao.ClientsDao;
import Models.Clients;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
    @DisplayName("Поиск клиента по фамилии")
    void testFindClient() throws Exception {
        long stamp = System.currentTimeMillis();
        String target = "Findov" + stamp;
        String other = "Otherov" + stamp;
        Clients first = dao.save(new Clients(0, target, "Alex", "", "79001110001", LocalDate.of(2000, 1, 2)));
        Clients second = dao.save(new Clients(0, other, "Petr", "", "79001110002", LocalDate.of(2000, 1, 2)));
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
    void testEmptySurname() throws Exception {
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
    @DisplayName("Нельзя сохранить клиента без даты рождения")
    void testBirthdayRequired() throws Exception {
        clickOn("Клиенты");
        clickOn("#btnClientsAdd");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfSurname").tryQuery().isPresent());
        clickOn("#tfSurname").write("Ivanov");
        clickOn("#tfName").write("Ivan");
        clickOn("#tfNumberPhone").write("79001234567");
        clickOn("#btnSave");

        waitFor(3, TimeUnit.SECONDS, () -> lookup(".alert").tryQuery().isPresent());
        DialogPane pane = lookup(".dialog-pane").query();
        assertThat(pane.getContentText()).contains("рождени");
        clickOn(lookup(".alert .button").queryButton());
        assertThat(lookup("#dtBirthday").tryQuery()).isPresent();
        closeClientDialog();
    }

    @Test
    @DisplayName("В телефон нельзя ввести буквы")
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

    @Test
    @DisplayName("Изменение клиента")
    void testEditClient() throws Exception {
        long stamp = System.currentTimeMillis();
        String surname = "Editov" + stamp;
        Clients created = dao.save(new Clients(0, surname, "Old", "", "7900" + (stamp % 10000000), LocalDate.of(2000, 1, 2)));
        idsToClean.add(created.getClientId());

        clickOn("Клиенты");
        clickOn("#btnClientsClear");
        sleep(300, TimeUnit.MILLISECONDS);
        clickOn("#tfClientsSearch").write(surname);
        clickOn("#btnClientsSearch");
        sleep(400, TimeUnit.MILLISECONDS);

        TableView<Clients> table = lookup("#tblClients").query();
        int index = indexOf(table, surname);
        assertThat(index).isGreaterThanOrEqualTo(0);
        interact(() -> table.getSelectionModel().select(index));
        clickOn("#btnClientsEdit");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfName").tryQuery().isPresent());
        clickOn("#tfName").push(javafx.scene.input.KeyCode.CONTROL, javafx.scene.input.KeyCode.A).write("New");
        clickOn("#btnSave");
        sleep(400, TimeUnit.MILLISECONDS);

        Clients updated = dao.findById(created.getClientId());
        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getSurname()).isEqualTo(surname);
    }

    @Test
    @DisplayName("Удаление клиента")
    void testDeleteClient() throws Exception {
        long stamp = System.currentTimeMillis();
        String surname = "Delov" + stamp;
        Clients created = dao.save(new Clients(0, surname, "Gone", "", "7901" + (stamp % 10000000), LocalDate.of(2000, 1, 2)));

        clickOn("Клиенты");
        clickOn("#btnClientsClear");
        sleep(300, TimeUnit.MILLISECONDS);
        clickOn("#tfClientsSearch").write(surname);
        clickOn("#btnClientsSearch");
        sleep(400, TimeUnit.MILLISECONDS);

        TableView<Clients> table = lookup("#tblClients").query();
        int index = indexOf(table, surname);
        assertThat(index).isGreaterThanOrEqualTo(0);
        interact(() -> table.getSelectionModel().select(index));
        clickOn("#btnClientsDelete");
        sleep(400, TimeUnit.MILLISECONDS);

        assertThat(indexOf(lookup("#tblClients").query(), surname)).isEqualTo(-1);
        assertThat(dao.findById(created.getClientId())).isNull();
    }

    @Test
    @DisplayName("Очистка поиска показывает обоих клиентов")
    void testClearSearch() throws Exception {
        long stamp = System.currentTimeMillis();
        String target = "Clearov" + stamp;
        String other = "Stayov" + stamp;
        idsToClean.add(dao.save(new Clients(0, target, "Ann", "", "7902" + (stamp % 10000000), LocalDate.of(2000, 1, 2))).getClientId());
        idsToClean.add(dao.save(new Clients(0, other, "Bob", "", "7903" + (stamp % 10000000), LocalDate.of(2000, 1, 2))).getClientId());

        clickOn("Клиенты");
        clickOn("#btnClientsClear");
        sleep(200, TimeUnit.MILLISECONDS);
        clickOn("#tfClientsSearch").write(target);
        clickOn("#btnClientsSearch");
        sleep(300, TimeUnit.MILLISECONDS);
        clickOn("#btnClientsClear");
        sleep(400, TimeUnit.MILLISECONDS);

        TableView<Clients> table = lookup("#tblClients").query();
        assertThat(indexOf(table, target)).isGreaterThanOrEqualTo(0);
        assertThat(indexOf(table, other)).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Переключение языка на английский и обратно")
    void testLanguageSwitch() throws Exception {
        ChoiceBox<String> language = lookup("#cbLang").query();
        interact(() -> language.setValue("English"));
        waitFor(3, TimeUnit.SECONDS, () -> lookup("Clients").tryQuery().isPresent());
        assertThat(lookup("#llbConnectBD").queryLabeled().getText()).isEqualTo("Connected");

        ChoiceBox<String> restored = lookup("#cbLang").query();
        interact(() -> restored.setValue("Русский"));
        waitFor(3, TimeUnit.SECONDS, () -> lookup("Клиенты").tryQuery().isPresent());
        assertThat(lookup("#llbConnectBD").queryLabeled().getText()).isEqualTo("Подключено");
    }

    @Test
    @DisplayName("Статус подключения к базе")
    void testDatabaseStatus() {
        assertThat(lookup("#llbConnectBD").queryLabeled().getText()).isEqualTo("Подключено");
        assertThat(lookup("#lbTineServer").queryLabeled().getText()).contains(".");
        assertThat(lookup("#lblUser").queryLabeled().getText()).isNotBlank();
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
