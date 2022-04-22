package games.ThreeCard;

import games.ThirteenKiller.TKRoom;
import games.core.datatype.BasePlayer;

public class TCPlayer extends BasePlayer
{
    private TCRoom tkRoom;

    public boolean isFinishing;

    public boolean isLoser;

    public boolean isPass;

    public boolean isMasterClient;

    public long winMoney;

    public boolean isLostFour;

    public int idAvatar = 0;

    public int id;

    public long totalBet;

    public boolean isReady;

    public int timeToBet;

    public TCPlayer(int id)
    {
        this.id = id;
    }

    public TCPlayer()
    {

    }

    @Override
    public boolean isPlaying()
    {
        return false;
    }

    public void SetRoom(TCRoom room)
    {
        tkRoom = room;
    }

    public TCRoom GetRoom()
    {
        return tkRoom;
    }

}
