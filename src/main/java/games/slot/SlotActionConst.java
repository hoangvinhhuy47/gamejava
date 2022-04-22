package games.slot;

public interface SlotActionConst
{
    int plus = 179;

    int GAME_INFO = 21 + plus;
    int SPIN = 22 + plus;
    int PLAYER_UPDATE_LINES = 23 + plus;
    int PLAYER_HISTORY = 24 + plus;
    int JOIN_ACTION = 1 + plus;
    int LEAVE_ACTION = 2 + plus;
    int JOIN_ROOM = 11 + plus;
    int LEAVE_ROOM = 12 + plus;
    int INCREASE_STAKE_STEP = 13 + plus;
    int DECREASE_STAKE_STEP = 14 + plus;
    int ROOM_POT_INFO = 15 + plus;
    int RANKING_MONEY_WIN = 16 + plus;
    int CHANGE_ROOM = 17 + plus;
    int OPEN_BONUS = 25 + plus;
}
