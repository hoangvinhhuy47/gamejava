package games.core.exception.game;

import games.core.exception.BaseException;
import games.core.game.GameErrorCode;
import games.core.user.User;

public class NotFoundPlayerInGame extends BaseException {
    public NotFoundPlayerInGame(User user){
        super(GameErrorCode.PLAYER_NOT_FOUND_IN_GAME, String.format("Can not found Player[%s] in Games", user.getId()));
    }
}
