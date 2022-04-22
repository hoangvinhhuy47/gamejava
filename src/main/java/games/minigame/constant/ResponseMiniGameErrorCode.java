package games.minigame.constant;

/**
 * Created by tuanhoang on 8/21/17.
 */
public interface ResponseMiniGameErrorCode {
    //General
    /**
     * Response success (not any error or Exception)
     */
    int REC_SUCCESS = 0;

    /**
     * Response can't spin because not enough ticket
     */
    int REC_WHEEL_NOT_ENOUGH_TICKET = 2;

    /**
     * Response gift code doesn't exist
     */
    int REC_GIFTCODE_DOESNT_EXIST = 10;

    /**
     * Response gift code is used
     */
    int REC_GIFTCODE_USED = 11;

    /**
     * Response you can use 2 code with same name
     */
    int REC_GIFTCODE_YOU_USED_CODE_SAME_NAME = 12;


    /**
     * Response you enter the code
     */
    int REC_PROMOTE_YOU_ENTERD_FIEND_CODE = 20;

    /**
     * Response promote code invalid
     */
    int REC_PROMOTE_CODE_INVALID = 21;
    /**
     * Response Not define
     */
    int REC_NOT_DEFINE = 99;
}
