package games.ThirteenKiller;

import games.core.datatype.PlayerPool;

public class TKPlayerPool extends PlayerPool<TKPlayer> {
    @Override
    public TKPlayer createObject() {
        return new TKPlayer();
    }
}
