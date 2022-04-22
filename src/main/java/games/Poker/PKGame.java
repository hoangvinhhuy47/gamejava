package games.Poker;


import games.Poker.ENUM.PokerConstanst;

public class PKGame extends PokerGame {
    public static PKGame instance = null;
    public synchronized static PKGame getInstance()
    {
        if (instance == null) {
            instance = new PKGame();
        }
        return instance;
    }
    @Override
    protected void initRooms() {
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_1);
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_2);
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_3);
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_4);
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_5);
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_6);
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_7);
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_8);
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_9);
        this.addRoom(PokerConstanst.POKER_ROOMID_MONGODB_10);

    }
    @Override
    public void run() {

    }
}
