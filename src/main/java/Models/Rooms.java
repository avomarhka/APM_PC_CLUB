package Models;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Rooms {
    private final LongProperty roomId = new SimpleLongProperty(0L);
    private final LongProperty number = new SimpleLongProperty(0L);
    private final LongProperty typeId = new SimpleLongProperty(0L);

    public Rooms() {
    }

    public Rooms(long roomId, long number, long typeId){
        this.roomId.set(roomId);
        this.number.set(number);
        this.typeId.set(typeId);
    }

    public LongProperty numberProperty() {
        return number;
    }

    public long getRoomId(){
        return roomId.get();
    }
    public long getNumber(){
        return number.get();
    }
    public long getTypeId() {
        return typeId.get();
    }


    public void setRoomId(long value) {
        roomId.set(value);
    }
    public void setNumber(long value) {
        number.set(value);
    }
    public void setTypeId(long value) {
        typeId.set(value);
    }



}
