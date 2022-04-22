package games.shootingfish.datatype;

import java.util.concurrent.ConcurrentHashMap;

public class HashByIntSync<T> extends ConcurrentHashMap<Integer, T> {
    public HashByIntSync(){

    }
    HashByIntSync(int capacity){
        super(capacity);
    }

    public T put(T value){return super.putIfAbsent(value.hashCode(), value);}

}
