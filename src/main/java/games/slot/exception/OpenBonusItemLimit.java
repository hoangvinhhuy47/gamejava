package games.slot.exception;

import games.core.exception.BaseException;

public class OpenBonusItemLimit extends BaseException {
    public OpenBonusItemLimit(){
        super(SlotErrorCode.LIMIT_BONUS_ITEM_CAN_OPEN, "Can not open more bonus item");
    }
}
