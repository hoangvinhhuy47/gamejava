package games.payment.object;

/**
 * Created by tuanhoang on 10/4/17.
 */
public class UserCardResponse {
    private int ResponseCode = 0;
    private String Description = "";
    private String ResponseContent = "";
    private String Signature = "";

    //region getter - setter

    public int getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(int responseCode) {
        ResponseCode = responseCode;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getResponseContent() {
        return ResponseContent;
    }

    public void setResponseContent(String responseContent) {
        ResponseContent = responseContent;
    }

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }


    //endregion
}
