package games.tx;

public class TXCommand extends TaiXiuCommand {

    private static TXCommand instance = null;

    public synchronized static TXCommand getInstance(){
        if (instance == null){
            instance = new TXCommand();
        }
        return instance;
    }

    private TXCommand()
    {
        super(TXGame.getInstance());
    }
}
