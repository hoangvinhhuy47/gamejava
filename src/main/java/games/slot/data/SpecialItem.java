package games.slot.data;

import libs.util.data.IDataRow;

public class SpecialItem implements IDataRow {
    private int slotAWill;
    private int slotABonus;
    private int slotBWill;
    private int slotBBonus;

    public SpecialItem(String[] strings) {
        loadDataRow(strings);
    }

    @Override
    public void loadDataRow(String[] strings) {
        this.slotAWill = Integer.valueOf(strings[0]);
        this.slotABonus = Integer.valueOf(strings[1]);
        this.slotBWill = Integer.valueOf(strings[2]);
        this.slotBBonus = Integer.valueOf(strings[3]);
    }

    public int getSlotAWill() {
        return slotAWill;
    }

    public void setSlotAWill(int slotAWill) {
        this.slotAWill = slotAWill;
    }

    public int getSlotABonus() {
        return slotABonus;
    }

    public void setSlotABonus(int slotABonus) {
        this.slotABonus = slotABonus;
    }

    public int getSlotBWill() {
        return slotBWill;
    }

    public void setSlotBWill(int slotBWill) {
        this.slotBWill = slotBWill;
    }

    public int getSlotBBonus() {
        return slotBBonus;
    }

    public void setSlotBBonus(int slotBBonus) {
        this.slotBBonus = slotBBonus;
    }
}
