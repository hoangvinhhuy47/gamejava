package games.Poker.ENUM;

import games.Poker.Descreption_Anno;

public final class ACTIONPHASE {
@Descreption_Anno(Name = "WAITING")
    public static final int WAITING = 0;
@Descreption_Anno(Name = "PREFLOP")
    public static final int PREFLOP_SETDEALER = 11;
    public static final int PREFLOP_SETSMALLBLIND = 12;
    public static final int PREFLOP_SETBIGBLIND = 13;
    public static final int PREFLOP_2CARDS = 14;
    public static final int PREFLOP_POT = 19;
@Descreption_Anno(Name = "FLOP")
    public static final int FLOP_COMMUNITYCARDS_3 = 21;
@Descreption_Anno(Name = "TURN")
    public static final int TURN_COMMUNITYCARDS_4 = 31;
@Descreption_Anno(Name = "RIVER")
    public static final int RIVER_COMMUNITYCARDS_5 = 41;
@Descreption_Anno(Name = "SHOWNDOWN")
    public static final int SHOWDOWN = 41;
}
