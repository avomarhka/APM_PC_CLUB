package Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Dao.Dao;
import Dao.TariffDao;
import Dao.RoomTypesDao;
import Pojo.Prices;

import Pojo.RoomTypes;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class TariffViewController {
    private static final Logger logger = LoggerFactory.getLogger(TariffViewController.class);

    public TextField tfTariff;
    public Button btnAdd;
    public Button btnDelete;
    public TableView<Prices> tblTariff;
    public TableColumn <Prices, String> TariffColumn;
    public TableColumn  <Prices, Number> PriceColumn;
    public TextField tfPrices;
    public ChoiceBox <RoomTypes> chbTypes;

    private Dao<Prices,Long> dao;
    private Dao<RoomTypes, Long> typeDao;

    private Stage dialogStage;
    private long roomTypeId;

    public TariffViewController() {
        this.dao = new TariffDao();
        this.typeDao = new RoomTypesDao();
    }

    private ObservableList<Prices> prices =
            FXCollections.observableArrayList();

    @FXML
    void initialize(){
        try{
            if (roomTypeId == 0) {
                refresh();
            }

            TariffColumn.setCellValueFactory(cellData -> {
                String tariffName = cellData.getValue().getTariff();
                long typeId = cellData.getValue().getTypeId();
                String typeName;
                try {
                    RoomTypes newType = typeDao.findById(typeId);
                    typeName = newType.getName();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                String result = tariffName + " (" + typeName + ")";
                return new SimpleStringProperty(result);
            });
            PriceColumn.setCellValueFactory(cellData ->
                    cellData.getValue().priceProperty());
            tblTariff.setItems(prices);

            chbTypes.getItems().clear();
            chbTypes.getItems().addAll(typeDao.findAll());

            chbTypes.setConverter(new javafx.util.StringConverter<RoomTypes>() {
                @Override
                public String toString(RoomTypes object) {
                    return object != null ? object.getName() : "";
                }

                @Override
                public RoomTypes fromString(String string) {
                    return null;
                }

            });

            System.out.println("Получен ID комнаты: " + roomTypeId);

            if (roomTypeId > 0) {
                for (RoomTypes type : chbTypes.getItems()) {
                    if (type.getId() == roomTypeId) {
                        chbTypes.setValue(type);
                        break;
                    }
                }
                chbTypes.setDisable(true);


                tblTariff.setVisible(false);
                tblTariff.setManaged(false);
                btnDelete.setVisible(false);
                btnDelete.setManaged(false);
            }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }

    }

    public void initData() {
        try {
            if (roomTypeId > 0) {
                for (RoomTypes type : chbTypes.getItems()) {
                    if (type.getId() == roomTypeId) {
                        chbTypes.setValue(type);
                        break;
                    }
                }
                chbTypes.setDisable(true);
                tblTariff.setVisible(false);
                tblTariff.setManaged(false);
                btnDelete.setVisible(false);
                btnDelete.setManaged(false);
            }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }
    }
    
    


    public void onAdd(ActionEvent actionEvent) {
        logger.info("onAdd");
        try {
            Prices newPrices = new Prices();
            newPrices.setTariff(tfTariff.getText());
            newPrices.setPrice(Double.valueOf(tfPrices.getText()));
            newPrices.setTypeId(chbTypes.getValue().getId());

            dao.save(newPrices);

            tfTariff.clear();
            dialogStage.close();
            refresh();
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }
    }

    public void onDelete(ActionEvent actionEvent) {
        logger.info("onDelete");
        try{
           Prices selected = tblTariff.getSelectionModel().getSelectedItem();

            dao.deleteById(selected.getId());
            tblTariff.getItems().remove(selected);
//            lblLog.setText("Строка удалена");
//            Alerts.InfoAlert("Строка удалена");
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }


    public void refresh() throws Exception {
        try{
            tblTariff.getItems().clear();
            prices.addAll(dao.findAll());
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }

    }

    public void setRoomTypeId(long roomId) {
        this.roomTypeId = roomId;
    }

    public void setAddStage(Stage addStage) {
        this.dialogStage = addStage;
    }
}
