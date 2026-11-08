package org.example;

import Dao.ClientsDao;
import Dao.OrdersDao;
import Dao.PCDao;
import Dao.RoomsDao;
import Dao.RoomTypesDao;
import Dao.TariffDao;
import Models.Clients;
import Models.Orders;
import Models.PC;
import Models.Prices;
import Models.Rooms;
import Models.RoomTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.DBHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class DaoCoverageTest {
    private static final ClientsDao clientsDao = new ClientsDao();
    private static final OrdersDao ordersDao = new OrdersDao();
    private static final PCDao pcDao = new PCDao();
    private static final RoomsDao roomsDao = new RoomsDao();
    private static final RoomTypesDao typesDao = new RoomTypesDao();
    private static final TariffDao tariffDao = new TariffDao();

    @BeforeAll
    static void connect() {
        DBHelper.initConnection("appuser", "pass");
    }

    @Test
    @DisplayName("Клиент: сохранение, поиск, изменение")
    void clientSaveSearchUpdate() throws Exception {
        long stamp = System.currentTimeMillis();
        String surname = "Dao" + stamp;
        Clients client = clientsDao.save(new Clients(0, surname, "Vera", "Ivanovna", "7999" + (stamp % 1000000), LocalDate.of(2000, 1, 2)));
        try {
            assertThat(client.getClientId()).isPositive();
            Clients loaded = clientsDao.findById(client.getClientId());
            assertThat(loaded.getSurname()).isEqualTo(surname);
            assertThat(loaded.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 2));

            assertThat(clientsDao.search(surname)).anyMatch(item -> item.getClientId() == client.getClientId());
            assertThat(clientsDao.searchByPhone("7999" + (stamp % 1000000)))
                    .anyMatch(item -> item.getClientId() == client.getClientId());
            assertThat(clientsDao.search("no-such-" + stamp)).noneMatch(item -> surname.equals(item.getSurname()));

            loaded.setName("Vera2");
            clientsDao.update(loaded);
            assertThat(clientsDao.findById(client.getClientId()).getName()).isEqualTo("Vera2");
        } finally {
            clientsDao.deleteById(client.getClientId());
            assertThat(clientsDao.findById(client.getClientId())).isNull();
        }
    }

    @Test
    @DisplayName("Комната, компьютер, тариф и заказ удаляются каскадом")
    void cascadeFromTypeRoomPcAndClient() throws Exception {
        long stamp = System.currentTimeMillis();
        RoomTypes type = null;
        Rooms room = null;
        PC pc = null;
        Clients client = null;
        long extraClientId = 0;
        try {
            type = new RoomTypes();
            type.setName("DaoType" + stamp);
            type.setPcCount(2);
            typesDao.save(type);

            room = new Rooms();
            room.setTypeId(type.getId());
            room.setNumber(90000 + (stamp % 1000));
            roomsDao.save(room);

            pc = new PC();
            pc.setRoomId(room.getRoomId());
            pc.setNumberPC(1);
            pc.setAvailable(true);
            pcDao.save(pc);

            Prices price = new Prices();
            price.setTariff("Дневной" + stamp);
            price.setPrice(120.0);
            price.setTypeId(type.getId());
            tariffDao.save(price);

            client = clientsDao.save(new Clients(0, "Orderov" + stamp, "Oleg", "", "7888" + (stamp % 1000000), LocalDate.of(1990, 5, 5)));

            LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(12).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusHours(1);
            Orders order = new Orders();
            order.setClientId(client.getClientId());
            order.setPcId(pc.getPcId());
            order.setDateTimeStart(start);
            order.setDateTimeEnd(end);
            order.setCost(120);
            order.setOrderNumber("DAO-" + stamp);
            ordersDao.save(order);

            long savedPcId = pc.getPcId();
            long savedRoomId = room.getRoomId();
            long savedTypeId = type.getId();
            long savedPriceId = price.getId();
            assertThat(pcDao.findById(savedPcId).getRoomId()).isEqualTo(savedRoomId);
            assertThat(pcDao.findByRoomId(savedRoomId)).anyMatch(item -> item.getPcId() == savedPcId);
            assertThat(roomsDao.findById(savedRoomId).getNumber()).isEqualTo(room.getNumber());
            assertThat(typesDao.findById(savedTypeId).getPcCount()).isEqualTo(2);
            assertThat(tariffDao.findByRoomId(savedTypeId)).anyMatch(item -> item.getId() == savedPriceId);
            assertThat(ordersDao.search("DAO-" + stamp)).anyMatch(item -> item.getOrderId() == order.getOrderId());
            assertThat(ordersDao.isPcBusy(pc.getPcId(), start.plusMinutes(10), end.plusMinutes(10))).isTrue();
            assertThat(ordersDao.isPcBusy(pc.getPcId(), end.plusHours(3), end.plusHours(4))).isFalse();
            assertThat(ordersDao.getTodayOrderCount()).isGreaterThanOrEqualTo(0);

            order.setCost(150);
            ordersDao.update(order);
            Orders updated = findOrder(order.getOrderId());
            assertThat(updated.getCost()).isEqualTo(150);

            pc.setAvailable(false);
            pcDao.update(pc);
            assertThat(pcDao.findById(pc.getPcId()).getAvailable()).isFalse();

            type.setPcCount(4);
            typesDao.update(type);
            assertThat(typesDao.findById(type.getId()).getPcCount()).isEqualTo(4);

            clientsDao.deleteById(client.getClientId());
            client = null;
            assertThat(findOrder(order.getOrderId())).isNull();
            assertThat(pcDao.findById(pc.getPcId())).isNotNull();

            Clients second = clientsDao.save(new Clients(0, "Second" + stamp, "Petr", "", "7777" + (stamp % 1000000), LocalDate.of(2000, 1, 2)));
            extraClientId = second.getClientId();
            Orders again = new Orders();
            again.setClientId(extraClientId);
            again.setPcId(pc.getPcId());
            again.setDateTimeStart(start.plusHours(5));
            again.setDateTimeEnd(end.plusHours(5));
            again.setCost(10);
            again.setOrderNumber("DAO2-" + stamp);
            ordersDao.save(again);

            pcDao.deleteById(pc.getPcId());
            pc = null;
            assertThat(findOrder(again.getOrderId())).isNull();
            clientsDao.deleteById(extraClientId);
            extraClientId = 0;

            PC replacement = new PC();
            replacement.setRoomId(room.getRoomId());
            replacement.setNumberPC(2);
            replacement.setAvailable(true);
            pcDao.save(replacement);
            roomsDao.deleteById(room.getRoomId());
            room = null;
            assertThat(pcDao.findById(replacement.getPcId())).isNull();

            long typeId = type.getId();
            long priceId = price.getId();
            typesDao.deleteById(typeId);
            type = null;
            assertThat(typesDao.findById(typeId)).isNull();
            assertThat(tariffDao.findAll()).noneMatch(item -> item.getId() == priceId);
        } finally {
            if (extraClientId != 0) {
                clientsDao.deleteById(extraClientId);
            }
            if (type != null && type.getId() != 0) {
                typesDao.deleteById(type.getId());
            }
            if (room != null && room.getRoomId() != 0) {
                roomsDao.deleteById(room.getRoomId());
            }
            if (pc != null && pc.getPcId() != 0) {
                pcDao.deleteById(pc.getPcId());
            }
            if (client != null && client.getClientId() != 0) {
                clientsDao.deleteById(client.getClientId());
            }
        }
    }

    private static Orders findOrder(long id) throws Exception {
        for (Orders order : ordersDao.findAll()) {
            if (order.getOrderId() == id) {
                return order;
            }
        }
        return null;
    }
}
