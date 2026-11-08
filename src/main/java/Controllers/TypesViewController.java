package Controllers;

import Dao.Dao;
import Dao.RoomTypesDao;
import Models.RoomTypes;
import javafx.beans.property.LongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Alerts;

public class TypesViewController {
    public TextField tfTypes;
    public Button btnAdd;
    public Button btnDelete;
    public TableView<RoomTypes> tblTypes;
    public TableColumn <RoomTypes, String>TypeColumn;


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

            tblTypes.setItems(types);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onAdd(ActionEvent actionEvent) {
        try {
            RoomTypes newType = new RoomTypes();
            newType.setName(tfTypes.getText());

            dao.save(newType);

            tfTypes.clear();
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDelete(ActionEvent actionEvent) {
        try{
            RoomTypes selected = tblTypes.getSelectionModel().getSelectedItem();

            dao.deleteById(selected.getId());
            tblTypes.getItems().remove(selected);
//            lblLog.setText("Строка удалена");
//            Alerts.InfoAlert("Строка удалена");
        } catch (Exception e) {
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
