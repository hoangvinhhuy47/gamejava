package games.core.exception.game;

import games.core.datatype.BasePlayer;
import games.core.exception.BaseException;
import games.core.game.GameErrorCode;
import games.core.user.User;

public class PlayerNotEnoughMoney extends BaseException {
    public PlayerNotEnoughMoney(BasePlayer player, long requireMoney){
        super(GameErrorCode.PLAYER_NOT_ENOUGH_MONEY, String.format("Player[%s] not enough money, require %s", player.getId(), requireMoney));
    }

    public PlayerNotEnoughMoney(User user, long requireMoney){
        super(GameErrorCode.PLAYER_NOT_ENOUGH_MONEY, String.format("User[%s] not enough money, require %s", user.getId(), requireMoney));
    }
}
