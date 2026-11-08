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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DateFields;
import utils.Texts;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;


public class OrderViewController {
    private static final Logger logger = LoggerFactory.getLogger(OrderViewController.class);

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
    public Spinner <Integer> spEndMin;
    public Spinner <Integer> spStart;
    public Spinner <Integer> spStartMin;
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
    private boolean timeAlertOpen;

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
            DateFields.setup(dtpStart);
            DateFields.setup(dtEnd);

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
            spStartMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
            spEnd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
            spEndMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
            setupTimeSpinner(spStart);
            setupTimeSpinner(spStartMin);
            setupTimeSpinner(spEnd);
            setupTimeSpinner(spEndMin);


            dtpStart.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            dtEnd.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            spStart.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            spStartMin.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            spEnd.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            spEndMin.valueProperty().addListener((obs, oldV, newV) -> calculateCost());
            chbRoom.valueProperty().addListener((obs, oldV, newV) -> calculateCost());

        } catch (Exception e) {
            logger.error("Ошибка операции", e);
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
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }
    }

    public void onNumberChoice(ActionEvent actionEvent) {
        logger.info("onNumberChoice");
        openClientChoice();
    }

    public void onFioChoice(ActionEvent actionEvent) {
        logger.info("onFioChoice");
        openClientChoice();
    }

    private void openClientChoice() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Texts.bundle());
            loader.setLocation(Main.class.getResource("/orderClientsView.fxml"));
            Parent page = loader.load();

            Stage choiceStage = new Stage();
            choiceStage.setTitle(Texts.get("title.chooseClient"));
            choiceStage.initModality(Modality.APPLICATION_MODAL);
            choiceStage.initOwner(dialogStage != null ? dialogStage : Main.getPrimaryStage());
            choiceStage.setScene(new Scene(page));

            OrderClientsController controller = loader.getController();
            controller.setDialogStage(choiceStage);
            choiceStage.showAndWait();

            Clients client = controller.getSelectedClient();
            if (client != null) {
                applyClient(client);
            }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    private void applyClient(Clients client) {
        selectedClient = client;
        tfFio.setText(formatFio(client));
        tfNumberPhone.setText(client.getNumberPhone() == null ? "" : client.getNumberPhone());
    }

    private String formatFio(Clients client) {
        String surname = client.getSurname() == null ? "" : client.getSurname();
        String name = client.getName() == null ? "" : client.getName();
        String patronymic = client.getPatronymic() == null ? "" : client.getPatronymic();
        return (surname + " " + name + " " + patronymic).trim();
    }

    private Clients findTypedClient() throws Exception {
        String phone = tfNumberPhone.getText() == null ? "" : tfNumberPhone.getText().trim();
        if (!phone.isEmpty()) {
            Collection<Clients> found = ((ClientsDao) clientDao).searchByPhone(phone);
            if (found.size() == 1) {
                return found.iterator().next();
            }
        }
        String fio = tfFio.getText() == null ? "" : tfFio.getText().trim();
        if (!fio.isEmpty()) {
            Collection<Clients> found = clientDao.search(fio);
            if (found.size() == 1) {
                return found.iterator().next();
            }
        }
        return null;
    }
    public void onTfNumberPhone(ActionEvent actionEvent) {
        logger.info("onTfNumberPhone");
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
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }
    }

    public void onTfFio(ActionEvent actionEvent) {
        logger.info("onTfFio");
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
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }
    }
    private void calculateCost() {
        Rooms room = chbRoom.getValue();
        LocalDate startDate = dtpStart.getValue();
        LocalDate endDate = dtEnd.getValue();

        if (room == null || startDate == null || endDate == null || !hasTime()) {
            tfCost.setText("0");
            return;
        }

        try {
            RoomTypes type = typeDao.findById(room.getTypeId());
            Prices tariff = findSuitableTariff(type.getId(), startDate, spStart.getValue());

            if (tariff == null) {
                tfCost.setText(Texts.get("status.noTariff"));
                return;
            }

            LocalDateTime start = toDateTime(startDate, spStart, spStartMin);
            LocalDateTime end = toDateTime(endDate, spEnd, spEndMin);
            if (start == null || end == null) {
                tfCost.setText("0");
                return;
            }
            long minutes = java.time.Duration.between(start, end).toMinutes();

            if (minutes <= 0) {
                tfCost.setText("0");
                return;
            }

            double cost = minutes / 60.0 * tariff.getPrice();
            tfCost.setText(String.format("%.2f", cost));

        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }
    }

    private void setupTimeSpinner(Spinner<Integer> spinner) {
        spinner.setEditable(true);
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
        factory.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                return value == null ? "" : Integer.toString(value);
            }

            @Override
            public Integer fromString(String text) {
                return acceptTime(spinner, text);
            }
        });
        spinner.getEditor().setOnAction(event -> commitTime(spinner));
        spinner.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitTime(spinner);
            }
        });
    }

    private void commitTime(Spinner<Integer> spinner) {
        Integer value = acceptTime(spinner, spinner.getEditor().getText());
        if (value == null) {
            return;
        }
        spinner.getValueFactory().setValue(value);
        spinner.getEditor().setText(String.valueOf(value));
    }

    private Integer acceptTime(Spinner<Integer> spinner, String text) {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
        Integer current = spinner.getValue();
        String raw = text == null ? "" : text.trim();
        if (raw.isEmpty()) {
            restoreTime(spinner, current);
            return current;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value < factory.getMin() || value > factory.getMax()) {
                restoreTime(spinner, current);
                showInvalidTime();
                return current;
            }
            return value;
        } catch (NumberFormatException e) {
            restoreTime(spinner, current);
            showInvalidTime();
            return current;
        }
    }

    private void restoreTime(Spinner<Integer> spinner, Integer current) {
        spinner.getEditor().setText(current == null ? "" : String.valueOf(current));
    }

    private void showInvalidTime() {
        if (timeAlertOpen) {
            return;
        }
        timeAlertOpen = true;
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(Texts.get("alert.badTimeHeader"));
            alert.setContentText(Texts.get("alert.badTimeBody"));
            alert.showAndWait();
        } finally {
            timeAlertOpen = false;
        }
    }

    private boolean hasTime() {
        return spStart.getValue() != null && spStartMin.getValue() != null
                && spEnd.getValue() != null && spEndMin.getValue() != null;
    }

    private LocalDateTime toDateTime(LocalDate date, Spinner<Integer> hours, Spinner<Integer> minutes) {
        if (date == null || hours.getValue() == null || minutes.getValue() == null) {
            return null;
        }
        int hour = hours.getValue();
        int minute = minutes.getValue();
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }
        return date.atTime(hour, minute);
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
        logger.info("onSave");
        try {
            commitTime(spStart);
            commitTime(spStartMin);
            commitTime(spEnd);
            commitTime(spEndMin);

            Rooms selectedRoom = chbRoom.getValue();
            PC selectedPC = chbPC.getValue();
            LocalDate startDate = dtpStart.getValue();
            LocalDate endDate = dtEnd.getValue();

            if (selectedClient == null) {
                selectedClient = findTypedClient();
                if (selectedClient != null) {
                    applyClient(selectedClient);
                }
            }
            if (selectedRoom == null || selectedPC == null || selectedClient == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, Texts.get("alert.pickRoom"));
                alert.showAndWait();
                return;
            }
            if (startDate == null || endDate == null || !hasTime()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, Texts.get("alert.pickDate"));
                alert.showAndWait();
                return;
            }


            LocalDateTime start = toDateTime(startDate, spStart, spStartMin);
            LocalDateTime end = toDateTime(endDate, spEnd, spEndMin);
            if (start == null || end == null) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.badTimeHeader") + ". " + Texts.get("alert.badTimeBody")).showAndWait();
                return;
            }

            if (end.isBefore(start) || end.isEqual(start)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, Texts.get("alert.endAfterStart"));
                alert.showAndWait();
                return;
            }

// --- НОВАЯ ПРОВЕРКА ---
            if (start.isBefore(LocalDateTime.now())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(Texts.get("alert.pastTitle"));
                alert.setHeaderText(Texts.get("alert.pastHeader"));
                alert.setContentText(Texts.get("alert.pastBody"));
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
                        alert.setTitle(Texts.get("alert.ageTitle"));
                        alert.setHeaderText(Texts.get("alert.nightForbidden"));
                        alert.setContentText(Texts.get("alert.under18"));
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
                alert.setTitle(Texts.get("alert.pcBusyTitle"));
                alert.setHeaderText(Texts.get("alert.pcBusyHeader"));
                alert.setContentText(Texts.get("alert.pcBusyOrder"));
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
            logger.error("Ошибка операции", e);
            e.printStackTrace();
            // Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка сохранения: " + e.getMessage());
          // alert.showAndWait();
        }

    }




}


