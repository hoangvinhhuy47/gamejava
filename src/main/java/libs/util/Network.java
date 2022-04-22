package libs.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class Network {
    private static final Network _instance = new Network();

    /**
     * @return the _instance
     */
    public static Network getInstance() {
        return _instance;
    }

    //return a redirect url request (use for getting avatar facebook, or some thing like that)
    public String getURLResponseURL(String urlRequest) {
        try {

            URL obj = new URL(urlRequest);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.getInputStream();
            return con.getURL().toString();
        } catch (Exception ex) {
            //SystemLog.getInstance().error(ex);
        }
        return null;
    }

    public String getURLResponseString(String urlRequest) {
        return getURLResponseString(urlRequest, null, "GET");
    }

    public String getURLResponseString(String urlRequest, Map<String, String> header, String Method) {
        try {
            URL obj = new URL(urlRequest);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(Method);

            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            } else {
                //set default header
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
            }

            if (con.getResponseCode() != 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getErrorStream())));
                String output;
                while ((output = br.readLine()) != null) {
                    System.err.println(output);
                }
                //SystemLog.getInstance().error("Failed : HTTP error code : " + con.getResponseCode() + " url: "+ urlRequest);
            } else {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                StringBuffer response = new StringBuffer();
                String inputLine = "";
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            }
        } catch (Exception ex) {
            //SystemLog.getInstance().error(ex);
        }
        return null;
    }

    public Map<String, Object> getURLResponseObject(String urlRequest) {
        return convertStringToMap(getURLResponseString(urlRequest));
    }

    public Map<String, Object> getURLResponseObject(String urlRequest, Map<String, String> header, String method) {
        return convertStringToMap(getURLResponseString(urlRequest, header, method));
    }

    private Map<String, Object> convertStringToMap(String responseString){
        Gson gson = new GsonBuilder().create();
        Type stringStringMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> responseData = gson.fromJson(responseString, stringStringMap);
        return responseData;
    }
}
