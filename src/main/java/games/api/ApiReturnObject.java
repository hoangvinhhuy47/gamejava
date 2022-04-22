package games.api;

import libs.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * return data for web api
 */
public class ApiReturnObject implements ApiResponseConst{

    private int code = SUCCESS;
    //private String status = ApiReturnConstant.STATUS.SUCCESS;
    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static ApiReturnObject parseResponseMessage(String json) {
        try {
            // result.buildConfigList();
            return (ApiReturnObject) JsonUtil.parseJson(json, new TypeReference<ApiReturnObject>() {
            });
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ApiReturnObject() {
    }

    public ApiReturnObject(int code, String status, Object data) {
        this.code = code;
        this.data = data;
    }

    public ApiReturnObject(int code, Object data) {
        this.code = code;
        this.data = data;
    }
}
