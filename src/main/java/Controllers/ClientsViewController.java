package Controllers;

import Dao.Dao;
import Dao.ClientsDao;
import Models.Clients;
import Models.Rooms;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Alerts;

public class ClientsViewController {
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
        try {
            clients.setSurname(tfSurname.getText());
            clients.setName(tfName.getText());
            clients.setPatronymic(tfPatronymic.getText());
            clients.setNumberPhone(tfNumberPhone.getText());
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
            throw new RuntimeException(e);
        }

    }

    public boolean isOkClicked() {
        return isOkClicked;
    }
}
