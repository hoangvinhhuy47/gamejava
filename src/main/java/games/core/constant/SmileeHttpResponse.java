package games.core.constant;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class SmileeHttpResponse {
    private int		status	= ServerConstant.HTTP_STATUS.SUCCESS;
    private String	data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public SmileeHttpResponse() {

    }

    public SmileeHttpResponse(int status, String data) {
        this.status = status;
        this.data = data;
    }
}
