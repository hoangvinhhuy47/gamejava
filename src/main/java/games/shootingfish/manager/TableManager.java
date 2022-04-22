package games.shootingfish.manager;

import games.shootingfish.GameTable;
import games.shootingfish.data.DataManager;
import games.shootingfish.datatype.RoomType;
import games.tx.TaiXiuGame;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TableManager {
    private static Logger logger = LogFactory.getLogger(TableManager.class.getSimpleName());
    /**
     * current maxTableId
     */
    private int maxTableId = 50;
    /**
     * minimize num of table will be free at the time
     */
    private int minTableFree = 3;

    private RoomType roomType;

    /**
     * pool of table
     */
    private LinkedBlockingQueue<GameTable> poolTable;

    /**
     *
     */
    private ConcurrentHashMap<Integer, GameTable> listActiveTable;

    /**
     * hashMap contains list GameTable active now( not full )
     */
    private BlockingQueue<GameTable> listAvailableTable;

    /**
     *  all table free now (if size > minFreeTable pushBack table to pool)
     */
    private BlockingQueue<GameTable> listFreeTable;

    public TableManager(RoomType roomType)
    {
        this.roomType = roomType;
        //INIT TABLE MANAGER
        listActiveTable = new ConcurrentHashMap<>();
        listFreeTable = new LinkedBlockingQueue<>();
        listAvailableTable = new LinkedBlockingQueue<>();
        poolTable = new LinkedBlockingQueue<>();


        //Init poolTable
        while(poolTable.size() < maxTableId)
        {
            int tableId = 1000000 * roomType.getRoomId() + poolTable.size() + 1;
            GameTable table = new GameTable(tableId, roomType);
            poolTable.add(table);
        }
        while (listFreeTable.size() < minTableFree)
        {
            try
            {
                GameTable gameTable = shiftTable();
                gameTable.getFishShooting().start();
                listFreeTable.offer(gameTable, 100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
        logger.info("---------Init table of Room["+roomType.getRoomName()+"] DONE!--------- ");
    }

    public GameTable getActiveTable(int tableId){
        return listActiveTable.get(tableId);
    }

    public void addActive(GameTable gameTable){
         listActiveTable.putIfAbsent(gameTable.getId(), gameTable);
    }

    public boolean isInListActive(GameTable gameTable){
        return listActiveTable.containsKey(gameTable.getId());
    }

    public boolean isInAvailable(GameTable gameTable){
        return listAvailableTable.contains(gameTable);
    }

    public boolean isInFree(GameTable gameTable){
        return listFreeTable.contains(gameTable);
    }

    public boolean removeAvailable(GameTable gameTable){
        if (gameTable.isFull() || gameTable.isFree())
            if (listAvailableTable.contains(gameTable))
                return listAvailableTable.remove(gameTable);
        return false;
    }

    public boolean addAvailable(GameTable gameTable){
        if(!gameTable.isFree() && !gameTable.isFull() && !listAvailableTable.contains(gameTable) && listActiveTable.containsKey(gameTable.getId())){
            try
            {
                return listAvailableTable.offer(gameTable, 100, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                logger.error("Can't add to available table");
            }
        }
        return false;
    }

    public boolean moveToFree(GameTable gameTable){
        try {
            //Thêm gameTable vào listFree
            if (listActiveTable.containsKey(gameTable.getId())){
                listFreeTable.offer(gameTable, 100, TimeUnit.MILLISECONDS);
                checkFreeTable();
            }

            //nếu không xóa được trong listActive or listAvailable thì xóa trong list free đi tránh việc 1 bàn tồn tại 2 nơi
            if ( ! ( listActiveTable.remove(gameTable.getId(), gameTable ) ) || !( listAvailableTable.remove(gameTable) ) )
                listFreeTable.remove(gameTable);
            return true;
        } catch (InterruptedException e) {
            logger.error("Can't move Table["+gameTable.getId()+"] from Active to Free!", e);
        }
        return false;
    }

    public GameTable getTableCanJoin(){
        try
        {
            if (listAvailableTable.size() > 0)
            return listAvailableTable.poll(100, TimeUnit.MILLISECONDS);

            //Nếu 1 không có bàn nào dang sẵn sàn để join vào thì thêm 1 bàn free vào list free
            //Sau đó lấy 1 bàn từ list free ra
            checkFreeTable();
            GameTable gameTable = listFreeTable.poll(100, TimeUnit.MILLISECONDS);

            return gameTable;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            logger.error("Can't get table can join: ", e);
        }
        return null;
    }

    public static void main(String[] args) {
        RoomType roomType = DataManager.getInstance().getDataRoomType().iterator().next();
        TableManager tableManager = new TableManager(roomType);
        System.out.println(tableManager.getTableCanJoin());
        tableManager.getTableCanJoin();

    }

    /**
     * call when have user join or leave table
     */
    private void checkFreeTable()
    {
        if (listFreeTable.size() > minTableFree) //size > 3
        {
            putFreeTableBackToPool();
        }
        else
        {
            if (listFreeTable.size() < minTableFree-1) //size < 2
            {
                addMoreFreeTable();
            }
        }
    }

    /**
     * call when not enough min game free
     */
    private void addMoreFreeTable() {
        try {
            GameTable tableFromPool = shiftTable();
            listFreeTable.put(tableFromPool);
            //tableFromPool.getFishShooting().start();
            logger.info("Start new table - " + tableFromPool.getId() + " - Size current = " + listActiveTable.size());
        } catch (InterruptedException e) {
            logger.error("Can't shift table from pool: ", e);
        }
    }
    /**
     * call if so much game free
     */
    private void putFreeTableBackToPool(){
        GameTable gameTable;
        try {
            //poll cái thằng đầu tiên của cái BlockingQueue ra
            gameTable = listFreeTable.poll(100, TimeUnit.MILLISECONDS);
            gameTable.getFishShooting().stop();
            if (!poolTable.offer(gameTable)){
                try {
                    listFreeTable.put(gameTable);
                    logger.error("Can't put free table back to pool, so put it back to freeTable");
                } catch (InterruptedException e) {
                    logger.error("Can't put free table back to pool, so put it back to freeTable but it take a exception -> show this log =]]");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("Can't poll from listFreeTable.", e);
        }
    }

    /**
     * shift table from pool
     * @return game table from pool
     */
    private GameTable shiftTable() throws InterruptedException {
        if (!poolTable.isEmpty()){
            return poolTable.poll(100, TimeUnit.MILLISECONDS);
        } else {
            maxTableId++;
            int tableId = roomType.getRoomId() * 1000000 + maxTableId;
            return new GameTable(tableId, roomType);
        }
    }

    /**
     *
     * @return
     */
    public ConcurrentHashMap<Integer, GameTable> getListActiveTable() {
        return listActiveTable;
    }
}
