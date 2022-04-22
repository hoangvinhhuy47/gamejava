package libs.util.pool;

import java.util.PriorityQueue;

/**
 * Giúp cấp phát trước một lượng object chỉ định nhằm tránh việc cấp phát bộ nhớ liên tục
 * dành cho những đối tượng được khởi tạo và huỷ liên tục (Như đạn bắn ra, lính trong game...)
 * @param <T> Loại đối tượng
 */
public abstract class Pool<T> implements PoolAble<T>{
    protected PriorityQueue<T> data;
    protected PriorityQueue<T> outData;
    /**
     * Mặc định sẽ khởi tạo 16 phần tử
     */
    private static final int NUM_INIT_ELEMENT = 16;

    protected Pool(){
        this(NUM_INIT_ELEMENT);
    }

    protected Pool(int numInitElement) {
        this.data = new PriorityQueue<>();
        this.outData = new PriorityQueue<>();
        for (int i = 1; i<=numInitElement; i++){
            data.offer(this.createObject());
        }
    }

    @Override
    public boolean put(T e) {
        this.outData.remove(e);
        return this.data.offer(e);
    }

    @Override
    public T shift() {
        T e = this.data.poll();
        this.outData.offer(e);
        return e;
    }

    @Override
    public void reset() {
        this.data.addAll(this.outData);
        this.outData.clear();
    }

    @Override
    public int getMaxSize() {
        return Integer.MAX_VALUE;
    }
}
