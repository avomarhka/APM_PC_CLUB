package Models;
import javafx.beans.property.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;

@Data
@Getter
@Setter

public class Clients {
    private final LongProperty clientId = new SimpleLongProperty(0L);
    private final StringProperty surname = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty patronymic = new SimpleStringProperty("");
    private final StringProperty numberPhone = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> birthday = new SimpleObjectProperty<>(null);

    public Clients() {
    }

    public Clients(long clientId, String surname, String name, String patronymic, String number_phone, LocalDate birthday){
        this.clientId.set(clientId);
        this.surname.set(surname);
        this.name.set(name);
        this.patronymic.set(patronymic);
        this.numberPhone.set(number_phone);
        this.birthday.set(birthday);
    }
    public StringProperty surnameProperty() {
        return surname;
    }
    public StringProperty nameProperty() {
        return name;
    }
    public StringProperty patronymicProperty() {
        return patronymic;
    }
    public StringProperty numberPhoneProperty() {
        return numberPhone;
    }
    public ObjectProperty<LocalDate>  birthdayProperty() {
        return birthday;
    }

    public long getClientId(){
        return clientId.get();
    }
    public  String getSurname(){
        return surname.get();
    }
    public  String getName(){
        return name.get();
    }
    public  String getPatronymic(){
        return patronymic.get();
    }
    public  String getNumberPhone(){
        return numberPhone.get();
    }
    public LocalDate getBirthday(){
        return birthday.get();
    }

    public void setClientId(long value) {
        clientId.set(value);
    }
    public void setSurname(String value) {
        surname.set(value);
    }
    public void setName(String value) {
        name.set(value);
    }
    public void setPatronymic(String value) {
        patronymic.set(value);
    }
    public void setNumberPhone(String value) {
        numberPhone.set(value);
    }
    public void setBirthday(LocalDate  value) {
        birthday.set(value);
    }

}
