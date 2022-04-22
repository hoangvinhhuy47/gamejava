package games.slot;

import games.core.constant.ServerCommandConstant;

public class SlotThreeKingdomsCommand extends SlotCommand {

    private static SlotThreeKingdomsCommand instance = null;

    public synchronized static SlotThreeKingdomsCommand getInstance() {
        if (instance == null){
            instance = new SlotThreeKingdomsCommand();
        }
        return instance;
    }

    private SlotThreeKingdomsCommand(){
        super(SlotThreeKingdomsGame.getInstance());
    }

    @Override
    public int getCommandCode() {
        return ServerCommandConstant.SLOT_THREE_KINGDOMS_COMMAND;
    }

    public static void main(String[] args) {
    }

}