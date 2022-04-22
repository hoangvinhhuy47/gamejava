package games.ThirteenKiller;

import games.core.datatype.BasePlayer;

public class TKPlayer extends BasePlayer
{
    private TKRoom tkRoom;

    public int timerCountdown;

    public float timeToThrowCard;

    public boolean isFinishing;

    public boolean isLoser;

    public boolean isPass;

    public boolean isMasterClient;

    public long winMoney;

    public boolean isLostFour;

    public int idAvatar = 0;

    @Override
    public boolean isPlaying()
    {
        return false;
    }

    public void SetRoom(TKRoom room)
    {
        tkRoom = room;
    }

    public TKRoom GetRoom()
    {
        return tkRoom;
    }

}
