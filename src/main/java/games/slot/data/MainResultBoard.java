package games.slot.data;

import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.MoneyType;
import games.core.user.User;
import games.slot.*;

import java.util.ArrayList;

public class MainResultBoard extends ResultBoard<MainSlotLine, MainSlotReward> {

    public MainResultBoard(SlotPlayer player, SlotGameConfig<MainSlotLine, MainSlotReward> slotGameConfig) {
        super(player, slotGameConfig);
    }

    public MainResultBoard(
            SlotPlayer player,
            SlotGameConfig<MainSlotLine, MainSlotReward> slotGameConfig,
            int[][] rawBoard
    ) {
        super(player, slotGameConfig, rawBoard);
    }

    @Override
    protected int numRow() {
        return 3;
    }

    @Override
    protected int numCol() {
        return 5;
    }

    @Override
    protected int itemWill() {
        return 3;
    }

    @Override
    protected int itemBonus() {
        return 1;
    }

    @Override
    protected int itemFreeSpin(){
        return 2;
    }

    @Override
    protected int itemJackpot() {
        return 4;
    }

    protected int[][] bonusWinBoard(){
        int[][] testBoard = new int[3][5];
        testBoard[0][0] = itemBonus();
        testBoard[0][1] = 5;
        testBoard[0][2] = 8;
        testBoard[0][3] = 12;
        testBoard[0][4] = 15;

        testBoard[1][0] = 9;
        testBoard[1][1] = 9;
        testBoard[1][2] = itemBonus();
        testBoard[1][3] = 4;
        testBoard[1][4] = 2;

        testBoard[2][0] = itemBonus();
        testBoard[2][1] = 10;
        testBoard[2][2] = 11;
        testBoard[2][3] = 13;
        testBoard[2][4] = 3;
        return testBoard;
    }


    protected int[][] twoLineWin(){
        int[][] testBoard = new int[3][5];
        testBoard[0][0] = 3;
        testBoard[0][1] = 3;
        testBoard[0][2] = 14;
        testBoard[0][3] = 3;
        testBoard[0][4] = 2;

        testBoard[1][0] = 4;
        testBoard[1][1] = 4;
        testBoard[1][2] = 4;
        testBoard[1][3] = 2;
        testBoard[1][4] = 2;

        testBoard[2][0] = 15;
        testBoard[2][1] = 15;
        testBoard[2][2] = 15;
        testBoard[2][3] = 13;
        testBoard[2][4] = 12;
        return testBoard;
    }

    protected int[][] jackpotWinBoard(){
        int[][] testBoard = new int[3][5];
        testBoard[0][0] = 7;
        testBoard[0][1] = 9;
        testBoard[0][2] = 6;
        testBoard[0][3] = itemJackpot();
        testBoard[0][4] = itemJackpot();

        testBoard[1][0] = itemJackpot();
        testBoard[1][1] = itemJackpot();
        testBoard[1][2] = itemJackpot();
        testBoard[1][3] = itemJackpot();
        testBoard[1][4] = itemJackpot();

        testBoard[2][0] = 1;
        testBoard[2][1] = 7;
        testBoard[2][2] = 7;
        testBoard[2][3] = 8;
        testBoard[2][4] = 1;
        return testBoard;
    }


    protected int[][] willWinBoard(){
        int[][] testBoard = new int[3][5];
        testBoard[0][0] = 8;
        testBoard[0][1] = 9;
        testBoard[0][2] = 9;
        testBoard[0][3] = 6;
        testBoard[0][4] = 7;

        testBoard[1][0] = 1;
        testBoard[1][1] = 6;
        testBoard[1][2] = itemWill();
        testBoard[1][3] = itemWill();
        testBoard[1][4] = 8;

        testBoard[2][0] = itemWill();
        testBoard[2][1] = itemWill();
        testBoard[2][2] = 2;
        testBoard[2][3] = 12;
        testBoard[2][4] = 8;
        return testBoard;
    }

    protected int[][] freeSpinBoard(){
        int[][] testBoard = new int[3][5];
        testBoard[0][0] = 8;
        testBoard[0][1] = itemFreeSpin();
        testBoard[0][2] = 9;
        testBoard[0][3] = 6;
        testBoard[0][4] = itemFreeSpin();

        testBoard[1][0] = 1;
        testBoard[1][1] = 6;
        testBoard[1][2] = itemFreeSpin();
        testBoard[1][3] = itemWill();
        testBoard[1][4] = 8;

        testBoard[2][0] = 5;
        testBoard[2][1] = itemWill();
        testBoard[2][2] = 1;
        testBoard[2][3] = 12;
        testBoard[2][4] = 8;
        return testBoard;
    }

//    @Override
//    protected int[][] createRawBoard() {
//        resultItems = new ArrayList<>();
//        int[][] a = bonusWinBoard();
//        PrettyPrinter2DArray printer2DArray = new PrettyPrinter2DArray(System.out);
//        printer2DArray.print(a);
//        return a;
//    }

    public static void main(String[] args)
    {
//        User user = new User(19335);
////        SlotPlayer slotPlayer = new SlotPlayer();
////        slotPlayer.setUser(user);
////        slotPlayer.setRoom(
////                SlotOceanGame.getInstance()
////                        .getRoomById(SlotConst.SLOT_OCEAN_VIP_ROOM_ID)
////        );
//        Money money = new Money(1000, MoneyType.GOLD);
//        MoneyService.getInstance().addMoneyN(money, user);
//        SlotOceanGame.getInstance().addPlayer(user);
//        SlotOceanGame.getInstance().joinRoom(user, SlotOceanGame.getInstance().getRoomById(20003));
////        slotPlayer.getGoldStake().setAmount(100);
////        System.out.println(slotPlayer.getGoldStake().getAmount());
//        ArrayList<Integer> lines = new ArrayList<>();
////        lines.add(9);
////        lines.add(3);
////        lines.add(5);
//
//
//        for (int i = 1; i < 21; i++) {
//            lines.add(i);
//        }
////        lines.add(1);
//        SlotPlayer slotPlayer = SlotOceanGame.getInstance().getPlayer(user);
//        slotPlayer.updateLines(lines, SlotGame.lines);
//        long before = slotPlayer.getUser().getMoneyGold();
//        int numWinJackpot = 0;
//        int numWinBonus = 0;
//        int freeSpinCounter = 0;
//        int winCounter = 0;
//        int loseCounter = 0;
//        for (int i = 0; i < 10000; i++) {
//            if (i % 1000 == 0){
//                System.out.println(slotPlayer.getUser().getMoneyGold() + " - " + slotPlayer.getRoom().getRoomFund().getAmount() + " - " + slotPlayer.getRoom().getRoomPot().getAmount());
//            }
//            if (slotPlayer.isFreeSpin()){
//                freeSpinCounter++;
////                System.out.println("Free: " + freeSpinCounter + " - " + slotPlayer.getFreeSpin());
//            }
//            MainResultBoard resultBoard = SlotOceanGame.getInstance().spin(slotPlayer);
//            if (resultBoard.getMoneyWin() >= slotPlayer.getTotalStake().getAmount()){
//                winCounter++;
//            } else {
//                loseCounter++;
//            }
//
//            try {
//                if (resultBoard.getJackpotMoneyWin() > 0){
//                    System.out.println("WIN JACKPOT");
//                    System.out.println(i);
//                    System.out.println(resultBoard);
//                    numWinJackpot++;
//                }
//
//                if (resultBoard.getBonusWinResult().isWin()){
//                    if (resultBoard.getBonusWinResult().getBonusItemNumber() > 0){
//                        BonusWinResult bonusWinResult = resultBoard.getBonusWinResult();
//                        BonusWinResult.OpenedItems openedItems = bonusWinResult.getOpenedItems();
//                        bonusWinResult.openItem(0,1);
//                        bonusWinResult.openItem(1,1);
//                        System.out.println("WIN BONUS");
//                        //System.out.println(i);
//                        //System.out.println(resultBoard);
//                        numWinBonus++;
//                    } else {
//                        System.out.println("NO WIN BONUS????");
//                    }
//
//
//
//                }
//            } catch (Exception ex) {
//                System.out.println("Bonus error - " + ex.getStackTrace());
//            }
//
//        }
//        System.out.println("Total free spin: " + freeSpinCounter);
//        System.out.println("Num win jackpot: " + numWinJackpot);
//        System.out.println("Num win Bonus: " + numWinBonus);
//        long after = slotPlayer.getUser().getMoneyGold();
//        System.out.println(String.format("Before %d, After %d", before, after));
//
////        for (int i = 0; i < 45; i++) {
////            MainResultBoard mainResultBoard = new MainResultBoard(slotPlayer, SlotGame.config);
////            int counter = 1;
////            while (mainResultBoard.getJackpotMoneyWin() <= 0){
////                counter++;
////                mainResultBoard = new MainResultBoard(slotPlayer, SlotGame.config);
////            }
////            System.out.println(mainResultBoard.printRawBoardCSV());
////            System.out.println(counter);
////            System.out.println(mainResultBoard.toString());
////        }
//
//
////        for (int i = 1; i < 21; i++) {
////            lines.add(i);
////        }
////
///*        for (int i = 0; i < 1; i++) {
//            MainResultBoard resultBoard = new MainResultBoard(slotPlayer, SlotGame.config);
//            resultBoard.runRecheck();
//            long a = resultBoard.getMoneyWin();
//            resultBoard.getSpinResult().getListBonusWinResult().getMergedProto();
//            System.out.println(resultBoard.toString());
//
//        }*/
    }
}
