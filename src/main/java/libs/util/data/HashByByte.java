package libs.util.data;
import java.util.HashMap;

public class HashByByte<T> extends HashMap<Byte, T> {
    public HashByByte(){
        super();
    }
    public HashByByte(int capacity){
        super(capacity);
    }
}
