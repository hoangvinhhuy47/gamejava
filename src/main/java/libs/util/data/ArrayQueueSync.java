package libs.util.data;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

public class ArrayQueueSync<E> extends ArrayBlockingQueue<E>{
    public ArrayQueueSync(int capacity) {
        super(capacity);
    }

    public ArrayQueueSync(int capacity, boolean fair) {
        super(capacity, fair);
    }

    public ArrayQueueSync(int capacity, boolean fair, Collection<? extends E> c) {
        super(capacity, fair, c);
    }
}
