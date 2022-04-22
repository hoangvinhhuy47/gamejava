package games.tx.pool;

import games.core.datatype.PlayerPool;
import games.tx.TXPlayer;

public class TXPlayerPool extends PlayerPool<TXPlayer> {
    @Override
    public TXPlayer createObject() {
        return new TXPlayer();
    }

    public static void main(String[] args) throws InterruptedException
    {
        TXPlayerPool playerPool = new TXPlayerPool();
        for (int i = 0; i < 100000; i++) {
            TXPlayer player = playerPool.shiftSync();
            System.out.println("" + i + player);
        }

    }
}
