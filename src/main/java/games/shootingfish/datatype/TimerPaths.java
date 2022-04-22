package games.shootingfish.datatype;

import games.shootingfish.data.DataRow;
import games.shootingfish.data.IDataRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WINDNCC on 7/18/2017.
 */
public class TimerPaths implements IDataRow {
    private int timer;
    private List<Integer> listPath;
    public TimerPaths(){

    }
    public TimerPaths(String[] strings){
        loadDataRow(strings);
    }

    public int getTimer() {
        return timer;
    }

    public List<Integer> getListPath() {
        return listPath;
    }

    @Override
    public void loadDataRow(String[] strings) {
        listPath = new ArrayList<>();
        timer = Integer.valueOf(strings[0]);
        String[] listPaths = strings[1].split(";");
        for (String path : listPaths){
            listPath.add(Integer.valueOf(path));
        }
    }
}
