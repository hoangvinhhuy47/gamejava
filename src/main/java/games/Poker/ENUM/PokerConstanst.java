package games.Poker.ENUM;

import games.Poker.Descreption_Anno;
import libs.database.mongodb.annotation.CollectionClass;

public interface PokerConstanst {
    @Descreption_Anno(Name = "CONST VARIABLE")
    int POKER_ROOMID_MONGODB_1 = 50001;
    int POKER_ROOMID_MONGODB_2 = 50002;
    int POKER_ROOMID_MONGODB_3 = 50003;
    int POKER_ROOMID_MONGODB_4 = 50004;
    int POKER_ROOMID_MONGODB_5 = 50005;
    int POKER_ROOMID_MONGODB_6 = 50006;
    int POKER_ROOMID_MONGODB_7 = 50007;
    int POKER_ROOMID_MONGODB_8 = 50008;
    int POKER_ROOMID_MONGODB_9 = 50009;
    int POKER_ROOMID_MONGODB_10 = 50010;

    int POKER_GAMEID_MONGODB = 8; // Game thứ 8
    @Descreption_Anno(Name = "IN GAME")
    int POKER_COUNTDOWN_GAMEWAITING = 20000; // tương đương 15 s
    int POKER_COUNTDOWN_PLAYER = 25000;
    int POKER_COUNTDOWN_SHOWDOWN = 20000;

    Long POKER_SMALL_BLIND = 500L;
    Long POKER_BIG_BLIND = 1000L;

    long POKER_MIN_JOINNING_MONEY = 10000;

    int POKER_MAX_COMMUNITY_CARDS = 5;
    @Descreption_Anno(Name = "CARD STRENGTH")
    long POKER_STRAIGHTFLUSH_9 = 90000000000L;
    long POKER_FOUROFAKIND_8 = 80000000000L;
    long POKER_FULLHOUSE_7 = 70000000000L;
    long POKER_FLUSH_6 = 60000000000L;
    long POKER_STRAIGHT_5 = 50000000000L;
    long POKER_THREEOFAKIND_4 = 40000000000L;
    long POKER_TWOPAIR_3 = 30000000000L;
    long POKER_PAIR_2 = 20000000000L;
    long POKER_HIGHCARD_1 = 10000000000L;
    long POKER_NOTHING = 0;

    @Descreption_Anno(Name = "REQUEST - RESPONSE")
    int JOIN_LOBBY = 0;
    // Poker_Request_JoinLobby
    int LEAVE_LOBBY = 6;
    // Poker_Request_LeaveLobby
    int JOIN_ROOM = 1;
    // Poker_Request_JoinRoom
    int LEAVE_ROOM = 2;
    // Poker_Request_LeaveRoom
    int GAME_INFO = 20;
    // Poker_Response_RoomInfo
    int GAME_INFO_1ROOM = 22;
    // Poker_Response_RoomInfo
    int UPDATE_MONEY = 23;
    // Poker_Response_UpdateMoney
    int GAME_PLAYERCOUNTDOWN = 5;
    // Poker_Response_AllCountDown_10Room
    int POKER_FAKEPLAYER = 21;
    // Poker_Request_JoinFakePlayer



    int POKER_STOPTURN = 10;
    // Poker_TurnOver
    int POKER_NEWGAME = 3;
    // Poker_NEWGAME


    int POKER_ROOM_FUND = 50; //????

}




