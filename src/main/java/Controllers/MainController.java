package Controllers;

import Dao.Dao;
import Dao.OrdersDao;
import Dao.ClientsDao;
import Dao.PCDao;
import Dao.RoomsDao;
import Dao.TariffDao;
import Dao.RoomTypesDao;

import Models.*;
import javafx.beans.property.SimpleBooleanProperty;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

public class MainController {
    public Button btnExit;
    public CheckBox chbAvailable;
    public CheckBox chnBooked;
    public TableView tablePC;
    public Button btnUpdate;
    public Button btnNewOrder;
    public Button btnProlong;
    public Button btnShutDown;
    public DatePicker datePicker;
    public Spinner spnTime;
    public Label lblLanguage;
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
    public TableColumn <PC, Boolean> GuideAvaliColumn;
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
    public CheckBox chbPCAvailable;
    public CheckBox chnPcBooked;
    public TableView tblPC;
    public TableColumn PcRoomColumn;
    public TableColumn PcPCColumn;
    public TableColumn PcAvailable;
    public TableColumn PcDateEnd;
    public TableColumn PcCost;
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

    private TableView<?> lastFocusedTable;
    private Rooms lastSelectedRoom;

    private void showRoom(long roomId) {
        try {
            pcs.clear();
            prices.clear();



            List<PC> pcsInRoom = (List<PC>) pcDao.findByRoomId(roomId);
            pcs.addAll(pcsInRoom);
            tblGuidePC.setItems(pcs);
            GiudePcColumn.setCellValueFactory(cellData ->
                    new SimpleLongProperty(cellData.getValue().getNumberPC()).asObject());

            GuideAvaliColumn.setCellValueFactory(cellData ->
                    new SimpleBooleanProperty(cellData.getValue().getAvailable()));



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
            throw new RuntimeException(e);
        }

    }



    @FXML
    void initialize() throws Exception {
        try{

            refreshRoom();
            refreshClients();
            refreshOrder();

            pcs.clear();
            dtpOrder.setValue(LocalDate.now());


//            PcRoomColumn;
//            PcPCColumn;
//            PcAvailable;
//            PcDateEnd;
//            PcCost




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
                    return new SimpleStringProperty("Ошибка");
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
                    return new SimpleStringProperty("Ошибка");
                }
            });
            OrderFioColumn.setCellValueFactory(cellData -> {
                long clientId = cellData.getValue().getClientId();
                System.out.println("OrderFioColumn: orderId = " + cellData.getValue().getOrderId() + ", clientId = " + clientId);
                try {
                    Clients client = clientDao.findById(clientId);
                    if (client == null) {
                        return new SimpleStringProperty("Клиент не найден");
                    }
                    return new SimpleStringProperty(client.getSurname() + " " + client.getName() + " " + client.getPatronymic());
                } catch (Exception e) {
                    e.printStackTrace(); // <-- покажет реальную ошибку
                    return new SimpleStringProperty("Ошибка: " + e.getMessage());
                }
            });
            OrderStartColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDateTimeStart().toString()));

            OrderEndColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDateTimeEnd().toString()));

            OrderCostColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getCost())));

            tblOrder.setItems(orders);
            filterOrdersByDate(LocalDate.now());
            dtpOrder.valueProperty().addListener((obs, oldDate, newDate) -> {
                filterOrdersByDate(newDate);
            });
            cbOrderHide.selectedProperty().addListener((obs, oldVal, newVal) -> {
                refreshOrder(); // Перезагружаем заказы с учетом фильтра
            });





            ClientsFioColumn.setCellValueFactory(cellData -> {
                String surname = cellData.getValue().getSurname();
                String name = cellData.getValue().getName();
                String patronymic = cellData.getValue().getPatronymic();
                String result = surname + " " + name + " " + patronymic;
                return new SimpleStringProperty(result);
            });
            ClientsNumberColumn.setCellValueFactory(cellData ->
                    cellData.getValue().numberPhoneProperty());
            ClientsBirthdayColumn.setCellValueFactory(cellData ->
                    cellData.getValue().birthdayProperty().asString());
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
            throw new RuntimeException(e);
        }


    }
    private void filterOrdersByDate(LocalDate selectedDate) {
        try {
            // 1. Загружаем ВСЕ заказы из БД (или берем из кэша)
            List<Orders> allOrders = (List<Orders>) orderDao.findAll();

            // 2. Очищаем текущий список
            orders.clear();

            if (selectedDate == null) {
                // Если дата не выбрана — показываем все заказы
                orders.addAll(allOrders);
            } else {
                // 3. Фильтруем по дате начала
                for (Orders order : allOrders) {
                    // Предполагаем, что getDateTimeStart() возвращает LocalDateTime
                    LocalDate orderDate = order.getDateTimeStart().toLocalDate();
                    if (orderDate.equals(selectedDate)) {
                        orders.add(order);
                    }
                }
            }

            // 4. Обновляем таблицу
            tblOrder.setItems(orders);

            // 5. (Опционально) Обновляем счетчик заказов на форме
            // lblOrderCount.setText("Количество заказов: " + orders.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public void onPcNewOrder(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource(
                    "/orderView.fxml"));
            Parent page =  loader.load();

            Stage addStage = new Stage();
            addStage.setTitle("Новый заказ");
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
            throw new RuntimeException(e);
        }
    }


    public void onPcUpdate(ActionEvent actionEvent) {
    }

    public void onPcProlong(ActionEvent actionEvent) {
    }

    public void onPcShutDown(ActionEvent actionEvent) {
    }
    public void onPCAvailable(ActionEvent actionEvent) {
    }

    public void onPClBooked(ActionEvent actionEvent) {
    }




    public void refreshOrder()  {
        try {
            tblOrder.getItems().clear();
            orders.clear();

            // Загружаем все заказы из БД
            List<Orders> allOrders = (List<Orders>) orderDao.findAll();

            // Если галочка "Скрыть завершенные" стоит — фильтруем
            if (cbOrderHide.isSelected()) {
                LocalDateTime now = LocalDateTime.now();
                for (Orders order : allOrders) {
                    // Показываем только те, у которых дата окончания еще не наступила
                    if (order.getDateTimeEnd().isAfter(now) || order.getDateTimeEnd().isEqual(now)) {
                        orders.add(order);
                    }
                }
            } else {
                // Иначе показываем все
                orders.addAll(allOrders);
            }

            tblOrder.setItems(orders);

            // Обновляем счетчик (если у вас есть Label)
            lblOrderCount.setText(String.valueOf(orders.size()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void onOrderNew(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource(
                    "/orderView.fxml"));
            Parent page =  loader.load();

            Stage addStage = new Stage();
            addStage.setTitle("Новый заказ");
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
            throw new RuntimeException(e);
        }
    }

    public void onOrderUpdate(ActionEvent actionEvent) {
        refreshOrder();
    }

    public void onOrderCancel(ActionEvent actionEvent) {
        try{
            Orders selected = tblOrder.getSelectionModel().getSelectedItem();

            orderDao.deleteById(selected.getOrderId());
            tblOrder.getItems().remove(selected);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onOrderSearch(ActionEvent actionEvent) {
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
            throw new RuntimeException(e);
        }
    }

    public void onOrderCancelSearch(ActionEvent actionEvent) {
        try{
            dtpOrder.setValue(null); // Сбрасываем дату
            refreshOrder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public void refreshRoom()  {
        try {
            tblGuideRoom.getItems().clear();
            rooms.addAll(roomDao.findAll());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void onGuideAdd(ActionEvent actionEvent) {
        try{
            Rooms selectedRoom = tblGuideRoom.getSelectionModel().getSelectedItem();
            PC selectedPC = tblGuidePC.getSelectionModel().getSelectedItem();

            if (lastFocusedTable == tblGuideTariff) {
                System.out.println("Выбрана таблица тарифа");

                long selectedRoomTypeId = lastSelectedRoom.getTypeId();
                long roomId = lastSelectedRoom.getRoomId();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Main.class.getResource(
                        "/tariffView.fxml"));
                Parent page =  loader.load();

                Stage addStage = new Stage();
                addStage.setTitle("Заполнить тариф");
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
                loader.setLocation(Main.class.getResource(
                        "/roomView.fxml"));
                Parent page =  loader.load();

                Stage addStage = new Stage();
                addStage.setTitle("Создать комнату");
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
            throw new RuntimeException(e);
        }
    }

    public void onGuideDelete(ActionEvent actionEvent) {
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
            throw new RuntimeException(e);
        }

    }

    public void onGuideShowTypes(ActionEvent actionEvent)  {
       try{
           FXMLLoader loader = new FXMLLoader();
           loader.setLocation(Main.class.getResource(
                   "/typesView.fxml"));
           Parent page =  loader.load();

           Stage addStage = new Stage();
           addStage.setTitle("Информация о типах");
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
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource(
                    "/tariffView.fxml"));
            Parent page =  loader.load();

            Stage addStage = new Stage();
            addStage.setTitle("Информация о тарифах");
            addStage.initModality(Modality.APPLICATION_MODAL);
            addStage.initOwner(Main.getPrimaryStage());
            Scene scene = new Scene(page);
            addStage.setScene(scene);

            TariffViewController controller = loader.getController();
            //controller.setAddStage(addStage);

            addStage.showAndWait();
            refreshRoom();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }







    public void refreshClients()  {
        try {
            tblClients.getItems().clear();
            clients.addAll(clientDao.findAll());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private boolean showDialog(Clients clients){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource(
                    "/clientView.fxml"));
            Parent page =  loader.load();

            Stage addStage = new Stage();
            addStage.setTitle("Создать комнату");
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
            throw new RuntimeException(e);
        }
    }

    public void onClientsAdd(ActionEvent actionEvent) {
        try {
            Clients newClient = new Clients();
            if (showDialog(newClient)) {
                clients.add(newClient);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        refreshClients();



    }

    public void onClientsEdit(ActionEvent actionEvent) {
        Clients selectedClient = tblClients.getSelectionModel().getSelectedItem();

        if(selectedClient != null){
            showDialog(selectedClient);
        }
        refreshClients();


    }

    public void onClientsDelete(ActionEvent actionEvent) {
        try{
            Clients selected = tblClients.getSelectionModel().getSelectedItem();

            clientDao.deleteById(selected.getClientId());
            tblClients.getItems().remove(selected);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void onClientsSearch(ActionEvent actionEvent) {
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
            throw new RuntimeException(e);
        }
    }

    public void onClientsClear(ActionEvent actionEvent) {
        try {
            tfClientsSearch.clear();
            refreshClients();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }






    public void onExit(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void onSelectTariffTbl(MouseEvent mouseEvent) {
    }
}
