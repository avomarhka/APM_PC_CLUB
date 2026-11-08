package Models;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RoomTypes {
    private final LongProperty typeId = new SimpleLongProperty(0L);
    private final StringProperty name = new SimpleStringProperty("");
    private final IntegerProperty pcCount = new SimpleIntegerProperty(0);

    public RoomTypes() {
    }

    public RoomTypes(int typeId, String name){
        this.typeId.set(typeId);
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public long getId(){
        return typeId.get();
    }
    public  String getName(){
        return name.get();
    }


    public void setId(long value) {
        typeId.set(value);
    }
    public void setName(String value) {
        name.set(value);
    }

    public IntegerProperty pcCountProperty() {
        return pcCount;
    }

    public int getPcCount() {
        return pcCount.get();
    }

    public void setPcCount(int value) {
        pcCount.set(value);
    }
}
