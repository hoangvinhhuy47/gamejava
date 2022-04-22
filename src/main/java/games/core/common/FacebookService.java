package games.core.common;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Created by caont on 2/10/2017.
 */
public class FacebookService {
    private static final FacebookService _instance = new FacebookService();

    /**
     * @return the _instance
     */
    public static FacebookService getInstance() {
        return _instance;
    }

    public void validateToken(long id, String fbToken) {
        String realFid = String.valueOf(Network.getInstance().getURLResponseObject("https://graph.facebook.com/me?access_token=" + fbToken).get("id"));
    }

    public Map<String, Object> getUserInfo(String fbToken) {
        return Network.getInstance().getURLResponseObject("https://graph.facebook.com/me?access_token=" + fbToken);
    }

    public String getAvatar(String fbToken) {
        return Network.getInstance().getURLResponseURL("https://graph.facebook.com/me/picture?&type=large&access_token=" + fbToken);
    }

    public Set<String> getFacebookFriend( String fbToken) {
        Set<String> listUid = new HashSet<>();
        Map<String, Object> friendData = Network.getInstance().getURLResponseObject("https://graph.facebook.com/me/friends?limit=500&offset=0&access_token=" + fbToken);
        List<Map<String, Object>> listFriend = (List) friendData.get("data");
        for (Map<String, Object> user : listFriend) {
            listUid.add(String.valueOf(user.get("id")));
        }
        return listUid;
    }
}
