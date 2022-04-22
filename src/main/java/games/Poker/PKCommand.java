package games.Poker;


public class PKCommand extends PokerCommand{
    private static PKCommand instance = null;
    public synchronized static PKCommand getInstance(){
        if (instance == null){
            instance = new PKCommand();
        }
        return instance;
    }
    private PKCommand(){
        super(PKGame.getInstance());
    }
}
