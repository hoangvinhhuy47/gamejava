package games.shootingfish.datatype;
import java.lang.reflect.Field;

import games.shootingfish.object.Fish;
/**
 * Created by WINDNCC on 7/5/2017.
 */
public class HashFish extends HashByIntSync<Fish> {
    public HashFish(){
        super();
    }
    public HashFish(int capacity){
        super(capacity);
    }
    @Override
    public Fish put(Fish fish){
        this.putIfAbsent(fish.getId(), fish);
        return fish;
    }

    public static void main(String[] args) throws NoSuchFieldException {
        Fish fish = new Fish(100);
        System.out.println(fish.hashCode());
        fish.setGroupId(11);
        System.out.println(fish.hashCode());
    }
}
