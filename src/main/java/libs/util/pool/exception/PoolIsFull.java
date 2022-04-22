package libs.util.pool.exception;

import libs.util.pool.PoolAble;

public class PoolIsFull extends RuntimeException{
    public PoolIsFull(PoolAble pool){
        super(String.format("Max size of pool[%s] is %s",
                pool.getClass().getSimpleName(),
                pool.getMaxSize()));
    }
}
