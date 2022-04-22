package games.core.exception.currency;

import games.core.exception.BaseException;
import games.core.game.GameErrorCode;

public class CannotLessThanZero extends BaseException {
    public CannotLessThanZero(){
        super(GameErrorCode.CAN_NOT_LESS_THAN_ZERO, "can not less than zero");
    }
}
