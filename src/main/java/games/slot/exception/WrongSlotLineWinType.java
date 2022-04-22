package games.slot.exception;

import games.core.exception.BaseException;
import games.slot.SlotPlayer;

public class WrongSlotLineWinType extends BaseException {
    public WrongSlotLineWinType(SlotPlayer player) {
        super(SlotErrorCode.WRONG_SLOT_WIN_TYPE, String.format("Player[%s] win a wrong slot winType",
                player.getId()));
    }
}
