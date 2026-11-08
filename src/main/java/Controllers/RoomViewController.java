package Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Dao.Dao;
import Dao.RoomsDao;
import Dao.RoomTypesDao;
import Models.RoomTypes;
import Models.Rooms;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RoomViewController {
    private static final Logger logger = LoggerFactory.getLogger(RoomViewController.class);

    public TextField tfNumber;
    public ChoiceBox <RoomTypes>chbType;
    public Button btnSave;

    private Stage dialogStage;


    private Dao<Rooms, Long> roomDao;
    private Dao<RoomTypes, Long> typeDao;

    public void setAddStage(Stage addStage) {
        this.dialogStage = addStage;
    }

    public RoomViewController(){
        this.roomDao = new RoomsDao();
        this.typeDao = new RoomTypesDao();
    }

    @FXML
    void initialize(){
        try{

            chbType.getItems().clear();
            chbType.getItems().addAll(typeDao.findAll());

            chbType.setConverter(new javafx.util.StringConverter<RoomTypes>() {
                @Override
                public String toString(RoomTypes object) {
                    return object != null ? object.getName() : "";
                }

                @Override
                public RoomTypes fromString(String string) {
                    return null;
                }

            });
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            e.printStackTrace();
        }

    }

    private boolean isOkClicked = false;

    public void onSave(ActionEvent actionEvent) {
        logger.info("onSave");
        try {
            Rooms room = new Rooms();
            room.setNumber(Long.parseLong(tfNumber.getText()));
            room.setTypeId(chbType.getValue().getId());
            
            roomDao.save(room);
            isOkClicked = true;
            dialogStage.close();

            
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }



    }

}
