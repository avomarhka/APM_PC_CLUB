package Controllers;

import Dao.Dao;
import Dao.OrdersDao;
import Dao.ClientsDao;
import Dao.PCDao;
import Dao.RoomsDao;
import Dao.TariffDao;
import Dao.RoomTypesDao;

import Models.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Alerts;
import org.example.Main;

import utils.DBHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DateFields;
import utils.Texts;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    public Button btnExit;
    public Label lblLanguage;
    public Label lblUser;
    public ChoiceBox cbLang;


    public Button btnGuideAdd;
    public Button btnGuideEdit;
    public Button btnGuideDelete;
    public Button btnGuideAllTypes;
    public Button btnGuideAllTariff;
    public TableView  <Rooms>  tblGuideRoom;
    public TableColumn <Rooms, String> GuideRoomsColumn;
    public TableView <PC>  tblGuidePC;
    public TableColumn <PC, Long> GiudePcColumn;
    public TableColumn <PC, String> GuideAvaliColumn;
    public TableView  <Prices>  tblGuideTariff;
    public TableColumn <Prices, String>GuideTariffColumn;
    public TableColumn<Prices, Number> GuidePriceColumn;

    public Button btnClientsAdd;
    public Button btnClientsEdit;
    public Button btnClientsDelete;
    public TableView <Clients>  tblClients;
    public TableColumn <Clients, String> ClientsFioColumn;
    public TableColumn <Clients, String> ClientsNumberColumn;
    public TableColumn <Clients, String> ClientsBirthdayColumn;
    public TableColumn <Clients, String> ClientsAgeColumn;
    public TextField tfClientsSearch;
    public Button btnClientsSearch;
    public Button btnClientsClear;



    public Button btnOrderUpdate;
    public Button btnOrderNew;
    public DatePicker dtpOrder;
    public TextField tfOrderSearch;
    public Button btnOrderSearch;
    public Button btnOrderCancel;
    public Button btnOrderCancelSearch;
    public TableView<Orders> tblOrder;
    public TableColumn <Orders, String> OrderNumberColumn;
    public TableColumn<Orders, Long> OrderPcColumn;
    public TableColumn<Orders, String> OrderRoomColumn;
    public TableColumn<Orders, String> OrderFioColumn;
    public TableColumn<Orders, String> OrderNumberPhoneColumn;
    public TableColumn <Orders, String>OrderStartColumn;
    public TableColumn<Orders, String> OrderEndColumn;
    public TableColumn<Orders, String> OrderCostColumn;
    public CheckBox cbOrderHide;
    public Label lblOrderCount;
    public Label lblOrderCount1;



    public Label lbPcFree;
    public Label lbPcAll;
    public Label llbConnectBD;
    public Label lbTineServer;
    private Timeline serverClock;
    public CheckBox chbPCAvailable;
    public CheckBox chnPcBooked;
    public TableView<PcTableRow> tblPC;
    public TableColumn<PcTableRow, String> PcRoomColumn;
    public TableColumn<PcTableRow, Long> PcPCColumn;
    public TableColumn<PcTableRow, String> PcAvailable;
    public TableColumn<PcTableRow, String> PcDateStart;
    public TableColumn<PcTableRow, String> PcDateEnd;
    public TableColumn<PcTableRow, String> PcCost;
    public Button btnPcUpdate;
    public Button btnPCNewOrder;
    public Button btnPcProlong;
    public Button btnPcShutDown;
    public DatePicker dpPC;


    private Dao<Clients, Long> clientDao;
    private Dao<PC,Long> pcDao;
    private Dao<Rooms,Long> roomDao;
    private Dao<Prices,Long> priceDao;
    private Dao<RoomTypes, Long> typeDao;
    private Dao<Orders, Long> orderDao;

    public MainController (){
        this.clientDao = new ClientsDao();
        this.pcDao = new PCDao();
        this.roomDao = new RoomsDao();
        this.priceDao = new TariffDao();
        this.typeDao = new RoomTypesDao();
        this.orderDao = new OrdersDao();

    }

    private ObservableList<Rooms> rooms = FXCollections.observableArrayList();
    private ObservableList<PC> pcs = FXCollections.observableArrayList();
    private ObservableList<Prices> prices = FXCollections.observableArrayList();
    private ObservableList<Clients> clients = FXCollections.observableArrayList();
    private ObservableList<Orders> orders = FXCollections.observableArrayList();
    private ObservableList<PcTableRow> currentPcOrders = FXCollections.observableArrayList();
 
    private TableView<?> lastFocusedTable;
    private Rooms lastSelectedRoom;

    private void showRoom(long roomId) {
        try {
            pcs.clear();
            prices.clear();



            List<PC> pcsInRoom = (List<PC>) pcDao.findByRoomId(roomId);
            pcs.addAll(pcsInRoom);
            tblGuidePC.setItems(pcs);
            List<Orders> allOrders = (List<Orders>) orderDao.findAll();
            LocalDateTime now = LocalDateTime.now();
            GiudePcColumn.setCellValueFactory(cellData ->
                    new SimpleLongProperty(cellData.getValue().getNumberPC()).asObject());

            GuideAvaliColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(guideAvailability(cellData.getValue(), allOrders, now)));



            Rooms nowRoom = roomDao.findById(roomId);
            long type = nowRoom.getTypeId();
            List <Prices> pricesInRoom = (List<Prices>) priceDao.findByRoomId(type);
            prices.addAll(pricesInRoom);


            tblGuideTariff.setItems(prices);
            GuideTariffColumn.setCellValueFactory(cellData ->
                    cellData.getValue().tariffProperty());
            GuidePriceColumn.setCellValueFactory(cellData ->
                    cellData.getValue().priceProperty());

            lastSelectedRoom = tblGuideRoom.getSelectionModel().getSelectedItem();

        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }

    }



    private void setupLanguage() {
        cbLang.getItems().setAll("Русский", "English");
        cbLang.setValue("en".equals(Texts.locale().getLanguage()) ? "English" : "Русский");
        cbLang.setOnAction(event -> changeLanguage());
    }

    private void changeLanguage() {
        Object selected = cbLang.getValue();
        if (selected == null) {
            return;
        }
        Locale locale = "English".equals(selected) ? new Locale("en", "EN") : new Locale("ru", "RU");
        if (locale.getLanguage().equals(Texts.locale().getLanguage())) {
            return;
        }
        Texts.apply(locale);
        logger.info("Язык интерфейса изменен на {}", locale);
        try {
            Stage stage = (Stage) cbLang.getScene().getWindow();
            Parent root = Texts.loader("/mainView.fxml").load();
            stage.setTitle(Texts.get("app.title"));
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    private void showDbStatus() {
        boolean connected = false;
        LocalDateTime serverTime = LocalDateTime.now();
        try {
            java.sql.Connection connection = DBHelper.getConnection();
            connected = connection != null && !connection.isClosed();
            if (connected) {
                try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT NOW()");
                     java.sql.ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getTimestamp(1) != null) {
                        serverTime = resultSet.getTimestamp(1).toLocalDateTime();
                    }
                }
            }
        } catch (Exception e) {
            connected = false;
        }
        llbConnectBD.setText(connected ? Texts.get("status.connected") : Texts.get("status.disconnected"));
        startServerClock(serverTime);
    }

    private void startServerClock(LocalDateTime serverTime) {
        java.time.Duration offset = java.time.Duration.between(LocalDateTime.now(), serverTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        if (serverClock != null) {
            serverClock.stop();
        }
        serverClock = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event ->
                lbTineServer.setText(LocalDateTime.now().plus(offset).format(formatter))));
        serverClock.setCycleCount(Animation.INDEFINITE);
        serverClock.play();
        lbTineServer.setText(serverTime.format(formatter));
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    @FXML
    void initialize() throws Exception {
        try{
            showDbStatus();
            lblUser.setText(Main.currentUserName);
            setupLanguage();

            refreshRoom();
            refreshClients();
            refreshOrder();

            pcs.clear();
            DateFields.setup(dtpOrder);
            DateFields.setup(dpPC);
            dtpOrder.setValue(LocalDate.now());
            dpPC.setValue(LocalDate.now());


            PcRoomColumn.setCellValueFactory(cellData -> {
                try {
                    PC pc = cellData.getValue().pc;
                    if (pc == null) {
                        return new SimpleStringProperty("-");
                    }
                    Rooms room = roomDao.findById(pc.getRoomId());
                    if (room == null) {
                        return new SimpleStringProperty("-");
                    }
                    RoomTypes type = typeDao.findById(room.getTypeId());
                    if (type == null) {
                        return new SimpleStringProperty(String.valueOf(room.getNumber()));
                    }
                    return new SimpleStringProperty(type.getName() + " " + room.getNumber());
                } catch (Exception e) {
                    return new SimpleStringProperty(Texts.get("status.error"));
                }
            });
            PcPCColumn.setCellValueFactory(cellData -> {
                PC pc = cellData.getValue().pc;
                if (pc == null) {
                    return new SimpleLongProperty(0).asObject();
                }
                return new SimpleLongProperty(pc.getNumberPC()).asObject();
            });
            PcAvailable.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().status));
            PcDateStart.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatDateTime(cellData.getValue().order == null ? null : cellData.getValue().order.getDateTimeStart())));
            PcDateEnd.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatDateTime(cellData.getValue().order == null ? null : cellData.getValue().order.getDateTimeEnd())));
            PcCost.setCellValueFactory(cellData -> {
                Orders order = cellData.getValue().order;
                if (order == null) {
                    return new SimpleStringProperty("");
                }
                return new SimpleStringProperty(String.valueOf(order.getCost()));
            });

            refreshPc();
            dpPC.valueProperty().addListener((obs, oldDate, newDate) -> refreshPc());




            OrderNumberColumn.setCellValueFactory(cellData ->
                    cellData.getValue().orderNumberProperty());
            OrderRoomColumn.setCellValueFactory(cellData ->{
                long pcId = cellData.getValue().getPcId();
                try {
                    PC pc = pcDao.findById(pcId);
                    Rooms room = roomDao.findById(pc.getRoomId());
                    RoomTypes type = typeDao.findById(room.getTypeId());
                    return new SimpleStringProperty(type.getName() + " " + room.getNumber());
                } catch (Exception e) {
                    return new SimpleStringProperty(Texts.get("status.error"));
                }
            });
            OrderPcColumn.setCellValueFactory(cellData -> {
                long pcId = cellData.getValue().getPcId();
                try {
                    PC pc = pcDao.findById(pcId);
                    return new SimpleLongProperty(pc.getNumberPC()).asObject();
                } catch (Exception e) {
                    return new SimpleLongProperty(0).asObject();
                }
            });
            OrderNumberPhoneColumn.setCellValueFactory(cellData -> {
                long clientId = cellData.getValue().getClientId();
                try {
                    Clients client = clientDao.findById(clientId);
                    if (client == null) {
                        return new SimpleStringProperty("-");
                    }
                    return new SimpleStringProperty(client.getNumberPhone());
                } catch (Exception e) {
                    return new SimpleStringProperty(Texts.get("status.error"));
                }
            });
            OrderFioColumn.setCellValueFactory(cellData -> {
                long clientId = cellData.getValue().getClientId();
                try {
                    Clients client = clientDao.findById(clientId);
                    if (client == null) {
                        return new SimpleStringProperty(Texts.get("status.clientMissing"));
                    }
                    return new SimpleStringProperty(client.getSurname() + " " + client.getName() + " " + client.getPatronymic());
                } catch (Exception e) {
                    e.printStackTrace(); // <-- покажет реальную ошибку
                    return new SimpleStringProperty(Texts.get("status.errorPrefix") + " " + e.getMessage());
                }
            });
            OrderStartColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatDateTime(cellData.getValue().getDateTimeStart())));

            OrderEndColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatDateTime(cellData.getValue().getDateTimeEnd())));

            OrderCostColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getCost())));

            tblOrder.setItems(orders);
            dtpOrder.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null && newDate.isBefore(LocalDate.now()) && cbOrderHide.isSelected()) {
                    cbOrderHide.setSelected(false);
                    return;
                }
                refreshOrder();
            });
            cbOrderHide.selectedProperty().addListener((obs, oldVal, newVal) -> {
                refreshOrder();
            });
            refreshOrder();





            ClientsFioColumn.setCellValueFactory(cellData -> {
                String surname = cellData.getValue().getSurname();
                String name = cellData.getValue().getName();
                String patronymic = cellData.getValue().getPatronymic();
                String result = surname + " " + name + " " + patronymic;
                return new SimpleStringProperty(result);
            });
            ClientsNumberColumn.setCellValueFactory(cellData ->
                    cellData.getValue().numberPhoneProperty());
            ClientsBirthdayColumn.setCellValueFactory(cellData -> {
                LocalDate birthday = cellData.getValue().getBirthday();
                if (birthday == null) {
                    return new SimpleStringProperty("");
                }
                return new SimpleStringProperty(birthday.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            });
            ClientsAgeColumn.setCellValueFactory(cellData ->{
                LocalDate birthday = cellData.getValue().getBirthday();
                LocalDate today = LocalDate.now();
                int age = Period.between(birthday, today).getYears();

                return new SimpleStringProperty(String.valueOf(age));
            });
            tblClients.setItems(clients);




            GuideRoomsColumn.setCellValueFactory(cellData ->{
                String roomNumber = String.valueOf(cellData.getValue().getNumber());
                long typeId = cellData.getValue().getTypeId();
                String typeName;
                try {
                    RoomTypes newType = typeDao.findById(typeId);
                    typeName = newType.getName();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                String result =  typeName + " " + roomNumber ;

                return new SimpleStringProperty(result);
            });
            tblGuideRoom.setItems(rooms);
            tblGuideRoom.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Rooms>() {
                @Override
                public void changed(
                        ObservableValue<? extends Rooms> observableValue,
                        Rooms student,
                        Rooms current)
                {
                    if(current != null){
                        showRoom(current.getRoomId());
                        //  lblLog.setText(lblLog.getText());
                    } else {
                        // lblLog.setText("");
                    }
                }
            });
            tblGuideRoom.focusedProperty().addListener((obs, oldV, newV) -> {
                if (newV) lastFocusedTable = tblGuideRoom;
            });
            tblGuidePC.focusedProperty().addListener((obs, oldV, newV) -> {
                if (newV) lastFocusedTable = tblGuidePC;
            });
            tblGuideTariff.focusedProperty().addListener((obs, oldV, newV) -> {
                if (newV) lastFocusedTable = tblGuideTariff;
            });

        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }


    }
    private void filterOrdersByDate(LocalDate selectedDate) {
        try {
            List<Orders> allOrders = (List<Orders>) orderDao.findAll();
            orders.clear();

            LocalDate date = selectedDate != null ? selectedDate : LocalDate.now();
            LocalDateTime now = LocalDateTime.now();
            boolean hideCompleted = cbOrderHide.isSelected();

            for (Orders order : allOrders) {
                if (order.getDateTimeStart() == null) {
                    continue;
                }
                if (!order.getDateTimeStart().toLocalDate().equals(date)) {
                    continue;
                }
                if (hideCompleted && order.getDateTimeEnd() != null && order.getDateTimeEnd().isBefore(now)) {
                    continue;
                }
                orders.add(order);
            }

            tblOrder.setItems(orders);
            lblOrderCount.setText(String.valueOf(orders.size()));
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }
    }




    public void onPcNewOrder(ActionEvent actionEvent) {
        logger.info("onPcNewOrder");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Texts.bundle());
            loader.setLocation(Main.class.getResource(
                    "/orderView.fxml"));
            Parent page =  loader.load();

            Stage addStage = new Stage();
            addStage.setTitle(Texts.get("title.newOrder"));
            addStage.initModality(Modality.APPLICATION_MODAL);
            addStage.initOwner(Main.getPrimaryStage());
            Scene scene = new Scene(page);
            addStage.setScene(scene);

            OrderViewController controller = loader.getController();
            controller.setAddStage(addStage);
            // controller.setOrder(orders);

            addStage.showAndWait();
            refreshOrder();

        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }


    public void onPcUpdate(ActionEvent actionEvent) {
        logger.info("onPcUpdate");
        chbPCAvailable.setSelected(false);
        chnPcBooked.setSelected(false);
        refreshPc();
    }

    public void onPcProlong(ActionEvent actionEvent) {
        logger.info("onPcProlong");
        try {
            PcTableRow selected = tblPC.getSelectionModel().getSelectedItem();
            if (selected == null || selected.order == null) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.selectOrder")).showAndWait();
                return;
            }

            Orders order = selected.order;
            LocalDateTime now = LocalDateTime.now();
            if (!isCurrentOrder(order, now)) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.prolongOnlyCurrent")).showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Texts.bundle());
            loader.setLocation(Main.class.getResource("/pcPrologView.fxml"));
            Parent page = loader.load();

            Stage addStage = new Stage();
            addStage.setTitle(Texts.get("title.prolong"));
            addStage.initModality(Modality.APPLICATION_MODAL);
            addStage.initOwner(Main.getPrimaryStage());
            addStage.setScene(new Scene(page));

            pcPrologViewController controller = loader.getController();
            controller.setDialogStage(addStage);
            controller.setOrder(order);

            addStage.showAndWait();
            refreshOrder();
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    public void onPcShutDown(ActionEvent actionEvent) {
        logger.info("onPcShutDown");
        try {
            PcTableRow selected = tblPC.getSelectionModel().getSelectedItem();
            if (selected == null || selected.order == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, Texts.get("alert.selectOrder"));
                alert.showAndWait();
                return;
            }

            Orders order = selected.order;
            LocalDateTime now = LocalDateTime.now();
            if (order.getDateTimeStart() != null && order.getDateTimeStart().isAfter(now)) {
                Alert alert = new Alert(Alert.AlertType.WARNING, Texts.get("alert.sessionNotStarted"));
                alert.showAndWait();
                return;
            }

            order.setDateTimeEnd(now);
            orderDao.update(order);

            if (selected.pc != null) {
                selected.pc.setAvailable(true);
                pcDao.update(selected.pc);
            }

            refreshOrder();
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }
    public void onPCAvailable(ActionEvent actionEvent) {
        logger.info("onPCAvailable");
        refreshPc();
    }

    public void onPClBooked(ActionEvent actionEvent) {
        logger.info("onPClBooked");
        refreshPc();
    }




    public void refreshOrder()  {
        try {
            LocalDate selectedDate = dtpOrder.getValue() != null ? dtpOrder.getValue() : LocalDate.now();
            filterOrdersByDate(selectedDate);

            refreshPc();
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }

    }

    public void refreshPc() {
        try {
            currentPcOrders.clear();
            boolean showAvailable = chbPCAvailable.isSelected();
            boolean showBooked = chnPcBooked.isSelected();
            LocalDateTime now = LocalDateTime.now();
            LocalDate selectedDate = dpPC.getValue() != null ? dpPC.getValue() : LocalDate.now();
            List<Orders> allOrders = (List<Orders>) orderDao.findAll();

            if (!showAvailable && !showBooked) {
                for (Orders order : allOrders) {
                    if (!isOrderOnDate(order, selectedDate) || !isCurrentOrder(order, now)) {
                        continue;
                    }
                    currentPcOrders.add(new PcTableRow(pcDao.findById(order.getPcId()), order, Texts.get("status.occupied")));
                }
            } else {
                if (showAvailable) {
                    for (PC pc : pcDao.findAll()) {
                        if (!isPcBusyNow(allOrders, pc.getPcId(), now)) {
                            currentPcOrders.add(new PcTableRow(pc, null, Texts.get("status.availableWord")));
                        }
                    }
                }
                if (showBooked) {
                    for (Orders order : allOrders) {
                        LocalDateTime start = order.getDateTimeStart();
                        if (isOrderOnDate(order, selectedDate) && start != null && start.isAfter(now)) {
                            currentPcOrders.add(new PcTableRow(pcDao.findById(order.getPcId()), order, Texts.get("status.reserved")));
                        }
                    }
                }
            }

            tblPC.setItems(currentPcOrders);
            updatePcCounters(allOrders, now);
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    private void updatePcCounters(List<Orders> allOrders, LocalDateTime now) throws Exception {
        int total = 0;
        int free = 0;
        for (PC pc : pcDao.findAll()) {
            total++;
            if (!isPcBusyNow(allOrders, pc.getPcId(), now)) {
                free++;
            }
        }

        lbPcFree.setText(String.valueOf(free));
        lbPcAll.setText(String.valueOf(total));
    }

    private String guideAvailability(PC pc, List<Orders> allOrders, LocalDateTime now) {
        boolean booked = false;
        for (Orders order : allOrders) {
            if (order.getPcId() != pc.getPcId()) {
                continue;
            }
            if (isCurrentOrder(order, now)) {
                return Texts.get("status.busy");
            }
            LocalDateTime start = order.getDateTimeStart();
            if (start != null && start.isAfter(now)) {
                booked = true;
            }
        }
        return booked ? Texts.get("status.booked") : Texts.get("status.free");
    }

    private boolean isPcBusyNow(List<Orders> allOrders, long pcId, LocalDateTime now) {
        for (Orders order : allOrders) {
            if (order.getPcId() == pcId && isCurrentOrder(order, now)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentOrder(Orders order, LocalDateTime now) {
        LocalDateTime start = order.getDateTimeStart();
        LocalDateTime end = order.getDateTimeEnd();
        if (start == null || end == null) {
            return false;
        }
        return !start.isAfter(now) && !end.isBefore(now);
    }

    private boolean isOrderOnDate(Orders order, LocalDate date) {
        return order.getDateTimeStart() != null && order.getDateTimeStart().toLocalDate().equals(date);
    }

    private static class PcTableRow {
        private final PC pc;
        private final Orders order;
        private final String status;

        private PcTableRow(PC pc, Orders order, String status) {
            this.pc = pc;
            this.order = order;
            this.status = status;
        }
    }

    public void onOrderNew(ActionEvent actionEvent) {
        logger.info("onOrderNew");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Texts.bundle());
            loader.setLocation(Main.class.getResource(
                    "/orderView.fxml"));
            Parent page =  loader.load();

            Stage addStage = new Stage();
            addStage.setTitle(Texts.get("title.newOrder"));
            addStage.initModality(Modality.APPLICATION_MODAL);
            addStage.initOwner(Main.getPrimaryStage());
            Scene scene = new Scene(page);
            addStage.setScene(scene);

            OrderViewController controller = loader.getController();
            controller.setAddStage(addStage);
           // controller.setOrder(orders);

            addStage.showAndWait();
            refreshOrder();

        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    public void onOrderUpdate(ActionEvent actionEvent) {
        logger.info("onOrderUpdate");
        if (cbOrderHide.isSelected()) {
            cbOrderHide.setSelected(false);
            return;
        }
        refreshOrder();
    }

    public void onOrderCancel(ActionEvent actionEvent) {
        logger.info("onOrderCancel");
        try{
            Orders selected = tblOrder.getSelectionModel().getSelectedItem();

            orderDao.deleteById(selected.getOrderId());
            tblOrder.getItems().remove(selected);
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    public void onOrderSearch(ActionEvent actionEvent) {
        logger.info("onOrderSearch");
        try{
            String valueSearch = tfOrderSearch.getText();

            // if (valueSearch.isEmpty()){
            // Alerts.WarningAlert("Введите данные для поиска");

            // } else {
            List<Orders> result = orderDao.search(valueSearch.trim());
            orders.setAll(result);
            tblOrder.setItems(orders);

            // Обновляем счетчик
            lblOrderCount.setText(String.valueOf(orders.size()));

//                if (result.isEmpty()){
//                    lblLog.setText("Ничего не найдено");
//                    Alerts.WarningAlert("Ничего не найдено");
//
//
//                } else {
//                    lblLog.setText("Найдено студентов: " + result.size());
//                }
            //  }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    public void onOrderCancelSearch(ActionEvent actionEvent) {
        logger.info("onOrderCancelSearch");
        try{
            tfOrderSearch.clear();
            boolean dateIsToday = LocalDate.now().equals(dtpOrder.getValue());
            boolean hideWasOff = !cbOrderHide.isSelected();
            cbOrderHide.setSelected(false);
            dtpOrder.setValue(LocalDate.now());
            if (dateIsToday && hideWasOff) {
                refreshOrder();
            }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }



    public void refreshRoom()  {
        try {
            tblGuideRoom.getItems().clear();
            rooms.addAll(roomDao.findAll());

        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }

    }

    public void onGuideAdd(ActionEvent actionEvent) {
        logger.info("onGuideAdd");
        try{
            Rooms selectedRoom = tblGuideRoom.getSelectionModel().getSelectedItem();
            PC selectedPC = tblGuidePC.getSelectionModel().getSelectedItem();

            if (lastFocusedTable == tblGuideTariff) {
                System.out.println("Выбрана таблица тарифа");

                long selectedRoomTypeId = lastSelectedRoom.getTypeId();
                long roomId = lastSelectedRoom.getRoomId();

                FXMLLoader loader = new FXMLLoader();
                loader.setResources(Texts.bundle());
                loader.setLocation(Main.class.getResource(
                        "/tariffView.fxml"));
                Parent page =  loader.load();

                Stage addStage = new Stage();
                addStage.setTitle(Texts.get("title.fillTariff"));
                addStage.initModality(Modality.APPLICATION_MODAL);
                addStage.initOwner(Main.getPrimaryStage());
                Scene scene = new Scene(page);
                addStage.setScene(scene);

                TariffViewController controller = loader.getController();
                controller.setAddStage(addStage);

                controller.setRoomTypeId(selectedRoomTypeId);
                controller.initData();

                addStage.showAndWait();
                //refreshRoom();
                showRoom(roomId);
            } else if (lastFocusedTable == tblGuidePC) {
                System.out.println("Выбрана таблица ПК");
                if (selectedRoom == null) {
                    new Alert(Alert.AlertType.WARNING, Texts.get("alert.selectRoom")).showAndWait();
                    return;
                }
                RoomTypes roomType = typeDao.findById(selectedRoom.getTypeId());
                int limit = roomType == null ? 0 : roomType.getPcCount();
                if (limit <= 0) {
                    new Alert(Alert.AlertType.WARNING, Texts.get("alert.pcLimitEmpty")).showAndWait();
                    return;
                }
                if (tblGuidePC.getItems().size() >= limit) {
                    new Alert(Alert.AlertType.WARNING, String.format(Texts.get("alert.pcLimit"), limit)).showAndWait();
                    return;
                }

                PC newPc = new PC();
                newPc.setRoomId(selectedRoom.getRoomId());

                int pcCount = tblGuidePC.getItems().size() + 1;
                newPc.setNumberPC(pcCount);
                newPc.setAvailable(true);

                pcDao.save(newPc);

                showRoom(selectedRoom.getRoomId());
                System.out.println("ПК добавлен!");
            } else if (lastFocusedTable == tblGuideRoom) {
                System.out.println("Выбрана таблица комнат");
                if (lastSelectedRoom == null) {
                    System.out.println("Сначала выберите комнату!");
                    return;
                }
                FXMLLoader loader = new FXMLLoader();
                loader.setResources(Texts.bundle());
                loader.setLocation(Main.class.getResource(
                        "/roomView.fxml"));
                Parent page =  loader.load();

                Stage addStage = new Stage();
                addStage.setTitle(Texts.get("title.createRoom"));
                addStage.initModality(Modality.APPLICATION_MODAL);
                addStage.initOwner(Main.getPrimaryStage());
                Scene scene = new Scene(page);
                addStage.setScene(scene);

                RoomViewController controller = loader.getController();
                controller.setAddStage(addStage);

                addStage.showAndWait();
                refreshRoom();
            }

            if (tblGuideTariff.isFocused()) {
                System.out.println("Выбрана таблица тарифа");
            }

//            if (selectedPC != null) {
////                if (selectedRoom == null) {
////                    System.out.println("Сначала выберите комнату!");
////                    return;
////                }
////
////                PC newPc = new PC();
////                newPc.setRoomId(selectedRoom.getRoomId());
////
////                int pcCount = tblGuidePC.getItems().size() + 1;
////                newPc.setNumberPC(pcCount);
////                newPc.setAvailable(true);
////
////                pcDao.save(newPc);
////
////                showRoom(selectedRoom.getRoomId());
////                System.out.println("ПК добавлен!");
//
//            }else{
//                FXMLLoader loader = new FXMLLoader();
//                loader.setLocation(Main.class.getResource(
//                        "/roomView.fxml"));
//                Parent page =  loader.load();
//
//                Stage addStage = new Stage();
//                addStage.setTitle("Создать комнату");
//                addStage.initModality(Modality.APPLICATION_MODAL);
//                addStage.initOwner(Main.getPrimaryStage());
//                Scene scene = new Scene(page);
//                addStage.setScene(scene);
//
//                RoomViewController controller = loader.getController();
//                controller.setAddStage(addStage);
//
//                addStage.showAndWait();
//                refreshRoom();
//            }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    public void onGuideDelete(ActionEvent actionEvent) {
        logger.info("onGuideDelete");
        try {
            Rooms selectedRoom = tblGuideRoom.getSelectionModel().getSelectedItem();
            PC selectedPC = tblGuidePC.getSelectionModel().getSelectedItem();

            if (selectedPC != null) {
                if (selectedRoom == null) {
                    System.out.println("Сначала выберите комнату!");
                    return;


                }
                PC selected = tblGuidePC.getSelectionModel().getSelectedItem();

                pcDao.deleteById(selected.getPcId());
                tblGuidePC.getItems().remove(selected);

                long roomId = selectedRoom.getRoomId();
                pcs.clear();
                List<PC> pcsInRoom = (List<PC>) pcDao.findByRoomId(roomId);
                pcs.addAll(pcsInRoom);

                tblGuidePC.setItems(pcs);

            }else{
                Rooms selected = tblGuideRoom.getSelectionModel().getSelectedItem();

                roomDao.deleteById(selected.getRoomId());
                tblGuideRoom.getItems().remove(selected);

            }
            refreshRoom();

        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }

    }

    public void onGuideShowTypes(ActionEvent actionEvent)  {
        logger.info("onGuideShowTypes");
       try{
           FXMLLoader loader = new FXMLLoader();
           loader.setResources(Texts.bundle());
           loader.setLocation(Main.class.getResource(
                   "/typesView.fxml"));
           Parent page =  loader.load();

           Stage addStage = new Stage();
           addStage.setTitle(Texts.get("title.types"));
           addStage.initModality(Modality.APPLICATION_MODAL);
           addStage.initOwner(Main.getPrimaryStage());
           Scene scene = new Scene(page);
           addStage.setScene(scene);

           TypesViewController controller = loader.getController();
           controller.setAddStage(addStage);

           addStage.showAndWait();
           refreshRoom();
       } catch (Exception e) {
           throw new RuntimeException(e);
       }

    }

    public void onGuideShowTariff(ActionEvent actionEvent) {
        logger.info("onGuideShowTariff");
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Texts.bundle());
            loader.setLocation(Main.class.getResource(
                    "/tariffView.fxml"));
            Parent page =  loader.load();

            Stage addStage = new Stage();
            addStage.setTitle(Texts.get("title.tariffs"));
            addStage.initModality(Modality.APPLICATION_MODAL);
            addStage.initOwner(Main.getPrimaryStage());
            Scene scene = new Scene(page);
            addStage.setScene(scene);

            TariffViewController controller = loader.getController();
            //controller.setAddStage(addStage);

            addStage.showAndWait();
            refreshRoom();
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }







    public void refreshClients()  {
        try {
            tblClients.getItems().clear();
            clients.addAll(clientDao.findAll());

        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }

    }

    private boolean showDialog(Clients clients){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Texts.bundle());
            loader.setLocation(Main.class.getResource(
                    "/clientView.fxml"));
            Parent page =  loader.load();

            Stage addStage = new Stage();
            addStage.setTitle(Texts.get("title.client"));
            addStage.initModality(Modality.APPLICATION_MODAL);
            addStage.initOwner(Main.getPrimaryStage());
            Scene scene = new Scene(page);
            addStage.setScene(scene);

            ClientsViewController controller = loader.getController();
            controller.setAddStage(addStage);
            controller.setClients(clients);

            addStage.showAndWait();
            return controller.isOkClicked();
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    public void onClientsAdd(ActionEvent actionEvent) {
        logger.info("onClientsAdd");
        try {
            Clients newClient = new Clients();
            if (showDialog(newClient)) {
                clients.add(newClient);
            }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
        refreshClients();



    }

    public void onClientsEdit(ActionEvent actionEvent) {
        logger.info("onClientsEdit");
        Clients selectedClient = tblClients.getSelectionModel().getSelectedItem();

        if(selectedClient != null){
            showDialog(selectedClient);
        }
        refreshClients();


    }

    public void onClientsDelete(ActionEvent actionEvent) {
        logger.info("onClientsDelete");
        try{
            Clients selected = tblClients.getSelectionModel().getSelectedItem();

            clientDao.deleteById(selected.getClientId());
            tblClients.getItems().remove(selected);
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }

    }

    public void onClientsSearch(ActionEvent actionEvent) {
        logger.info("onClientsSearch");
        try{
            String valueSearch = tfClientsSearch.getText();

           // if (valueSearch.isEmpty()){
               // Alerts.WarningAlert("Введите данные для поиска");

           // } else {
                List<Clients> result = clientDao.search(valueSearch.trim());
                clients.setAll(result);

//                if (result.isEmpty()){
//                    lblLog.setText("Ничего не найдено");
//                    Alerts.WarningAlert("Ничего не найдено");
//
//
//                } else {
//                    lblLog.setText("Найдено студентов: " + result.size());
//                }
          //  }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    public void onClientsClear(ActionEvent actionEvent) {
        logger.info("onClientsClear");
        try {
            tfClientsSearch.clear();
            refreshClients();
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }






    public void onExit(ActionEvent actionEvent) {
        logger.info("onExit");
        System.exit(0);
    }

    public void onSelectTariffTbl(MouseEvent mouseEvent) {
        logger.info("onSelectTariffTbl");
    }
}
