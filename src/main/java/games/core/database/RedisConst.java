package games.core.database;

public interface RedisConst {
    String RD_PREFIX = "fish_";
    String RD_CONFIG_FUND_ROOM_KEY = RD_PREFIX + "config_fund_room";
    String RD_FUND_KEY ="fund";
    String RD_BASE_FUND_KEY = "baseFund";

    String RD_CONFIG_FUND_DEALER_ROOM_KEY = RD_PREFIX + "config_fund_dealer_room";
    String RD_FUND_DEALER_KEY ="fundDealer";

    String RD_CONFIG_JACK_POT_ROOM_KEY = RD_PREFIX + "config_jack_pot_room";
    String RD_JACK_POT_KEY = "jackPot";
    String RD_RATE_FUND_FOR_JACK_POT_KEY = "rateFundForJackPot";
    String RD_RATE_BASE_RECIEVE_JACK_POT = "rateBaseRecieveJackPot";
}
