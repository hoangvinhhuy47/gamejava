package games;

import com.google.protobuf.ByteString;
import games.Poker.ENUM.PokerConstanst;
import games.Poker.PKGame;
import games.Poker.PokerCommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.exception.game.NotFoundPlayerInGame;
import games.core.exception.game.NotFoundPlayerInRoom;
import games.shootingfish.GameCommand;
import games.tx.TXGame;
import games.tx.TaiXiuCommand;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

public class GameManager {

    private static GameManager instance;
    private GameCommand fishingCommand;
    private TXGame txGame;
    private  TaiXiuCommand taiXiuCommand;
    private PKGame pkGame;
    private PokerCommand pokerCommand;

    private Logger logger;
    public static GameManager getInstance() {
        if (instance == null){
            instance = new GameManager();
        }
        return instance;
    }

    private GameManager(){
        logger = LogFactory.getLogger(getClass().getSimpleName());
        fishingCommand = GameCommand.getInstance();
        txGame = txGame.getInstance();
        taiXiuCommand = TaiXiuCommand.getInstance();

        pkGame = PKGame.getInstance();
        pokerCommand = PokerCommand.getInstance();
    }

    private void logError(ChannelContextInfo ctx, Exception e){
        logger.error(
                String.format(
                        "Disconnect remove userCtx[%d] fail", ctx.getUserId()
                ), e
        );
    }

    public void disconnectCtx(ChannelContextInfo ctx){
        try {
            fishingCommand.leaveTable(ctx);
        } catch (Exception e) {
            this.logError(ctx, e);
        }
        try
        {
            txGame.leaveRoom(ctx.getUser());
        }
        catch (NotFoundPlayerInRoom | NotFoundPlayerInGame ignore){

        }
        catch (Exception e)
        {
            this.logError(ctx, e);
        }
        try
        {
            txGame.removePlayer(ctx.getUser());
        }
        catch (NotFoundPlayerInRoom | NotFoundPlayerInGame ignore){

        }
        catch (Exception e)
        {
            this.logError(ctx, e);
        }


        // Game Poker - leave room + leave game khi player disconnect với server
        try {
            pkGame.leaveRoom(ctx.getUser());
        } catch (NotFoundPlayerInRoom | NotFoundPlayerInGame ignore) {
        } catch (Exception e) {
            this.logError(ctx, e);
        }
        try {
            pkGame.removePlayer(ctx.getUser());
        } catch (NotFoundPlayerInRoom | NotFoundPlayerInGame ignore) {

        } catch (Exception e) {
            this.logError(ctx, e);
        }
        ByteString byteString = pkGame.parseMessageToByteString();
        pkGame.SendToAll(byteString, PokerConstanst.GAME_INFO);
    }
}
