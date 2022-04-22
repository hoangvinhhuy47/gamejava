package games.slot.exception;

import games.core.exception.BaseException;

public class ItemBonusIsOpened extends BaseException {
    public ItemBonusIsOpened(){
        super(SlotErrorCode.BONUS_ITEM_IS_OPENED, "Item had been opened");
    }
}
