package Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Dao.ClientsDao;
import Dao.Dao;
import Models.Clients;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Main;
import utils.Texts;

import java.util.List;

public class OrderClientsController {
    private static final Logger logger = LoggerFactory.getLogger(OrderClientsController.class);

    public TextField tfSearch;
    public Button btnSearch;
    public Button btnClear;
    public Button btnAdd;
    public Button btnSelect;
    public TableView<Clients> tblClient;
    public TableColumn<Clients, String> FioColumn;
    public TableColumn<Clients, String> numberColumn;

    private final Dao<Clients, Long> clientDao = new ClientsDao();
    private final ObservableList<Clients> clients = FXCollections.observableArrayList();

    private Stage dialogStage;
    private Clients selectedClient;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public Clients getSelectedClient() {
        return selectedClient;
    }

    @FXML
    void initialize() {
        FioColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatFio(cellData.getValue())));
        numberColumn.setCellValueFactory(cellData ->
                cellData.getValue().numberPhoneProperty());
        tblClient.setItems(clients);
        loadAll();
    }

    public void onSearch(ActionEvent actionEvent) {
        logger.info("onSearch");
        try {
            String value = tfSearch.getText() == null ? "" : tfSearch.getText().trim();
            if (value.isEmpty()) {
                loadAll();
                return;
            }
            List<Clients> result = clientDao.search(value);
            clients.setAll(result);
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    public void onClear(ActionEvent actionEvent) {
        logger.info("onClear");
        tfSearch.clear();
        loadAll();
    }

    public void onAdd(ActionEvent actionEvent) {
        logger.info("onAdd");
        try {
            Clients newClient = new Clients();
            if (showClientDialog(newClient)) {
                tfSearch.clear();
                loadAll();
                for (Clients item : clients) {
                    if (item.getClientId() == newClient.getClientId()) {
                        tblClient.getSelectionModel().select(item);
                        tblClient.scrollTo(item);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    private boolean showClientDialog(Clients client) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(Texts.bundle());
        loader.setLocation(Main.class.getResource("/clientView.fxml"));
        Parent page = loader.load();

        Stage addStage = new Stage();
        addStage.setTitle(Texts.get("title.addClient"));
        addStage.initModality(Modality.APPLICATION_MODAL);
        addStage.initOwner(dialogStage != null ? dialogStage : Main.getPrimaryStage());
        addStage.setScene(new Scene(page));

        ClientsViewController controller = loader.getController();
        controller.setAddStage(addStage);
        controller.setClients(client);

        addStage.showAndWait();
        return controller.isOkClicked();
    }

    public void onSelect(ActionEvent actionEvent) {
        logger.info("onSelect");
        Clients selected = tblClient.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, Texts.get("alert.selectClient"));
            alert.showAndWait();
            return;
        }
        selectedClient = selected;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void loadAll() {
        try {
            clients.setAll(clientDao.findAll());
        } catch (Exception e) {
            logger.error("Ошибка операции", e);
            throw new RuntimeException(e);
        }
    }

    private String formatFio(Clients client) {
        String surname = client.getSurname() == null ? "" : client.getSurname();
        String name = client.getName() == null ? "" : client.getName();
        String patronymic = client.getPatronymic() == null ? "" : client.getPatronymic();
        return (surname + " " + name + " " + patronymic).trim();
    }
}
