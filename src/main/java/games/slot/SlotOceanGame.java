package games.slot;

public class SlotOceanGame extends SlotGame {
    private static SlotOceanGame instance = null;
    public synchronized static SlotOceanGame getInstance(){
        if (instance == null){
            instance = new SlotOceanGame();
        }
        return instance;
    }

    @Override
    protected void initRooms() {
        this.addRoom(SlotConst.SLOT_OCEAN_NORMAL_ROOM_ID);
        this.addRoom(SlotConst.SLOT_OCEAN_RICH_ROOM_ID);
        this.addRoom(SlotConst.SLOT_OCEAN_VIP_ROOM_ID);
    }
}
