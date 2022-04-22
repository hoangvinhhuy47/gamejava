package games.shootingfish.constant;

public interface ResponseBeanFishingConst
{
    int plus = 179;

    short LIST_ROOM_INFO_RESPONSE_BEAN                                         = 31 + plus;

    short TIMING_SPAWN_FISH_RESPONSE_BEAN                                      = 1 + plus;
    short TIMING_SPAWN_FISH_AT_JOIN_RESPONSE_BEAN                              = 2 + plus;
    short TIMING_SPAWN_FISH_NEXT_STATE_RESPONSE_BEAN                           = 3 + plus;

    short JACK_POT_RESPONSE_BEAN                                               = 4 + plus;

    short NEW_BULLET_RESPONSE_BEAN                                             = 10 + plus;
    short FISH_HAS_BEEN_KILL_RESPONSE_BEAN                                     = 11 + plus;
    short OTHER_PLAYER_CHANGE_GUN_RESPONSE_BEAN                                = 12 + plus;
    short PLAYER_CHANGE_GUN_RESPONSE_BEAN                                      = 13 + plus;
    short OTHER_PLAYER_CHANGE_SLOT_RESPONSE_BEAN                               = 14 + plus;
    short PLAYER_CHANGE_SLOT_RESPONSE_BEAN                                     = 15 + plus;

    short OTHER_PLAYER_SUPER_GUN                                               = 16 + plus;

    short OTHER_PLAYER_FREE_BULLET                                             = 17 + plus;

    short JOIN_TABLE_RESPONSE_BEAN                                             = 20 + plus;
    short OTHER_JOIN_TABLE_RESPONSE_BEAN                                       = 21 + plus;
    short PLAYER_LEAVE_TABLE_RESPONSE_BEAN                                     = 22 + plus;
    short OTHER_PLAYER_LEAVE_TABLE_RESPONSE_BEAN                               = 23 + plus;

    short CHANGE_HOST_RESPONSE_BEAN                                            = 24 + plus;

    short CHAT_IN_TABLE_RESPONSE_BEAN                                          = 30 + plus;

    short BOSS_GROW_UP_RESPONSE_BEAN                                           = 40 + plus;

    short GET_MONEY_BOSS_RESPONSE_BEAN                                         = 50 + plus;

    short SEND_TREASURE                                                        = 32 + plus;
}
