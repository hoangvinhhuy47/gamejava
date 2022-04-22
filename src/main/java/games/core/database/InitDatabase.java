package games.core.database;
import games.Poker.ENUM.PokerConstanst;
import games.core.database.document.DRoomInfo;
import games.shootingfish.datatype.RoomType;
import games.slot.SlotConst;
import games.tx.TaiXiuConst;

public class InitDatabase
{
    public static void migrate()
    {
        migrateRoomInfo();
    }

    public static void migrateRoomInfo(){
        migrateRoomSlotOcean();
        migrateFishingRoom();
        migrateRoomSlotThreeKingdoms();
        migrateTXRoom();
        migratePokerRoom();
    }

    public static void migrateTXRoom()
    {
        DRoomInfo.initTaiXiuRoom(TaiXiuConst.TX_NORMAL_ROOM_ID, 0);
    }
    public static void migratePokerRoom(){
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_1,0);
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_2,0);
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_3,0);
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_4,0);
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_5,0);
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_6,0);
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_7,0);
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_8,0);
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_9,0);
        DRoomInfo.initPokerRoom(PokerConstanst.POKER_ROOMID_MONGODB_10,0);
    }
    public static void migrateRoomSlotOcean()
    {
        new DRoomInfo(
                SlotConst.SLOT_OCEAN_NORMAL_ROOM_ID,
                SlotConst.SLOT_OCEAN_ID,
                10000,0, 5, 25,
                1, 10, 100,
                20000, 50000, 0.5);
        new DRoomInfo(
                SlotConst.SLOT_OCEAN_RICH_ROOM_ID,
                SlotConst.SLOT_OCEAN_ID,
                100000, 0, 5, 25, 1, 10, 1000,
                200000, 500000, 0.45);
        new DRoomInfo(
                SlotConst.SLOT_OCEAN_VIP_ROOM_ID,
                SlotConst.SLOT_OCEAN_ID,
                500000, 0, 5, 25, 1, 10, 5000,
                600000, 900000, 0.45);
    }

    public static void migrateRoomSlotThreeKingdoms(){
        new DRoomInfo(
                SlotConst.SLOT_THREE_KINGDOMS_NORMAL_ROOM_ID,
                SlotConst.SLOT_THREE_KINGDOMS_ID,
                10000,0, 5, 25, 1,
                10, 100,
                20000, 50000, 0.5);
        new DRoomInfo(
                SlotConst.SLOT_THREE_KINGDOMS_RICH_ROOM_ID,
                SlotConst.SLOT_THREE_KINGDOMS_ID,
                100000, 0, 5, 25, 1, 10, 1000,
                200000, 500000, 0.45);
        new DRoomInfo(
                SlotConst.SLOT_THREE_KINGDOMS_VIP_ROOM_ID,
                SlotConst.SLOT_THREE_KINGDOMS_ID,
                500000, 0, 5, 25, 1, 10, 5000,
                600000, 900000, 0.45);
    }


    private static void migrateFishingRoom() {
        DRoomInfo.initFishRoom(RoomType.NORMAL, 0);
        DRoomInfo.initFishRoom(RoomType.VIP, 0);
        DRoomInfo.initFishRoom(RoomType.SUPER_VIP, 0);
    }


    public static void main(String[] args) {
        migrate();
    }

}
