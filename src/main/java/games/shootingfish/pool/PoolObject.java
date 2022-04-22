package games.shootingfish.pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by WINDNCC on 7/6/2017.
 */
public abstract class PoolObject<T> {
    BlockingQueue<T> dataQueue;
    BlockingQueue<T> outPool;
    int maxId;

    T shiftSync() throws InterruptedException {
        T object = dataQueue.poll(100, TimeUnit.MILLISECONDS);
        outPool.offer(object, 100, TimeUnit.MILLISECONDS);
        return object;
    }

    boolean putSync(T object) throws InterruptedException {
        outPool.remove(object);
        dataQueue.offer(object, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    public BlockingQueue<T> getDataQueue() {
        return dataQueue;
    }

    public BlockingQueue<T> getOutPool() {
        return outPool;
    }
    public void collectBackToPool(){
        dataQueue.addAll(outPool);
        outPool.clear();
    }
}
