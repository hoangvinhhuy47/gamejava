package libs.util.data;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class DataLoader {

    protected void loadDataFromFileToObject(String filePath, DataRowHandler dataRowHandler){
        this.loadDataFromFileToObject(filePath, dataRowHandler, ",", 1);
    }

    protected void loadDataFromFileToObject(String filePath, DataRowHandler dataRowHandler, int rowStart){
        this.loadDataFromFileToObject(filePath, dataRowHandler, ",", rowStart);
    }

    protected void loadDataFromFileToObject(String filePath, DataRowHandler dataRowHandler, String separateString){
        this.loadDataFromFileToObject(filePath, dataRowHandler, separateString, 1);
    }

    protected void loadDataFromFileToObject(String filePath, DataRowHandler dataRowHandler, String separateString, int rowStart){
        try {
            List<String> listStr = FileUtils.readLines(new File(filePath), "utf-8");
            for (int i = rowStart; i< listStr.size(); i++){
                String string = listStr.get(i);
                String[] str = string.split(separateString);
                dataRowHandler.handle(str);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
