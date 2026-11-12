package Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Dao.ClientsDao;
import Dao.Dao;
import Dao.OrdersDao;
import Dao.PCDao;
import Dao.RoomTypesDao;
import Dao.RoomsDao;
import Dao.TariffDao;
import Pojo.Clients;
import Pojo.Orders;
import Pojo.PC;
import Pojo.Prices;
import Pojo.RoomTypes;
import Pojo.Rooms;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import utils.DateFields;
import utils.Texts;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class pcPrologViewController {
    private static final Logger logger = LoggerFactory.getLogger(pcPrologViewController.class);

    public Label lbRoom;
    public Label lbPc;
    public Label lbCurrentEnd;
    public DatePicker dpEnd;
    public Spinner<Integer> spEnd;
    public Spinner<Integer> spEndMin;
    public Label lbCurrentCost;
    public Label lbFutureCost;
    public Label lbTotalCost;
    public Button btnSave;

    private final Dao<Prices, Long> pricesDao = new TariffDao();
    private final Dao<RoomTypes, Long> typeDao = new RoomTypesDao();
    private final Dao<Rooms, Long> roomDao = new RoomsDao();
    private final Dao<PC, Long> pcDao = new PCDao();
    private final Dao<Clients, Long> clientDao = new ClientsDao();
    private final Dao<Orders, Long> orderDao = new OrdersDao();

    private Orders order;
    private long roomTypeId;
    private Stage dialogStage;
    private boolean timeAlertOpen;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    void initialize() {
        DateFields.setup(dpEnd);
        spEnd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        spEndMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        setupTimeSpinner(spEnd);
        setupTimeSpinner(spEndMin);
        dpEnd.valueProperty().addListener((obs, oldValue, newValue) -> calculateCost());
        spEnd.valueProperty().addListener((obs, oldValue, newValue) -> calculateCost());
        spEndMin.valueProperty().addListener((obs, oldValue, newValue) -> calculateCost());
    }

    public void setOrder(Orders order) {
        this.order = order;
        try {
            PC pc = pcDao.findById(order.getPcId());
            if (pc == null) {
                lbRoom.setText("-");
                lbPc.setText("-");
            } else {
                lbPc.setText(String.valueOf(pc.getNumberPC()));
                Rooms room = roomDao.findById(pc.getRoomId());
                if (room == null) {
                    lbRoom.setText("-");
                } else {
                    roomTypeId = room.getTypeId();
                    RoomTypes type = typeDao.findById(room.getTypeId());
                    String typeName = type == null ? "" : type.getName();
                    lbRoom.setText((typeName + " " + room.getNumber()).trim());
                }
            }
        } catch (Exception e) {
            lbRoom.setText(Texts.get("status.error"));
            lbPc.setText(Texts.get("status.error"));
        }

        LocalDateTime currentEnd = order.getDateTimeEnd();
        lbCurrentEnd.setText(currentEnd == null ? "-" : currentEnd.format(formatter));
        lbCurrentCost.setText(formatMoney(order.getCost()));

        if (currentEnd != null) {
            LocalDateTime proposed = currentEnd.plusHours(1);
            dpEnd.setValue(proposed.toLocalDate());
            spEnd.getValueFactory().setValue(proposed.getHour());
            spEndMin.getValueFactory().setValue(proposed.getMinute());
        }
        calculateCost();
    }

    public void onSave(ActionEvent actionEvent) {
        logger.info("onSave");
        try {
            commitTime(spEnd);
            commitTime(spEndMin);
            if (order == null || order.getDateTimeEnd() == null) {
                return;
            }
            LocalDateTime newEnd = readNewEnd();
            if (newEnd == null) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.pickNewEnd")).showAndWait();
                return;
            }
            if (!newEnd.isAfter(order.getDateTimeEnd())) {
                new Alert(Alert.AlertType.ERROR, Texts.get("alert.endMustBeLater")).showAndWait();
                return;
            }
            if (futureHours(newEnd) <= 0) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.prolongMustBeLater")).showAndWait();
                return;
            }

            int startHour = order.getDateTimeEnd().getHour();
            if (startHour >= 22 || startHour < 6) {
                Clients client = clientDao.findById(order.getClientId());
                if (client != null && client.getBirthday() != null) {
                    int age = java.time.Period.between(client.getBirthday(), LocalDate.now()).getYears();
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

            if (((OrdersDao) orderDao).isPcBusy(order.getPcId(), order.getDateTimeEnd(), newEnd)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(Texts.get("alert.pcBusyTitle"));
                alert.setHeaderText(Texts.get("alert.pcBusyProlong"));
                alert.setContentText(Texts.get("alert.pcBusyChoose"));
                alert.showAndWait();
                return;
            }

            Prices tariff = findSuitableTariff(roomTypeId, startHour);
            if (tariff == null) {
                new Alert(Alert.AlertType.ERROR, Texts.get("alert.noProlongTariff")).showAndWait();
                return;
            }

            double total = order.getCost() + futureHours(newEnd) * tariff.getPrice();
            order.setDateTimeEnd(newEnd);
            order.setCost(total);
            orderDao.update(order);

            if (dialogStage != null) {
                dialogStage.close();
            }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    private void calculateCost() {
        if (order == null) {
            return;
        }
        double current = order.getCost();
        lbCurrentCost.setText(formatMoney(current));

        LocalDateTime newEnd = readNewEnd();
        if (newEnd == null || order.getDateTimeEnd() == null || !newEnd.isAfter(order.getDateTimeEnd())) {
            lbFutureCost.setText(formatMoney(0));
            lbTotalCost.setText(formatMoney(current));
            return;
        }

        try {
            int startHour = order.getDateTimeEnd().getHour();
            Prices tariff = findSuitableTariff(roomTypeId, startHour);
            if (tariff == null) {
                lbFutureCost.setText(Texts.get("status.noTariff"));
                lbTotalCost.setText(Texts.get("status.noTariff"));
                return;
            }
            double future = futureHours(newEnd) * tariff.getPrice();
            lbFutureCost.setText(formatMoney(future));
            lbTotalCost.setText(formatMoney(current + future));
        } catch (Exception e) {
            lbFutureCost.setText(Texts.get("status.error"));
            lbTotalCost.setText(Texts.get("status.error"));
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

    private LocalDateTime readNewEnd() {
        if (dpEnd.getValue() == null || spEnd.getValue() == null || spEndMin.getValue() == null) {
            return null;
        }
        int hour = spEnd.getValue();
        int minute = spEndMin.getValue();
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }
        return dpEnd.getValue().atTime(hour, minute);
    }

    private double futureHours(LocalDateTime newEnd) {
        long minutes = java.time.Duration.between(order.getDateTimeEnd(), newEnd).toMinutes();
        return minutes / 60.0;
    }

    private Prices findSuitableTariff(long typeId, int startHour) throws Exception {
        String tariffType = (startHour >= 22 || startHour < 6) ? "Ночной" : "Дневной";
        Collection<Prices> allPrices = pricesDao.findAll();
        for (Prices price : allPrices) {
            if (price.getTypeId() == typeId && price.getTariff().equalsIgnoreCase(tariffType)) {
                return price;
            }
        }
        return null;
    }

    private String formatMoney(double value) {
        return String.format("%.2f", value);
    }
}
