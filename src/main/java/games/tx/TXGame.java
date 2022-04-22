package games.tx;



public class TXGame extends TaiXiuGame
{
    public static TXGame instance = null;
    public synchronized static TXGame getInstance()
    {
        if (instance == null) {
            instance = new TXGame();
        }
        return instance;
    }


    @Override
    protected void initRooms() {
        this.addRoom(TaiXiuConst.TX_NORMAL_ROOM_ID);
    }


    @Override
    public void run() {

    }
}
