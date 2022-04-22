package libs.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Map;

public class LoadConfig {
    private Map<String, Object> _configData;
    public LoadConfig(String configFileUrl){
        try{
            FileReader reader = new FileReader(new File(configFileUrl));
//            Map<String, Object> _configData = JSON
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            _configData = gson.fromJson(reader, type);
        } catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
    }

    /**
     *  Return string value of param
     * @param pattern want to get "obj1>obj2>...>objn
     * @return String
     */
    public String getValue(String pattern){
        return getValue(pattern, String.class);
    }

    /**
     *
     * @param pattern ex : "key1>key11>key111"
     * @param aClass Map or String
     * @param <T> return type
     * @return T (String or Map)
     */
    public <T> T getValue(String pattern, Class<T> aClass){
        String[] pats = pattern.split(">");
        if (pats.length == 1){
                return aClass.cast(_configData.get(pats[0]));
        } else {
            if (_configData.get(pats[0]) instanceof Map){
                Map data = (Map) _configData.get(pats[0]);
                for (int i = 0; i < pats.length-1; i++){
                    if (i != 0 && data != null) data = (Map) data.get(pats[i]);
                    if ( data != null){
                        if ( (data.containsKey(pats[i+1])
                                && !(data.get(pats[i+1]) instanceof Map))
                            || ((i+1) == pats.length-1) ){
                            return aClass.cast(data.get(pats[i+1]));
                        }
                    }
                }
            } else {
                return aClass.cast(String.valueOf(_configData.get(pats[0])));
            }
        }
        return aClass.cast("");
    }

    public static void main(String[] args) {
        LoadConfig loadConfig = new LoadConfig("server_config.json");
        System.out.println(loadConfig.getValue("mongodb>host", String.class));
    }
}
