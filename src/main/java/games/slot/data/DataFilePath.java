package games.slot.data;

public interface DataFilePath {
    String BASE_PATH = "config/";
    String SLOT_PATH = BASE_PATH + "slot/";

    //SLOT
    String SLOT_OCEAN_LINE_FILE_PATH = SLOT_PATH + "ocean/lines.csv";
    String SLOT_OCEAN_REEL_FILE_PATH = SLOT_PATH + "ocean/reels.csv";
    String SLOT_OCEAN_REWARD_FILE_PATH = SLOT_PATH + "ocean/rewards.csv";
    String SLOT_SPECIAL_FILE_PATH = SLOT_PATH + "special_item.csv";
    String SLOT_MAIN_JACKPOT_RAW_BOARD_FILE_PATH = SLOT_PATH + "main_jackpot_raw_board.csv";
    String SLOT_BONUS_CONFIG_FILE_PATH = SLOT_PATH + "bonus_config.json";
}
