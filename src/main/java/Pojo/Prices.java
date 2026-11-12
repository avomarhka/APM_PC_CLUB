package Pojo;
import javafx.beans.property.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter

public class Prices {
    private final LongProperty priceId = new SimpleLongProperty(0L);
    private final StringProperty tariff = new SimpleStringProperty("");
    private final DoubleProperty price = new SimpleDoubleProperty(0.0);
    private final LongProperty typeId = new SimpleLongProperty(0L);

    public Prices() {
    }

    public Prices(long priceId,String tariff, Double price, long typeId){
        this.priceId.set(priceId);
        this.tariff.set(tariff);
        this.price.set(price);
        this.typeId.set(typeId);
    }

    public StringProperty tariffProperty() {
        return tariff;
    }
    public DoubleProperty priceProperty() {
        return price;
    }

    public  String getTariff(){
        return tariff.get();
    }
    public  double getPrice(){
        return price.get();
    }
    public long getId(){
        return priceId.get();
    }
    public long getTypeId() {
        return typeId.get();
    }

    public void setId(long value) {
        priceId.set(value);
    }
    public void setTariff(String value) {
        tariff.set(value);
    }
    public void setPrice(Double value) {
        price.set(value);
    }
    public void setTypeId(long value) {
        typeId.set(value);
    }
}
