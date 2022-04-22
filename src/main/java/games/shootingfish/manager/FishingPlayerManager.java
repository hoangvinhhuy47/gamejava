package games.shootingfish.manager;

import games.core.exception.NullUserException;
import games.shootingfish.object.FishingPlayer;

import java.util.HashMap;

/**
 * @author Cuong Nguyen Cao
 */
public class FishingPlayerManager {
    private HashMap<Integer, FishingPlayer> listPlayer;

    private FishingPlayerManager() {
        listPlayer = new HashMap<>(1000);
    }

    public synchronized FishingPlayer addPlayer(Integer playerId) throws IllegalAccessException, NullUserException {
        FishingPlayer fishingPlayer = getPlayer(playerId);
        if (fishingPlayer != null){
            //phải remove nó. bị dính trong fishing player rồi
            //return fishingPlayer;

            this.removePlayer(playerId);
        }
        fishingPlayer = new FishingPlayer(playerId);
        if (fishingPlayer.getUser() != null){
            listPlayer.put(playerId, fishingPlayer);
            return fishingPlayer;
        }
        throw new NullUserException("[addPlayer in FishingManager] - Not Found User In UserService ");
    }

    public synchronized FishingPlayer removePlayer(Integer playerId){
        if (listPlayer.containsKey(playerId)) {
            return listPlayer.remove(playerId);
        }
        return  null;
    }
    public FishingPlayer getPlayer(Integer playerId){
        if (listPlayer.containsKey(playerId)) {
            return listPlayer.get(playerId);
        }
        return null;
    }
    public HashMap<Integer, FishingPlayer> getListPlayer() {
        return listPlayer;
    }

    /**
     * Singleton Instance
     */
    private static FishingPlayerManager instance;
    public static FishingPlayerManager getInstance() {
        if (instance == null) instance = new FishingPlayerManager();
        return instance;
    }

}
