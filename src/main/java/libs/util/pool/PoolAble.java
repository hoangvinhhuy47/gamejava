package libs.util.pool;

import com.sun.istack.NotNull;

public interface PoolAble<T> {
    @NotNull
    T createObject();
    boolean put(@NotNull T e);
    T shift();
    void reset();

    int getMaxSize();
}
