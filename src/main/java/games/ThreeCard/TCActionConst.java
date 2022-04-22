package games.ThreeCard;

public interface TCActionConst
{
    // Action
    int GAME_INFO = 1;
    int TRY_JOIN_ROOM = 2;
    int LEAVE_GAME = 3;
    int UPDATE_USERS_JOIN_ROOM = 4;
    int UPDATE_USERS_LEAVE_ROOM = 5;
    int CREATE_ROOM = 6;
    int COUNT_DOWN = 7;
    int DEAL_CARDS = 8;
    int THROW_CARDS = 9;
    int GO_FIRST = 10;
    int TIME_TO_SEE_CARDS = 11;
    int NEW_TURN = 12;
    int SMALL_CARD_GO_FIRST = 13;
    int OPEN_TURN = 14;
    int JOIN_ROOM = 15;
    int COUNT_DOWN_AGAIN = 16;
    int WIN_MONEY = 17;
    int LOBBY_INFO = 18;
    int THEFINED = 19;
    int FLIP_CARDS = 20;
    int JOIN_ROOM_RANDOM = 21;
    int BET = 22;
    int CANCEL_BET = 23;
    int READY = 24;
    int SEND_CARDS_FOR_OTHER_CLIENT = 25;
    int TURN_ON_MODE_SEE_CARD = 26;
    int COMPARE_RESULTS_AND_PAYMENT = 27;
    int PAYMENT = 28;


    // tạo phòng
    int GAME_ID = 13;
    int ROOM = 79999;
    int MONEY = 100000;

    //tham số khác
    int TIME_TO_START_GAME = 11;
    int TIME_COUNT_DOWN = 11;


    //ErrorCode
    int SUCCESS = 0; //  không có lỗi
    int PENALTY = 100; // đánh lủng
    int FULL_ROOM = 101; // phòng đầy
    int NOT_ENOUGH_GOLD = 102; // không đủ vàng
    int NOT_FOUND_ROOM = 103;
    int ROOM_PLAYING = 104;

}
