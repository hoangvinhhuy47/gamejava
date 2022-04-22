package games.slot.exception;

import games.core.exception.BaseException;
import games.slot.SlotPlayer;

public class PlayerLineNotValid extends BaseException {
    public PlayerLineNotValid(SlotPlayer player){
        super(SlotErrorCode.PLAYER_LINE_NOT_VALID, String.format("Player[%s] spin with Lines{%s} not match with config", player.getId(), player.getLines()));
    }
}
