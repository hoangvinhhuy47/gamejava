package games.Poker;

import games.core.application.handler.ChannelContextInfo;
import games.core.datatype.BasePlayer;
import games.core.datatype.HashPlayer;
import games.core.exception.game.NotFoundPlayerInGame;
import games.core.user.User;
import libs.util.data.HashByInt;

public class PokerPlayer extends BasePlayer {
@Descreption_Anno(Name = "SINGLETONE")
    public static PokerPlayer instance;
    public static PokerPlayer getInstance() {
        if (instance == null) {
            instance = new PokerPlayer();
        }
        return instance;
    }
@Descreption_Anno(Name = "FIELDS")
    protected PokerRoom room;
    protected HashPlayer<PokerPlayer> players = new HashPlayer<>();
    public PokerPlayer getPlayer(User user) throws NotFoundPlayerInGame {
        if (!players.containsKey(user.getId())) {
            throw new NotFoundPlayerInGame(user);
        }
        return this.players.get(user.getId());
    }

@Descreption_Anno(Name = "METHOD MẶC ĐỊNH KẾ THỪA INTERFACE")
    @Override
    public boolean isPlaying() {
        return false;
    }
@Descreption_Anno(Name = "ADD PLAYER TO ROOM")
    public void setRoom(PokerRoom room) {
        this.room = room;
    }
    public PokerRoom getRoom() {
        return room;
    }

}
