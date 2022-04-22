package games.slot.data;

import libs.util.LoadConfig;
import libs.util.data.DataLoader;
import libs.util.data.HashByInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Integer.parseInt;

public class SlotConfig extends DataLoader implements DataFilePath{
    private HashByInt<MainSlotLine> slotOceanLines = new HashByInt<>(20);

    private HashByInt<SlotReel> slotOceanReels = new HashByInt<>(5);

    private HashByInt<MainSlotReward> slotOceanRewards = new HashByInt<>(12);

    private ArrayList<int[][]> listMainJackpotRawBoard = new ArrayList<>();

    private HashByInt<ArrayList<int[][]>> hashMainJackpotRawBoard = new HashByInt<>();

    private SpecialItem specialItem;

    private Random randomGenerator = new Random();

    private static SlotConfig instance;

    private BonusConfig bonusConfig;

    public static SlotConfig getInstance() {
        if (instance == null)
            instance = new SlotConfig();
        return instance;
    }

    public static void main(String[] args) {
        SlotConfig slotConfig = SlotConfig.getInstance();
        BonusConfig bonusConfig = slotConfig.getBonusConfig();
        double a = slotConfig.getBonusConfig().getBaseNumber();
        System.out.println(a);
//        System.out.println(Arrays.deepToString(slotConfig.listMainJackpotRawBoard.toArray()));

    }

    private SlotConfig() {
        loadSlotOceanLines();
        loadSlotOceanReel();
        loadSlotOceanReward();
        loadSpecialItem();
        loadMainJackpotRawBoard();
        bonusConfig = new BonusConfig();
    }

    public SpecialItem getSpecialItem() {
        return specialItem;
    }

    private void loadSpecialItem() {
        this.loadDataFromFileToObject(SLOT_SPECIAL_FILE_PATH, strings -> {
            this.specialItem = new SpecialItem(strings);
        });
    }

    private void loadSlotOceanLines() {
        this.loadDataFromFileToObject(SLOT_OCEAN_LINE_FILE_PATH, strings -> {
            MainSlotLine mainSlotLine = new MainSlotLine(strings);
            this.slotOceanLines.put(mainSlotLine.getId(), mainSlotLine);
        });
    }

    private void loadSlotOceanReel() {
        this.loadDataFromFileToObject(SLOT_OCEAN_REEL_FILE_PATH, strings -> {
            SlotReel slotReel = new SlotReel(strings);
            this.slotOceanReels.put(slotReel.getId(), slotReel);
        });
    }

    private void loadSlotOceanReward() {
        this.loadDataFromFileToObject(SLOT_OCEAN_REWARD_FILE_PATH, strings ->
        {
            MainSlotReward slotAReward = new MainSlotReward(strings);
            this.slotOceanRewards.put(slotAReward.getItemId(), slotAReward);
        });
    }

    private void loadMainJackpotRawBoard(){
        this.loadDataFromFileToObject(SLOT_MAIN_JACKPOT_RAW_BOARD_FILE_PATH, strings -> {
            int[][] rawBoard = parseMainJackpotRawBoard(strings);
            listMainJackpotRawBoard.add(rawBoard);
            int lineId = parseInt(strings[0]);
            ArrayList<int[][]> listRawBoard = hashMainJackpotRawBoard.computeIfAbsent(lineId, k -> new ArrayList<>());
            listRawBoard.add(rawBoard);
        });
    }

    public HashByInt<MainSlotLine> getSlotOceanLines() {
        return slotOceanLines;
    }

    public HashByInt<SlotReel> getSlotOceanReels() {
        return slotOceanReels;
    }

    public HashByInt<MainSlotReward> getSlotOceanRewards() {
        return slotOceanRewards;
    }

    private int[][] parseMainJackpotRawBoard(String[] strings){
        int[][] rawBoard = new int[3][5];
        rawBoard[0][0] = parseInt(strings[1]);
        rawBoard[0][1] = parseInt(strings[2]);
        rawBoard[0][2] = parseInt(strings[3]);
        rawBoard[0][3] = parseInt(strings[4]);
        rawBoard[0][4] = parseInt(strings[5]);

        rawBoard[1][0] = parseInt(strings[6]);
        rawBoard[1][1] = parseInt(strings[7]);
        rawBoard[1][2] = parseInt(strings[8]);
        rawBoard[1][3] = parseInt(strings[9]);
        rawBoard[1][4] = parseInt(strings[10]);

        rawBoard[2][0] = parseInt(strings[11]);
        rawBoard[2][1] = parseInt(strings[12]);
        rawBoard[2][2] = parseInt(strings[13]);
        rawBoard[2][3] = parseInt(strings[14]);
        rawBoard[2][4] = parseInt(strings[15]);
        return rawBoard;
    }

    public int[][] getRandomMainJackpotRawBoard(){
        int index = randomGenerator.nextInt(listMainJackpotRawBoard.size());
        return listMainJackpotRawBoard.get(index);
    }

    public int[][] getRandomMainJackpotRawBoardByLines(ArrayList<MainSlotLine> lines){
        int randLineIndex = randomGenerator.nextInt(lines.size());
        MainSlotLine line = lines.get(randLineIndex);
        ArrayList<int[][]> listRawBoard = hashMainJackpotRawBoard.get(line.getId());
        int randRawBoardIndex = randomGenerator.nextInt(listRawBoard.size());
        return listRawBoard.get(randRawBoardIndex);
    }

    public class BonusConfig{
        LoadConfig loadConfig;
        private int baseNumber;
        private int bigWinNumber;
        private int emptyNumber;
        private int base3Multiple;
        private int base3Multiple2;
        private int base3Multiple3;
        private double percentBase3Multiple;
        private double percentBase3Multiple2;
        private double percentBase3Multiple3;
        private int base4Multiple;
        private int base5Multiple;
        private int bigWinMultiplePaddingMin;
        private int bigWinMultiplePaddingMax;
        private int rate3Bonus;
        private int rate4Bonus;
        private int rate5Bonus;


        BonusConfig(){
            loadConfig = new LoadConfig(SLOT_BONUS_CONFIG_FILE_PATH);
            baseNumber = (int) Math.round(loadConfig.getValue("base_number", Double.class));
            bigWinNumber = (int) Math.round(loadConfig.getValue("big_win_number", Double.class));
            emptyNumber = (int) Math.round(loadConfig.getValue("empty_number", Double.class));
            base3Multiple = (int) Math.round(loadConfig.getValue("base_3_multiple", Double.class));
            base3Multiple2 = (int) Math.round(loadConfig.getValue("base_3_multiple_2", Double.class));
            base3Multiple3 = (int) Math.round(loadConfig.getValue("base_3_multiple_3", Double.class));
            percentBase3Multiple = Math.round(loadConfig.getValue("percent_base_3_multiple", Double.class));
            percentBase3Multiple2 = Math.round(loadConfig.getValue("percent_base_3_multiple_2", Double.class));
            percentBase3Multiple3 = Math.round(loadConfig.getValue("percent_base_3_multiple_3", Double.class));
            base4Multiple = (int) Math.round(loadConfig.getValue("base_4_multiple", Double.class));
            base5Multiple = (int) Math.round(loadConfig.getValue("base_5_multiple", Double.class));
            bigWinMultiplePaddingMin = (int) Math.round(loadConfig.getValue("big_win_multiple_padding_min", Double.class));
            bigWinMultiplePaddingMax = (int) Math.round(loadConfig.getValue("big_win_multiple_padding_max", Double.class));
            rate3Bonus = (int) Math.round(loadConfig.getValue("rate_3_bonus", Double.class));
            rate4Bonus = (int) Math.round(loadConfig.getValue("rate_4_bonus", Double.class));
            rate5Bonus = (int) Math.round(loadConfig.getValue("rate_5_bonus", Double.class));

        }

        public int getBaseNumber() {
            return baseNumber;
        }

        public int getBigWinNumber() {
            return bigWinNumber;
        }


        public int getEmptyNumber() {
            return emptyNumber;
        }

        public int getBase3Multiple() {
            return base3Multiple;
        }

        public int getBase3Multiple2() {
            return base3Multiple2;
        }

        public int getBase3Multiple3() {
            return base3Multiple3;
        }

        public double getPercentBase3Multiple() {
            return percentBase3Multiple;
        }

        public double getPercentBase3Multiple2() {
            return percentBase3Multiple2;
        }

        public double getPercentBase3Multiple3() {
            return percentBase3Multiple3;
        }

        public int getBase4Multiple() {
            return base4Multiple;
        }

        public int getBase5Multiple() {
            return base5Multiple;
        }

        public int getBigWinMultiplePaddingMin() {
            return bigWinMultiplePaddingMin;
        }

        public int getBigWinMultiplePaddingMax() {
            return bigWinMultiplePaddingMax;
        }

        public int getRate3Bonus() {
            return rate3Bonus;
        }

        public int getRate4Bonus() {
            return rate4Bonus;
        }

        public int getRate5Bonus() {
            return rate5Bonus;
        }
    }

    public BonusConfig getBonusConfig() {
        return bonusConfig;
    }
}
