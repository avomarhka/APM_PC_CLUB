package Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Dao.Dao;
import Dao.ClientsDao;
import Pojo.Clients;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.DateFields;
import utils.Texts;

public class ClientsViewController {
    private static final Logger logger = LoggerFactory.getLogger(ClientsViewController.class);

    public TextField tfSurname;
    public TextField tfName;
    public TextField tfPatronymic;
    public TextField tfNumberPhone;
    public Button btnSave;
    public DatePicker dtBirthday;

    private Stage dialogStage;
    private Clients clients;

    private Dao<Clients, Long> clientsDao;

    public ClientsViewController(){
        this.clientsDao = new ClientsDao();
    }

    public void setAddStage(Stage addStage) {
        this.dialogStage = addStage;
    }

    @FXML
    void initialize() {
        DateFields.setup(dtBirthday);
    }

    public void setClients(Clients clients) {
        this.clients = clients;
        if (clients != null) {
            tfSurname.setText(clients.getSurname());
            tfName.setText(clients.getName());
            tfPatronymic.setText(clients.getPatronymic());
            tfNumberPhone.setText(String.valueOf(clients.getNumberPhone()));
            dtBirthday.setValue(clients.getBirthday());
        }
    }
    private boolean isOkClicked = false;

    public void onSave(ActionEvent actionEvent) {
        logger.info("onSave");
        try {
            String surname = tfSurname.getText() == null ? "" : tfSurname.getText().trim();
            String name = tfName.getText() == null ? "" : tfName.getText().trim();
            String phone = tfNumberPhone.getText() == null ? "" : tfNumberPhone.getText().trim();
            if (surname.isEmpty() || name.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.fillName")).showAndWait();
                return;
            }
            if (!phone.matches("\\d+")) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.phoneDigits")).showAndWait();
                return;
            }
            if (dtBirthday.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, Texts.get("alert.birthday")).showAndWait();
                return;
            }
            clients.setSurname(surname);
            clients.setName(name);
            clients.setPatronymic(tfPatronymic.getText());
            clients.setNumberPhone(phone);
            clients.setBirthday(dtBirthday.getValue());

            if (clients.getClientId() == 0) {
                clientsDao.save(clients);
                //Alerts.InfoAlert ("Запись добавлена");
            }
            else {
                clientsDao.update(clients);
                //Alerts.InfoAlert ("Запись отредактирована");
            }

            isOkClicked = true;
            dialogStage.close();
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }

    }

    public boolean isOkClicked() {
        return isOkClicked;
    }
}
