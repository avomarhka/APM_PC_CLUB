package Models;
import javafx.beans.property.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@Getter
@Setter

public class Orders {
    private final LongProperty orderId = new SimpleLongProperty(0L);
    private final DoubleProperty cost = new SimpleDoubleProperty(0.0D);
    private final ObjectProperty<LocalDateTime> dateTimeStart = new SimpleObjectProperty<>(null);
    private final ObjectProperty<LocalDateTime> dateTimeEnd = new SimpleObjectProperty<>(null);
    private final LongProperty pcId = new SimpleLongProperty(0L);
    private final LongProperty clientId = new SimpleLongProperty(0L);
    private final StringProperty orderNumber = new SimpleStringProperty(" ");


    public Orders(long orderId, LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd, long pcId, long clientId, double cost, String orderNumber){
        this.orderId.set(orderId);
        this.cost.set(cost);
        this.dateTimeStart.set(dateTimeStart);
        this.dateTimeEnd.set(dateTimeEnd);
        this.pcId.set(pcId);
        this.clientId.set(clientId);
        this.orderNumber.set(orderNumber);
    }

    public DoubleProperty costProperty() {
        return cost;
    }
    public ObjectProperty<LocalDateTime>  dateTimeStartProperty() {
        return dateTimeStart;
    }
    public ObjectProperty<LocalDateTime>  dateTimeEndProperty() {
        return dateTimeEnd;
    }
    public StringProperty orderNumberProperty() {
        return orderNumber;
    }

    public long getOrderId(){
        return orderId.get();
    }
    public double getCost(){
        return cost.get();
    }
    public LocalDateTime getDateTimeStart(){
        return dateTimeStart.get();
    }
    public LocalDateTime getDateTimeEnd(){
        return dateTimeEnd.get();
    }
    public long getPcId(){
        return pcId.get();
    }
    public long getClientId(){
        return clientId.get();
    }
    public String getOrderNumber(){
        return orderNumber.get();
    }

    public void setOrderId(long value) {
        orderId.set(value);
    }
    public void setCost(double value) {
        cost.set(value);
    }
    public void setDateTimeStart(LocalDateTime  value) {
        dateTimeStart.set(value);
    }
    public void setDateTimeEnd(LocalDateTime  value) {
        dateTimeEnd.set(value);
    }
    public void setPcId(long value) {
        pcId.set(value);
    }
    public void setClientId(long value) {
        clientId.set(value);
    }
    public void setOrderNumber(String value) {
        orderNumber.set(value);
    }

}
