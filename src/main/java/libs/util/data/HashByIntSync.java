package libs.util.data;

import java.util.concurrent.ConcurrentHashMap;

public class HashByIntSync<T> extends ConcurrentHashMap<Integer, T> {
    public HashByIntSync(){

    }
    HashByIntSync(int capacity){
        super(capacity);
    }

    public T put(T value){return super.putIfAbsent(value.hashCode(), value);}

    public boolean isExist(T value){
        return containsValue(value);
    }

}
