package games.ThirteenKiller;

public interface TKActionConst
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
    int TIME_TO_THROW_CARDS = 11;
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


    // tạo phòng
    int GAME_ID = 13;
    int ROOM = 69999;
    int MONEY = 100000;

    //tham số khác
    int TIME_TO_START_GAME = 15;
    float TIME_TO_THROW_CARD = 10.016f;


    //ErrorCode
    int SUCCESS = 0; //  không có lỗi
    int PENALTY = 100; // đánh lủng
    int FULL_ROOM = 101; // phòng đầy
    int NOT_ENOUGH_GOLD = 102; // không đủ vàng
    int NOT_FOUND_ROOM = 103;

}
