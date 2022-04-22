package games.slot.exception;

import games.core.game.GameErrorCode;

public interface SlotErrorCode extends GameErrorCode {
    int INVALID_STAKE_STEP = 2100;
    int PLAYER_LINE_NOT_VALID = 2100;
    int PLAYER_CANNOT_SPIN = 2101;
    int PLAYER_CANNOT_CHANGE_ROOM_IN_FREE_SPIN = 2102;
    int WRONG_SLOT_WIN_TYPE = 2103;
    int PLAYER_CANNOT_OPEN_BONUS = 2104;
    int BONUS_ITEM_IS_OPENED = 2105;
    int LIMIT_BONUS_ITEM_CAN_OPEN = 2016;
}
