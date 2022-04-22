package games.slot;

public class SlotOceanCommand extends SlotCommand {

    private static SlotOceanCommand instance = null;

    public synchronized static SlotOceanCommand getInstance(){
        if (instance == null){
            instance = new SlotOceanCommand();
        }
        return instance;
    }

    private SlotOceanCommand() {
        super(SlotOceanGame.getInstance());
    }
}
