package games.slot.data;

import libs.util.data.IDataRow;

public abstract class SlotLine implements IDataRow {
    protected int id;
    /**
     * Cột thứ 1: giá trị là index hàng của nó
     */
    protected int col1;
    protected int col2;
    protected int col3;

    /**
     * Mảng để mapping cột với hàng để xác định vị trí của line
     */
    protected int[] rowOfCol;

    public int getId() {
        return id;
    }

    public int[] getRowOfCol() {
        return rowOfCol;
    }
    public int getLength() {
        return rowOfCol.length;
    }
}
