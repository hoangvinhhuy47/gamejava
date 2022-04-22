package games.shootingfish.datatype;

import games.shootingfish.data.DataRow;

/**
 * Created by WINDNCC on 7/16/2017.
 */

/**
 * Hold pathId with FishGroup and FishType to spawn
 */
public class FishPathProperties extends DataRow {
    public FishPathProperties(String[] strings){
        load(strings);
    }
    private int pathId = 0;
    private int fishTypeId;
    private int fishGroupId;
    @Override
    public void load(String[] strings) {
        this.pathId = Integer.valueOf(strings[0]);
        this.fishTypeId = Integer.valueOf(strings[1]);
        this.fishGroupId = Integer.valueOf(strings[2]);
    }

    public int getPathId() {
        return pathId;
    }

    public int getFishGroupId() {
        return fishGroupId;
    }

    public int getFishTypeId() {
        return fishTypeId;
    }
}
