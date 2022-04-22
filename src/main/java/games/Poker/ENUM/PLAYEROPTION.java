package games.Poker.ENUM;

import games.Poker.Descreption_Anno;

public final class PLAYEROPTION {
    public static final int NOTHING = -1;
@Descreption_Anno(Name = "X")
    public static final int FOLD = 0;
@Descreption_Anno(Name = "1")
    public static final int CHECK = 1;
    public static final int CALL = 3;
@Descreption_Anno(Name = "2")
    public static final int BET = 2;
    public static final int RAISE = 4;
    public static final int ALL_IN = 5;


    public static final int OUT_OF_MONEY = 6;
}
