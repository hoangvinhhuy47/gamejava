package games.slot.exception;

import games.core.exception.BaseException;
import games.slot.SlotPlayer;

public class InvalidStakeStep extends BaseException {
    public InvalidStakeStep(SlotPlayer player, int invalidStep) {
        super(SlotErrorCode.INVALID_STAKE_STEP, String.format("Player[%s] send step[%s]",
                player.getId(), invalidStep));
    }
}
