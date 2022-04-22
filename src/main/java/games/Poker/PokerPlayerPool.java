package games.Poker;

import games.core.datatype.PlayerPool;

public class PokerPlayerPool extends PlayerPool<PokerPlayer> {
    @Override
    public PokerPlayer createObject() {
        return new PokerPlayer();
    }
    public static void main(String[] args) throws InterruptedException {
        PokerPlayerPool playerPool = new PokerPlayerPool();
        for (int i = 0; i < 100000; i++) {
            PokerPlayer player = playerPool.shiftSync();
            System.out.println("" + i + player);
        }
    }
}
