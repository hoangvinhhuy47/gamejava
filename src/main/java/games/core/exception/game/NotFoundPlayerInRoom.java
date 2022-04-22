package games.core.exception.game;

import games.core.datatype.BasePlayer;
import games.core.exception.BaseException;
import games.core.game.GameErrorCode;

public class NotFoundPlayerInRoom extends BaseException {
    public NotFoundPlayerInRoom(BasePlayer player){
        super(GameErrorCode.NOT_FOUND_PLAYER_IN_ROOM, String.format("Player[%s] not in any room",
                player.getId()));
    }
}
