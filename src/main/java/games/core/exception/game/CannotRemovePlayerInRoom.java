package games.core.exception.game;

import games.core.datatype.BasePlayer;
import games.core.exception.BaseException;
import games.core.game.GameErrorCode;

public class CannotRemovePlayerInRoom extends BaseException {
    public CannotRemovePlayerInRoom(BasePlayer player){
        super(GameErrorCode.CANNOT_REMOVE_PLAYER_IN_ROOM,
                String.format(
                        "Player[%s] cannot remove in Rooms",
                        player.getId()
                ));
    }
}
