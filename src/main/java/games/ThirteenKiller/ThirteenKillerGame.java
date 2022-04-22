package games.ThirteenKiller;

import com.google.protobuf.ByteString;
import games.core.application.handler.ChannelContextInfo;
import games.core.datatype.HashPlayer;
import games.core.exception.game.CannotRemovePlayerInRoom;
import games.core.exception.game.NotFoundPlayerInGame;
import games.core.exception.game.NotFoundPlayerInRoom;
import games.core.exception.game.RoomNotExist;
import games.core.user.User;
import games.shootingfish.datatype.HashByInt;
import message.XProtos;

import java.util.ArrayList;
import java.util.List;

import static games.core.constant.ServerCommandConstant.THIRTEENKILLER_COMMAND;

public class ThirteenKillerGame
{
    private static ThirteenKillerGame instance;
    private TKPlayerPool playerPool = new TKPlayerPool();

    private HashByInt<TKRoom> rooms = new HashByInt<TKRoom>();
    private HashPlayer<TKPlayer> players = new HashPlayer<>();
    private HashPlayer<TKPlayer> playersInLobby = new HashPlayer<>();

    public List<Integer> idOfDeletedRooms = new ArrayList<>();

    public static ThirteenKillerGame GetInstance()
    {
        if (instance == null)
            instance = new ThirteenKillerGame();

        return instance;
    }

    public void AddRoom(User masterClient, int idRoom, long betLevel, String tableOwner, int numberPlayers, int playerJoined)
    {
        if (!rooms.containsKey(idRoom))
        {
            TKPlayer master_Client = playersInLobby.get(masterClient.getId());
            if (master_Client == null)
                master_Client = CreatePlayer(masterClient);

            master_Client.isMasterClient = true;
            rooms.put(idRoom, new TKRoom(master_Client, idRoom, betLevel, tableOwner, numberPlayers, playerJoined));
        }
    }

    public HashByInt<TKRoom> GetRoom()
    {
        return rooms;
    }

    public TKRoom GetRoomRandom(long goldOfUser)
    {
        for (TKRoom room : rooms.values())
        {
            if (goldOfUser >= (room.betLevel * 5) && room.maxPlayer > room.playersJoinedRoom)
                return room;
        }

        return null;
    }

    public void RemoveRoom(int roomId)
    {
        if (rooms.containsKey(roomId))
        {
            TKRoom room = rooms.get(roomId);

            boolean havePlayers = false;

            for (TKPlayer player : room.players.values())
            {
                if (player.getUser() != null)
                {
                    havePlayers = true;
                    break;
                }
            }

            if (!havePlayers)
            {
                rooms.remove(roomId);
                idOfDeletedRooms.add(roomId);
            }
        }
    }

    public void SendToAll(ByteString responseBeanByteString, int responseCode)
    {
        for (TKPlayer player : players.values())
        {
            if (player != null)
            {
                SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
            }
        }
    }


    public  void SendToAllLobby(ByteString message, int responseCode)
    {
        for (TKPlayer player : playersInLobby.values())
        {
            if (player != null)
            {
                SendMessage(player.getCtxInfo(), message, responseCode);
            }
        }
    }

    public void SendMessage(ChannelContextInfo ctx, ByteString responseBeanByteString, int responseCode) {
        XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder();
        responseMessage.setCommand(THIRTEENKILLER_COMMAND)
                .setBeanType(responseCode)
                .setData(responseBeanByteString);

        ctx.writeAndFlush(responseMessage.build());
    }

    public TKRoom getRoomByID(int roomId)
    {
        TKRoom room = rooms.get(roomId);
        if (room == null) {
            throw new RoomNotExist(roomId);
        }
        return  room;
    }


    public void AddPlayer(User user)
    {
        if (!players.containsKey(user.getId()))
        {
            TKPlayer newPlayer = CreatePlayer(user);
            players.put(newPlayer.getId(), newPlayer);
        }
        else
        {
            TKPlayer player = GetPlayerLobbỵ̣(user.getId());

            if (player == null)
                player = CreatePlayer(user);

            players.replace(player.getId(), player);
        }

    }

    public void AddPlayerOnLobby(TKPlayer nPlayer)
    {
        User user =  nPlayer.getUser();
        if (!playersInLobby.containsKey(user.getId()))
        {
            TKPlayer player = CreatePlayer(user);
            playersInLobby.put(player.getId(), player);
        }

        else
        {
            TKPlayer newPlayer = GetPlayerLobbỵ̣(user.getId());
            playersInLobby.replace(newPlayer.getId(), newPlayer);
        }
    }

    public void RemovePlayerOnLobby(User user)
    {
        if (playersInLobby.containsKey(user.getId()))
        {
            playersInLobby.remove(user.getId());
        }
    }

    TKPlayer CreatePlayer(User user)
    {
        TKPlayer player = playerPool.shiftSync();
        player.setUser(user);

        return  player;
    }

    private TKPlayer GetPlayer(User user)
    {
        if (!players.containsKey(user.getId()))
            throw new NotFoundPlayerInGame(user);

        return players.get(user.getId());
    }

    public TKPlayer GetPlayer(int userId)
    {
        if (players.containsKey(userId))
            return players.get(userId);

        return null;
    }

    public TKPlayer GetPlayerLobbỵ̣(int userId)
    {
        if (playersInLobby.containsKey(userId))
            return playersInLobby.get(userId);

        return null;
    }


    public void RemovePlayer(User user)
            throws NotFoundPlayerInGame
    {
        playerPool.put(GetPlayer(user));
        players.remove(GetPlayer(user));
    }

    public void JoinRoom(User user, TKRoom room)
    {
        TKPlayer player = GetPlayer(user);
        room.AddPlayerToRoom(player);
        player.SetRoom(room);
    }

    public void LeaveRoom(User user)
            throws NotFoundPlayerInGame,
                   NotFoundPlayerInRoom,
                   CannotRemovePlayerInRoom
    {
        TKPlayer player = GetPlayer(user);
        TKRoom room = player.GetRoom();
        room.RemovePlayerFromRoom(player);

        if (player.isMasterClient)
        {
            TKPlayer newPlayer = room.GetPlayerOfNextTurn(user.getId());
            if (newPlayer != null)
            {
                newPlayer.isMasterClient = true;
                room.masterClient = newPlayer;
            }
        }
    }


}
