package games.slot.pool;

import games.core.datatype.PlayerPool;
import games.slot.SlotPlayer;

public class SlotPlayerPool extends PlayerPool<SlotPlayer> {
    @Override
    public SlotPlayer createObject() {
        return new SlotPlayer();
    }

    public static void main(String[] args) throws InterruptedException {
        SlotPlayerPool playerPool = new SlotPlayerPool();
        for (int i = 0; i < 100000; i++) {
            SlotPlayer player = playerPool.shiftSync();
            System.out.println("" + i + player);
        }
    }
}
