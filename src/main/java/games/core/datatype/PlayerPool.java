package games.core.datatype;

import libs.util.pool.SyncPoolObject;

public abstract class PlayerPool<P extends BasePlayer> extends SyncPoolObject<P> {
    /**
     * Số lượng user mặc định được khởi tạo của mỗi pool
     */
    private final static int NUM_INIT_PLAYER = 50;

    protected PlayerPool() {
        this(NUM_INIT_PLAYER);
    }
    private PlayerPool(int numInitElement){
        super(numInitElement);
    }
}
