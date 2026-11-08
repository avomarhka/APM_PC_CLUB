package Controllers;
import Dao.Dao;
import Dao.OrdersDao;
import Dao.ClientsDao;
import Dao.PCDao;
import Dao.RoomsDao;
import Dao.TariffDao;
import Dao.RoomTypesDao;

import Models.*;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;


public class OrderViewController {
    public TextField tfNumberPhone;
    public Button btnNumberChoice;
    public Button btnFioChoice;
    public TextField tfFio;
    public DatePicker dtpStart;
    public ChoiceBox <PC> chbPC;
    public ChoiceBox <Rooms> chbRoom;
    public DatePicker dtEnd;
    public TextField tfCost;
    public Spinner <Integer> spEnd;
    public Spinner <Integer> spStart;
    public Button btnSave;

    private Dao<Prices,Long> pricesDao;
    private Dao<RoomTypes, Long> typeDao;
    private Dao<Rooms, Long> roomDao;
    private Dao<PC, Long> pcDao;
    private Dao<Clients,Long> clientDao;
    private Dao<Orders,Long> orderDao;

    public OrderViewController() {
        this.pricesDao = new TariffDao();
        this.typeDao = new RoomTypesDao();
        this.roomDao = new RoomsDao();
        this.pcDao = new PCDao();
        this.clientDao = new ClientsDao();
        this.orderDao = new OrdersDao();
    }

    private Clients selectedClient;
    private Stage dialogStage;

    private ObservableList<Orders> orders =
          FXCollections.observableArrayList();
//
//    public void setOrders(Orders orders) {
//        this.orders = orders;
//        if (orders != null) {
//            tfSurname.setText(orders.getSurname());
//            tfName.setText(orders.getName());
//            tfPatronymic.setText(orders.getPatronymic());
//            tfNumberPhone.setText(String.valueOf(orders.getNumberPhone()));
//            dtBirthday.setValue(orders.getBirthday());
//        }
//    }


    private ObservableList<Prices> prices =
            FXCollections.observableArrayList();

    public void setAddStage(Stage addStage) {
       this.dialogStage = addStage;
    }


    @FXML
    void initialize() {
        try {

            Collection<Rooms> roomsList = roomDao.findAll();
            chbRoom.getItems().setAll(roomsList);

            // 2. Конвертер для комнат
            chbRoom.setConverter(new javafx.util.StringConverter<Rooms>() {
                @Override
                public String toString(Rooms room) {
                    if (room == null) return "";
                    try {
                        RoomTypes type = typeDao.findById(room.getTypeId());
                        return type.getName() + " " + room.getNumber();
                    } catch (Exception e) {
                        return String.valueOf(room.getNumber());
                    }
                }
                @Override
                public Rooms fromString(String string) { return null; }
            });

            chbRoom.setOnAction(event -> updatePcList());

            spStart.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
            spEnd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));


            dtpStart.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            dtEnd.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            spStart.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            spEnd.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            chbRoom.valueProperty().addListener((obs, oldV, newV) -> calculateCost());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePcList() {
        Rooms selectedRoom = chbRoom.getValue();
        if (selectedRoom == null) {
            chbPC.getItems().clear();
            return;
        }
        try {
            Collection<PC> pcs = pcDao.findByRoomId(selectedRoom.getRoomId());
            chbPC.getItems().setAll(pcs);

            chbPC.setConverter(new javafx.util.StringConverter<PC>() {
                @Override
                public String toString(PC pc) {
                    return pc != null ? String.valueOf(pc.getNumberPC()) : "";
                }
                @Override
                public PC fromString(String string) { return null; }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onNumberChoice(ActionEvent actionEvent) {

    }

    public void onFioChoice(ActionEvent actionEvent) {

    }
    public void onTfNumberPhone(ActionEvent actionEvent) {
        String phone = tfNumberPhone.getText().trim();
        if (phone.isEmpty()) return;

        try {
            Collection<Clients> found = ((ClientsDao) clientDao).searchByPhone(phone);
            if (found.size() == 1) {
                Clients client = found.iterator().next();
                tfFio.setText(client.getSurname() + " " + client.getName() + " " + client.getPatronymic());
                this.selectedClient = client;
            } else if (found.isEmpty()) {
                System.out.println("Клиент не найден");
            } else {
                System.out.println("Найдено несколько клиентов, уточните запрос");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTfFio(ActionEvent actionEvent) {
        String fio = tfFio.getText().trim();
        if (fio.isEmpty()) {
            System.out.println("Введите ФИО для поиска");
            return;
        }

        try {
            Collection<Clients> found = clientDao.search(fio);

            if (found.size() == 1) {
                Clients client = found.iterator().next();

                tfFio.setText(client.getSurname() + " " + client.getName() + " " + client.getPatronymic());

                tfNumberPhone.setText(client.getNumberPhone());
                this.selectedClient = client;
                System.out.println("Клиент найден: " + client.getSurname() + " " + client.getName());
            } else if (found.isEmpty()) {
                System.out.println("Клиент не найден");
            } else {
                System.out.println("Найдено несколько клиентов, уточните запрос");

                for (Clients c : found) {
                    System.out.println(c.getSurname() + " " + c.getName() + " " + c.getPatronymic());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void calculateCost() {
        Rooms room = chbRoom.getValue();
        LocalDate startDate = dtpStart.getValue();
        LocalDate endDate = dtEnd.getValue();

        if (room == null || startDate == null || endDate == null || spStart.getValue() == null || spEnd.getValue() == null) {
            tfCost.setText("0");
            return;
        }

        try {
            RoomTypes type = typeDao.findById(room.getTypeId());
            Prices tariff = findSuitableTariff(type.getId(), startDate, spStart.getValue());

            if (tariff == null) {
                tfCost.setText("Нет тарифа");
                return;
            }

            LocalDateTime start = startDate.atTime(spStart.getValue(), 0);
            LocalDateTime end = endDate.atTime(spEnd.getValue(), 0);
            long hours = java.time.Duration.between(start, end).toHours();

            if (hours <= 0) {
                tfCost.setText("0");
                return;
            }

            double cost = hours * tariff.getPrice();
            tfCost.setText(String.format("%.2f", cost));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Prices findSuitableTariff(long typeId, LocalDate date, int startHour) throws Exception {
        String tariffType = (startHour >= 22 || startHour < 6) ? "Ночной" : "Дневной";

        Collection<Prices> allPrices =  pricesDao.findAll();
        for (Prices p : allPrices) {
            if (p.getTypeId() == typeId && p.getTariff().equalsIgnoreCase(tariffType)) {
                return p;
            }
        }
        return null;
    }

    public void onSave(ActionEvent actionEvent) {
        try {

            Rooms selectedRoom = chbRoom.getValue();
            PC selectedPC = chbPC.getValue();
            LocalDate startDate = dtpStart.getValue();
            LocalDate endDate = dtEnd.getValue();

            if (selectedRoom == null || selectedPC == null || selectedClient == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Выберите комнату, ПК и клиента!");
                alert.showAndWait();
                return;
            }
            if (startDate == null || endDate == null || spStart.getValue() == null || spEnd.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Укажите даты и время начала/окончания!");
                alert.showAndWait();
                return;
            }


            LocalDateTime start = startDate.atTime(spStart.getValue(), 0);
            LocalDateTime end = endDate.atTime(spEnd.getValue(), 0);

            if (end.isBefore(start) || end.isEqual(start)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Дата окончания должна быть позже даты начала!");
                alert.showAndWait();
                return;
            }

// --- НОВАЯ ПРОВЕРКА ---
            if (start.isBefore(LocalDateTime.now())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка даты");
                alert.setHeaderText("Нельзя создать заказ в прошлом");
                alert.setContentText("Выберите дату и время начала не раньше текущего момента.");
                alert.showAndWait();
                return;
            }
// ----------------------


            int startHour = spStart.getValue();
            if (startHour >= 22 || startHour < 6) {
                LocalDate birthday = selectedClient.getBirthday();
                if (birthday != null) {
                    int age = java.time.Period.between(birthday, LocalDate.now()).getYears();
                    if (age < 18) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка возраста");
                        alert.setHeaderText("Ночной тариф запрещен");
                        alert.setContentText("Клиент не достиг 18 лет.");
                        alert.showAndWait();
                        return;
                    }
                }
            }


            double cost = 0.0;
            try {
                String costText = tfCost.getText().replace(",", "."); // <-- заменяем запятую на точку
                cost = Double.parseDouble(costText);
            } catch (NumberFormatException ex) {
                throw new RuntimeException(ex);
            }
            // После проверки возраста и до сохранения:
            if (((OrdersDao) orderDao).isPcBusy(selectedPC.getPcId(), start, end)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ПК занят");
                alert.setHeaderText("Компьютер уже забронирован");
                alert.setContentText("Выберите другое время или другой ПК.");
                alert.showAndWait();
                return;
            }




            Orders newOrder = new Orders();
            newOrder.setClientId(selectedClient.getClientId());
            newOrder.setPcId(selectedPC.getPcId());
            newOrder.setDateTimeStart(start);
            newOrder.setDateTimeEnd(end);
            newOrder.setCost(cost);


            int todayCount = ((OrdersDao) orderDao).getTodayOrderCount() + 1;
            newOrder.setOrderNumber("ORD-" + todayCount);
            System.out.println("clientId = " + selectedClient.getClientId());


            orderDao.save(newOrder);

            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка сохранения: " + e.getMessage());
          // alert.showAndWait();
        }

    }




}


