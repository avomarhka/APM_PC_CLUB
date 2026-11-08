package Models;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PC {
    private final LongProperty pcId = new SimpleLongProperty(0L);
    private final LongProperty numberPC = new SimpleLongProperty(0L);
    private final BooleanProperty available = new SimpleBooleanProperty();
    private final LongProperty roomId = new SimpleLongProperty(0L);

    public PC() {
    }

    public PC(long pcId,long numberPC,boolean available, long roomId){
        this.pcId.set(pcId);
        this.numberPC.set(numberPC);
        this.available.set(available);
        this.roomId.set(roomId);
    }

    public LongProperty numberPcProperty() {
        return numberPC;
    }
    public BooleanProperty availableProperty() {
        return available;
    }



    public long getPcId() {
        return pcId.get();
    }
    public long getNumberPC(){
        return numberPC.get();
    }
    public boolean getAvailable(){
        return available.get();
    }
    public long getRoomId(){
        return roomId.get();
    }


    public void setPcId(long value) {
        pcId.set(value);
    }
    public void setNumberPC(long value) {
        numberPC.set(value);
    }
    public void setAvailable(boolean value) {
        available.set(value);
    }
    public void setRoomId(long value) {
        roomId.set(value);
    }




}
