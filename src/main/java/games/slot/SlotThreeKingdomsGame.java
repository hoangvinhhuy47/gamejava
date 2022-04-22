package games.slot;

public class SlotThreeKingdomsGame extends SlotGame {
    private static SlotThreeKingdomsGame instance = null;
    public synchronized static SlotThreeKingdomsGame getInstance(){
        if (instance == null){
            instance = new SlotThreeKingdomsGame();
        }
        return instance;
    }

    @Override
    protected void initRooms() {
        this.addRoom(SlotConst.SLOT_THREE_KINGDOMS_NORMAL_ROOM_ID);
        this.addRoom(SlotConst.SLOT_THREE_KINGDOMS_RICH_ROOM_ID);
        this.addRoom(SlotConst.SLOT_THREE_KINGDOMS_VIP_ROOM_ID);
    }
}
