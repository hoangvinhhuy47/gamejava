package libs.util.pool;

import libs.util.pool.exception.PoolIsFull;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Đồng bộ việc thêm và lấy phần tử ra khỏi pool
 * @param <T> store type
 */
public abstract class SyncPoolObject<T> implements PoolAble<T> {
    /**
     * Thời gian chờ tối đa để đưa phần từ vào
     */
    private static final long MAX_INSERT_TIME_WAIT = 100;
    private long maxInsertTimeWait = MAX_INSERT_TIME_WAIT;

    /**
     * Thời gian chờ tối đa để lấy phần tử ra
     */
    private static final long MAX_REMOVE_TIME_WAIT = 100;
    private long maxRemoveTimeWait = MAX_REMOVE_TIME_WAIT;

    /**
     * Mặc định sẽ khởi tạo 16 phần tử
     */
    private static final int NUM_INIT_ELEMENT = 16;
    private int numInitElement = NUM_INIT_ELEMENT;
    /**
     * Số phần tử tối đa mà pool này có thể giữ (gồm cả data và outdata)
     */
    private static final int MAX_SIZE = Integer.MAX_VALUE;
    private int maxSize = MAX_SIZE;

    protected LinkedBlockingQueue<T> data;
    protected LinkedBlockingQueue<T> outData;


    protected SyncPoolObject(boolean isInitPool){
        if (isInitPool){
            initPool();
        }
    }

    protected SyncPoolObject(){
        initPool();
    }

    protected SyncPoolObject(int numInitElement) {
        this.numInitElement = numInitElement;
        initPool();
    }

    protected SyncPoolObject(int numInitElement, int maxSize){
        this.maxSize = maxSize;
        this.numInitElement = numInitElement;
        initPool();
    }
    protected SyncPoolObject(int numInitElement,
                             long maxInsertTimeWait,
                             long maxRemoveTimeWait) {
        this.numInitElement = numInitElement;
        this.maxInsertTimeWait = maxInsertTimeWait;
        this.maxInsertTimeWait = maxRemoveTimeWait;
        initPool();
    }

    protected SyncPoolObject(int numInitElement,
                             long maxInsertTimeWait,
                             long maxRemoveTimeWait,
                             int maxSize) {
        this.numInitElement = numInitElement;
        this.maxInsertTimeWait = maxInsertTimeWait;
        this.maxRemoveTimeWait = maxRemoveTimeWait;
        this.maxSize = maxSize;
        initPool();
    }

    protected void initPool(){
        this.data = new LinkedBlockingQueue<>();
        this.outData = new LinkedBlockingQueue<>();
        for (int i = 1; i<=numInitElement; i++){
            data.offer(this.createObject());
        }
    }

    @Override
    public boolean put(T e) {
        this.outData.remove(e);
        return this.data.offer(e);
    }

    public synchronized boolean putSync(T e) {
        return this.put(e);
    }

    private T generateNewObject(){
        if (totalSize() < getMaxSize()){
            T obj = createObject();
            this.outData.offer(obj);
            return obj;
        }
        throw new PoolIsFull(this);
    }

    @Override
    public T shift()
    {
        T obj;
        if (dataSize()>0){
            obj = this.data.poll();
            if (obj == null)
            {
                return generateNewObject();
            } else {
                this.outData.offer(obj);
                return obj;
            }
        } else {
            return generateNewObject();
        }
    }

    public synchronized T shiftSync() {
        return this.shift();
    }

    @Override
    public void reset() {
        this.data.addAll(this.outData);
        this.outData.clear();
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    public int dataSize(){
        return data.size();
    }

    public int outDataSize(){
        return outData.size();
    }

    public int totalSize(){
        return dataSize() + outDataSize();
    }
}
