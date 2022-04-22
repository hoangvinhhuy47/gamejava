package libs.util.data;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Không cho lấy ra khi queue đang rỗng
 * - Thread lấy ra sẽ bị block nếu queue rỗng cho đến khi queue được thêm vào
 * @param <E>
 */
public class LinkedQueueSync<E> extends LinkedBlockingQueue<E> {
}
