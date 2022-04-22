package games.slot.data;

import libs.util.data.IDataRow;

import java.util.ArrayList;

public class SlotReel implements IDataRow {
    private Integer id;
    private ArrayList<Integer> items = new ArrayList<>();
    public SlotReel(String[] strings){
        this.loadDataRow(strings);
    }
    @Override
    public void loadDataRow(String[] strings) {
        this.id = Integer.valueOf(strings[0]);
        for (int i=1; i < strings.length; i++){
            items.add(Integer.valueOf(strings[i]));
        }
    }

    public ArrayList<Integer> getItems() {
        return items;
    }

    public Integer getId() {
        return id;
    }
}
