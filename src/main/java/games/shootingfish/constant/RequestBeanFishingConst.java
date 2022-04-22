package games.shootingfish.constant;

public interface RequestBeanFishingConst {

    int plus = 179;
    //Room
    int GET_LIST_ROOM_INFO_REQUEST_BEAN                        = 31 + plus;

    //GAME BeanType
    int GET_TIMING_SPAWN_FISH_REQUEST_BEAN                     = 1 + plus;
    int GET_TIMING_SPAWN_FISH_AT_JOIN_REQUEST_BEAN             = 2 + plus;

    //Bullet
    int NEW_BULLET_REQUEST_BEAN                                = 10 + plus;
    int HIT_FISH_REQUEST_BEAN                                  = 11 + plus;
    int MISS_BULLET_REQUEST_BEAN                               = 12 + plus;
    int CHANGE_GUN_REQUEST_BEAN                                = 13 + plus;
    int CHANGE_SLOT_REQUEST_BEAN                               = 14 + plus;

    int JOIN_TABLE_REQUEST_BEAN                                = 20 + plus;
    int PLAYER_LEAVE_TABLE_REQUEST_BEAN                        = 21 + plus;
    int INSTANT_JOIN_TABLE_REQUEST_BEAN                        = 22 + plus;

    int CHAT_IN_TABLE_REQUEST_BEAN                             = 30 + plus;

    int GET_TREASURE                                           = 32 + plus;
    int GET_AGENCY                                             = 33 + plus;
    int GET_RANK                                               = 34 + plus;

    int GET_MONEY_FREE                                         = 6 + plus;

    int GOLD_FROM_ADS = 101 + plus;

    int GOLD_FROM_IAP = 102 + plus;

    int CHANGE_AVATAR = 103 + plus;

    int CHECK_WALLET  = 104 + plus;

}
