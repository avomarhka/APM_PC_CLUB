package org.example;

import Dao.PCDao;
import Dao.RoomsDao;
import Dao.RoomTypesDao;
import Dao.TariffDao;
import Models.PC;
import Models.Prices;
import Models.Rooms;
import Models.RoomTypes;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class ScreensUiTest extends AppTestBase {
    private final RoomsDao roomsDao = new RoomsDao();
    private final RoomTypesDao typesDao = new RoomTypesDao();
    private final TariffDao tariffDao = new TariffDao();
    private final PCDao pcDao = new PCDao();
    private final List<Long> roomIds = new ArrayList<>();
    private final List<Long> typeIds = new ArrayList<>();
    private final List<Long> priceIds = new ArrayList<>();
    private final List<Long> pcIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (Long id : pcIds) {
            try {
                pcDao.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        for (Long id : priceIds) {
            try {
                tariffDao.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        for (Long id : roomIds) {
            try {
                roomsDao.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        for (Long id : typeIds) {
            try {
                typesDao.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        pcIds.clear();
        priceIds.clear();
        roomIds.clear();
        typeIds.clear();
    }

    @Test
    @DisplayName("Продление и завершение без выбранного сеанса")
    void testPcActionsNeedSelection() throws Exception {
        clickOn("Компьютеры");
        clickOn("#btnPcProlong");
        assertAlertContains("заказ");
        clickOn("#btnPcShutDown");
        assertAlertContains("заказ");
    }

    @Test
    @DisplayName("Фильтры компьютеров и сброс кнопкой Обновить")
    void testPcFiltersAndCounters() {
        clickOn("Компьютеры");
        int total = Integer.parseInt(lookup("#lbPcAll").queryLabeled().getText());
        int free = Integer.parseInt(lookup("#lbPcFree").queryLabeled().getText());
        assertThat(total).isGreaterThanOrEqualTo(free);
        assertThat(free).isGreaterThanOrEqualTo(0);

        clickOn("#chbPCAvailable");
        clickOn("#chnPcBooked");
        sleep(300, TimeUnit.MILLISECONDS);
        assertThat(lookup("#chbPCAvailable").<CheckBox>query().isSelected()).isTrue();
        assertThat(lookup("#chnPcBooked").<CheckBox>query().isSelected()).isTrue();

        clickOn("#btnPcUpdate");
        sleep(300, TimeUnit.MILLISECONDS);
        assertThat(lookup("#chbPCAvailable").<CheckBox>query().isSelected()).isFalse();
        assertThat(lookup("#chnPcBooked").<CheckBox>query().isSelected()).isFalse();
    }

    @Test
    @DisplayName("Новый заказ с компьютеров без данных не сохраняется")
    void testPcNewOrderValidation() throws Exception {
        clickOn("Компьютеры");
        clickOn("#btnPCNewOrder");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#chbRoom").tryQuery().isPresent());
        clickOn("#btnSave");
        assertAlertContains("клиента");
        closeDialog("#chbRoom");
    }

    @Test
    @DisplayName("Заказ без комнаты и клиента не создаётся")
    void testOrderNeedsRoomAndClient() throws Exception {
        clickOn("Заказы");
        clickOn("#btnOrderNew");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#btnSave").tryQuery().isPresent());
        clickOn("#btnSave");
        assertAlertContains("клиента");
        closeDialog("#btnSave");
    }

    @Test
    @DisplayName("Неверная дата в фильтре заказов")
    void testBadOrderDate() throws Exception {
        clickOn("Заказы");
        DatePicker picker = lookup("#dtpOrder").query();
        clickOn(picker.getEditor());
        push(KeyCode.CONTROL, KeyCode.A);
        write("32.13.2026");
        type(KeyCode.ENTER);
        assertAlertContains("дату");
    }

    @Test
    @DisplayName("Поиск заказа по несуществующей строке и очистка")
    void testOrderSearchAndClear() {
        clickOn("Заказы");
        sleep(300, TimeUnit.MILLISECONDS);
        int before = Integer.parseInt(lookup("#lblOrderCount").queryLabeled().getText());

        clickOn("#tfOrderSearch").write("missing-order-" + System.currentTimeMillis());
        clickOn("#btnOrderSearch");
        sleep(400, TimeUnit.MILLISECONDS);
        assertThat(lookup("#lblOrderCount").queryLabeled().getText()).isEqualTo("0");

        clickOn("#btnOrderCancelSearch");
        sleep(400, TimeUnit.MILLISECONDS);
        assertThat(lookup("#tfOrderSearch").<TextField>query().getText()).isEmpty();
        int after = Integer.parseInt(lookup("#lblOrderCount").queryLabeled().getText());
        assertThat(after).isGreaterThanOrEqualTo(before);
    }

    @Test
    @DisplayName("Скрытие завершенных не увеличивает список заказов")
    void testHideCompleted() {
        clickOn("Заказы");
        sleep(300, TimeUnit.MILLISECONDS);
        int before = Integer.parseInt(lookup("#lblOrderCount").queryLabeled().getText());
        clickOn("#cbOrderHide");
        sleep(400, TimeUnit.MILLISECONDS);
        int after = Integer.parseInt(lookup("#lblOrderCount").queryLabeled().getText());
        assertThat(after).isLessThanOrEqualTo(before);
    }

    @Test
    @DisplayName("Выбор клиента без строки показывает предупреждение")
    void testPickClientWithoutSelection() throws Exception {
        clickOn("Заказы");
        clickOn("#btnOrderNew");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#btnFioChoice").tryQuery().isPresent());
        clickOn("#btnFioChoice");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#btnSelect").tryQuery().isPresent());
        clickOn("#btnSelect");
        assertAlertContains("клиента");
        closeDialog("#tfSearch");
        closeDialog("#btnFioChoice");
    }

    @Test
    @DisplayName("Нельзя добавить компьютер, пока не выбрана комната")
    void testAddPcWithoutRoom() throws Exception {
        clickOn("Справочник");
        interact(() -> lookup("#tblGuidePC").query().requestFocus());
        clickOn("#btnGuideAdd");
        assertAlertContains("комнату");
    }

    @Test
    @DisplayName("Лимит компьютеров по типу комнаты")
    void testPcLimit() throws Exception {
        clickOn("Справочник");
        TableView<Rooms> rooms = lookup("#tblGuideRoom").query();
        assertThat(rooms.getItems()).isNotEmpty();
        interact(() -> {
            rooms.getSelectionModel().selectFirst();
            rooms.requestFocus();
        });
        sleep(300, TimeUnit.MILLISECONDS);
        interact(() -> lookup("#tblGuidePC").query().requestFocus());
        TableView<?> pcTable = lookup("#tblGuidePC").query();
        int before = pcTable.getItems().size();
        Rooms room = rooms.getSelectionModel().getSelectedItem();
        RoomTypes type = typesDao.findById(room.getTypeId());
        int limit = type == null ? 0 : type.getPcCount();

        clickOn("#btnGuideAdd");
        if (limit <= 0 || before >= limit) {
            assertAlertContains(limit <= 0 ? "количество" : "больше");
            return;
        }
        sleep(400, TimeUnit.MILLISECONDS);
        TableView<PC> pcs = lookup("#tblGuidePC").query();
        assertThat(pcs.getItems()).hasSize(before + 1);
        PC created = pcs.getItems().get(pcs.getItems().size() - 1);
        pcIds.add(created.getPcId());
    }

    @Test
    @DisplayName("Пустое название типа и неверный лимит")
    void testTypeValidation() throws Exception {
        clickOn("Справочник");
        clickOn("#btnGuideAllTypes");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfTypes").tryQuery().isPresent());
        clickOn("#btnAdd");
        assertAlertContains("типа");

        clickOn("#tfTypes").write("Type" + System.currentTimeMillis());
        clickOn("#tfCount").write("ноль");
        clickOn("#btnAdd");
        assertAlertContains("нуля");
        closeDialog("#tfTypes");
    }

    @Test
    @DisplayName("Добавление и удаление типа комнаты")
    void testAddAndDeleteType() throws Exception {
        String name = "Type" + System.currentTimeMillis();
        clickOn("Справочник");
        clickOn("#btnGuideAllTypes");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfTypes").tryQuery().isPresent());
        clickOn("#tfTypes").write(name);
        clickOn("#tfCount").write("3");
        clickOn("#btnAdd");
        sleep(400, TimeUnit.MILLISECONDS);

        TableView<RoomTypes> table = lookup("#tblTypes").query();
        int index = -1;
        for (int i = 0; i < table.getItems().size(); i++) {
            if (name.equals(table.getItems().get(i).getName())) {
                index = i;
                typeIds.add(table.getItems().get(i).getId());
                break;
            }
        }
        assertThat(index).isGreaterThanOrEqualTo(0);
        int row = index;
        interact(() -> table.getSelectionModel().select(row));
        clickOn("#btnDelete");
        sleep(400, TimeUnit.MILLISECONDS);
        boolean stillThere = false;
        for (RoomTypes item : lookup("#tblTypes").<TableView<RoomTypes>>query().getItems()) {
            if (name.equals(item.getName())) {
                stillThere = true;
            }
        }
        assertThat(stillThere).isFalse();
        typeIds.clear();
        closeDialog("#tfTypes");
    }

    @Test
    @DisplayName("Тариф без цены не сохраняется")
    void testTariffNeedsPrice() throws Exception {
        clickOn("Справочник");
        clickOn("#btnGuideAllTariff");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfTariff").tryQuery().isPresent());
        clickOn("#tfTariff").write("Broken");
        clickOn("#tfPrices").write("abc");
        clickOn("#btnAdd");
        sleep(300, TimeUnit.MILLISECONDS);
        assertThat(lookup("#tfTariff").tryQuery()).isPresent();
        closeDialog("#tfTariff");
    }

    @Test
    @DisplayName("Добавление тарифа")
    void testAddTariff() throws Exception {
        String name = "Tariff" + System.currentTimeMillis();
        clickOn("Справочник");
        clickOn("#btnGuideAllTariff");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#chbTypes").tryQuery().isPresent());
        ChoiceBox<RoomTypes> types = lookup("#chbTypes").query();
        assertThat(types.getItems()).isNotEmpty();
        interact(() -> types.getSelectionModel().selectFirst());
        clickOn("#tfTariff").write(name);
        clickOn("#tfPrices").write("15.5");
        clickOn("#btnAdd");
        sleep(500, TimeUnit.MILLISECONDS);

        Prices created = null;
        for (Prices price : tariffDao.findAll()) {
            if (name.equals(price.getTariff())) {
                created = price;
                break;
            }
        }
        assertThat(created).isNotNull();
        priceIds.add(created.getId());
        assertThat(created.getPrice()).isEqualTo(15.5);
    }

    @Test
    @DisplayName("Добавление комнаты")
    void testAddRoom() throws Exception {
        long number = 80000 + (System.currentTimeMillis() % 10000);
        clickOn("Справочник");
        TableView<Rooms> rooms = lookup("#tblGuideRoom").query();
        assertThat(rooms.getItems()).isNotEmpty();
        interact(() -> {
            rooms.getSelectionModel().selectFirst();
            rooms.requestFocus();
        });
        clickOn("#btnGuideAdd");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfNumber").tryQuery().isPresent());
        ChoiceBox<RoomTypes> types = lookup("#chbType").query();
        interact(() -> types.getSelectionModel().selectFirst());
        clickOn("#tfNumber").write(String.valueOf(number));
        clickOn("#btnSave");
        sleep(500, TimeUnit.MILLISECONDS);

        Rooms created = null;
        for (Rooms room : roomsDao.findAll()) {
            if (room.getNumber() == number) {
                created = room;
                break;
            }
        }
        assertThat(created).isNotNull();
        roomIds.add(created.getRoomId());
        boolean shown = false;
        for (Rooms room : lookup("#tblGuideRoom").<TableView<Rooms>>query().getItems()) {
            if (room.getNumber() == number) {
                shown = true;
            }
        }
        assertThat(shown).isTrue();
    }
}
