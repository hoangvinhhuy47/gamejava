package games.ThreeCard;

import games.ThirteenKiller.TKPlayer;
import games.core.datatype.PlayerPool;

public class TCPlayerPool extends PlayerPool<TCPlayer> {
    @Override
    public TCPlayer createObject() {
        return new TCPlayer();
    }
}
