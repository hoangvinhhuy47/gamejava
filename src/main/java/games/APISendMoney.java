package games;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;

import java.io.IOException;

public class APISendMoney
{
    public static final String urlAPI = "http://apix.biragogame.com:4000/api/bonusBalance";
    public static String JsonObjectToString (String username, String money, int type, String hx) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_name",username);
        jsonObject.put("money",money);
        jsonObject.put("type",type);
        jsonObject.put("hx",hx);
        return jsonObject.toString();
    }

    public static void postWithJson(String url, String json) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        client.execute(httpPost);
        client.close();
    }
}
