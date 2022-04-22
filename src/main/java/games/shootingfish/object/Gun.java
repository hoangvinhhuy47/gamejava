package games.shootingfish.object;

import games.shootingfish.data.IDataRow;

/**
 * Created by WINDNCC on 7/11/2017.
 */
public class Gun extends GameObject implements IDataRow {

    private int multiple;

    public Gun(){

    }
    public Gun(String[] strings){
        loadDataRow(strings);
    }
    public Gun(int id, int multiple){
        this.id = id;
        this.multiple = multiple;
    }

    public int getMultiple() {
        return multiple;
    }

    @Override
    public void loadDataRow(String[] strings) {
            this.id = Integer.valueOf(strings[0]);
            this.multiple = Integer.valueOf(strings[1]);
    }
}
