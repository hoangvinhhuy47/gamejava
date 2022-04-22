package games.shootingfish.pool;

import games.shootingfish.object.Fish;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by WINDNCC on 7/6/2017.
 */
public class PoolFish extends PoolObject<Fish> {
    public PoolFish() {
        maxId = 1000;
        dataQueue = new LinkedBlockingQueue<>();
        outPool = new LinkedBlockingQueue<>();
        for (int i = 1; i<= maxId; i++){
            dataQueue.add(new Fish(i));
        }
//        System.out.println("Init pool size: " +dataQueue.size());
    }

    /**
     * Get fish in pool.
     * @return Next fish in pool, if pool empty return new fish with maxId++
     */
    public Fish shiftFish() throws InterruptedException {
        if (dataQueue.isEmpty()) return createFish();
        return shiftSync();
    }

    /**
     * create new fish when pool is empty
     * @return new fish
     * @throws InterruptedException
     */
    private Fish createFish() throws InterruptedException {
        maxId++;
        Fish fish = new Fish(maxId);
        outPool.offer(fish, 100, TimeUnit.MILLISECONDS);
        return new Fish(maxId);
    }

    /**
     * put fish back to pool
     * @param fish instance of fish wanna put
     */
    public boolean putFish(Fish fish){
        try {
            putSync(fish);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }


    /**
     * num of object has left in pool
     * @return size of pool now
     */
    public int inSize(){
        return dataQueue.size();
    }

    /**
     * num of object has been shift in pool
     * @return size of out object
     */
    public int outSize(){
        return outPool.size();
    }

    /**
     * total size object
     * @return total size pool
     */

    public int totalSize(){
        return outSize() + inSize();
    }

    @Override
    public String toString() {
        return dataQueue.toString();
    }
}
