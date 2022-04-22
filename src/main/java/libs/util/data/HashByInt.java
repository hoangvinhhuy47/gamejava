package libs.util.data;

import java.util.HashMap;

public class HashByInt<T> extends HashMap<Integer, T> {
    public HashByInt(){
        super();
    }
    public HashByInt(int capacity){
        super(capacity);
    }

    public T put(T value){return super.put(value.hashCode(), value);}
}
