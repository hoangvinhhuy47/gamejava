package games.slot.data;

import libs.util.data.HashByInt;
import libs.util.data.IDataRow;

public abstract class SlotReward implements IDataRow {
    protected int itemId;
    protected int x2;
    protected int x3;
    protected HashByInt<Integer> multiples;

    public Integer getMultiple(int x) {
        return multiples.get(x);
    }

    public int getItemId() {
        return itemId;
    }


    public int getX2() {
        return x2;
    }

    public int getX3() {
        return x3;
    }
}
