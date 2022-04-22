package games.slot.data;

import libs.util.data.HashByInt;

public class MainSlotReward extends SlotReward{
    private int x4;
    private int x5;

    public MainSlotReward(String[] strings) {
        multiples = new HashByInt<>(4);
        this.loadDataRow(strings);
    }

    @Override
    public void loadDataRow(String[] strings) {
        this.itemId = Byte.valueOf(strings[0]);
        this.x2 = Integer.valueOf(strings[1]);
        this.x3 = Integer.valueOf(strings[2]);
        this.x4 = Integer.valueOf(strings[3]);
        this.x5 = Integer.valueOf(strings[4]);
        multiples.put(2, x2);
        multiples.put(3, x3);
        multiples.put(4, x4);
        multiples.put(5, x5);
    }

    public int getX4() {
        return x4;
    }

    public int getX5() {
        return x5;
    }
}
