package Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Dao.Dao;
import Dao.RoomTypesDao;
import Pojo.RoomTypes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.Texts;

public class TypesViewController {
    private static final Logger logger = LoggerFactory.getLogger(TypesViewController.class);

    public TextField tfTypes;
    public Button btnAdd;
    public Button btnDelete;
    public TableView<RoomTypes> tblTypes;
    public TableColumn<RoomTypes, String> TypeColumn;
    public TableColumn<RoomTypes, Number> TypeColumn1;
    public TextField tfCount;


    private Dao<RoomTypes,Long> dao;

    public TypesViewController() {
        this.dao = new RoomTypesDao();
    }

    private ObservableList<RoomTypes> types =
            FXCollections.observableArrayList();


    @FXML
    void initialize()  {
        try{
            refresh();

            TypeColumn.setCellValueFactory(cellData ->
                    cellData.getValue().nameProperty());
            TypeColumn1.setCellValueFactory(cellData ->
                    cellData.getValue().pcCountProperty());

            tblTypes.setItems(types);
            tblTypes.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
                if (selected == null) {
                    return;
                }
                tfTypes.setText(selected.getName());
                tfCount.setText(String.valueOf(selected.getPcCount()));
            });
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }

    }

    public void onAdd(ActionEvent actionEvent) {
        logger.info("onAdd");
        try {
            String name = tfTypes.getText() == null ? "" : tfTypes.getText().trim();
            if (name.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.typeName")).showAndWait();
                return;
            }
            int count;
            try {
                count = Integer.parseInt(tfCount.getText().trim());
            } catch (Exception e) {
                count = -1;
            }
            if (count <= 0) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.badPcCount")).showAndWait();
                return;
            }

            RoomTypes selected = tblTypes.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getName().equals(name)) {
                selected.setPcCount(count);
                dao.update(selected);
            } else {
                RoomTypes newType = new RoomTypes();
                newType.setName(name);
                newType.setPcCount(count);
                dao.save(newType);
            }

            tfTypes.clear();
            tfCount.clear();
            tblTypes.getSelectionModel().clearSelection();
            refresh();
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }
    }

    public void onDelete(ActionEvent actionEvent) {
        logger.info("onDelete");
        try{
            RoomTypes selected = tblTypes.getSelectionModel().getSelectedItem();

            dao.deleteById(selected.getId());
            tblTypes.getItems().remove(selected);
//            lblLog.setText("Строка удалена");
//            Alerts.InfoAlert("Строка удалена");
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }

    }


    public void refresh() throws Exception {
        tblTypes.getItems().clear();
        types.addAll(dao.findAll());
    }

    public void setAddStage(Stage addStage) {
    }
}
