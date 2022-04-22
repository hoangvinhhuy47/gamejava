package games.slot.data;


public class MainSlotLine extends SlotLine{

    private int col4;
    private int col5;

    public MainSlotLine(String[] strings){
        rowOfCol = new int[5];
        loadDataRow(strings);
        this.rowOfCol[0] = this.col1;
        this.rowOfCol[1] = this.col2;
        this.rowOfCol[2] = this.col3;
        this.rowOfCol[3] = this.col4;
        this.rowOfCol[4] = this.col5;
    }

    @Override
    public void loadDataRow(String[] strings) {
        this.id = Integer.valueOf(strings[0]);
        this.col1 = Integer.valueOf(strings[1]);
        this.col2 = Integer.valueOf(strings[2]);
        this.col3 = Integer.valueOf(strings[3]);
        this.col4 = Integer.valueOf(strings[4]);
        this.col5 = Integer.valueOf(strings[5]);
    }

    @Override
    public String toString() {
        return String.format("[%s]: %s %s %s %s %s", this.id, this.col1, this.col2, this.col3, this.col4, this.col5);
    }
}
