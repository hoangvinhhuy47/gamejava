package games.ThreeCard;

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
import static games.core.constant.ServerCommandConstant.THREE_CARD_COMMAND;

public class ThreeCardGame
{
    private static ThreeCardGame instance;
    private TCPlayerPool playerPool = new TCPlayerPool();

    private HashByInt<TCRoom> rooms = new HashByInt<>();
    private HashPlayer<TCPlayer> players = new HashPlayer<>();
    private HashPlayer<TCPlayer> playersInLobby = new HashPlayer<>();

    public List<Integer> idOfDeletedRooms = new ArrayList<>();

    public static ThreeCardGame GetInstance()
    {
        if (instance == null)
            instance = new ThreeCardGame();

        return instance;
    }

    public void AddRoom(User masterClient, int idRoom, long betLevel, String tableOwner, int numberPlayers, int playerJoined)
    {
        if (!rooms.containsKey(idRoom))
        {
            TCPlayer master_Client = playersInLobby.get(masterClient.getId());
            if (master_Client == null)
                master_Client = CreatePlayer(masterClient);

            master_Client.isMasterClient = true;
            rooms.put(idRoom, new TCRoom(master_Client, idRoom, betLevel, tableOwner, numberPlayers, playerJoined));
        }
    }

    public HashByInt<TCRoom> GetRoom()
    {
        return rooms;
    }

    public TCRoom GetRoomRandom(long goldOfUser)
    {
        for (TCRoom room : rooms.values())
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
            TCRoom room = rooms.get(roomId);

            boolean havePlayers = false;

            for (TCPlayer player : room.players.values())
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
        for (TCPlayer player : players.values())
        {
            if (player != null)
            {
                SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
            }
        }
    }


    public  void SendToAllLobby(ByteString message, int responseCode)
    {
        for (TCPlayer player : playersInLobby.values())
        {
            if (player != null)
            {
                SendMessage(player.getCtxInfo(), message, responseCode);
            }
        }
    }

    public void SendMessage(ChannelContextInfo ctx, ByteString responseBeanByteString, int responseCode) {
        XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder();
        responseMessage.setCommand(THREE_CARD_COMMAND)
                .setBeanType(responseCode)
                .setData(responseBeanByteString);

        ctx.writeAndFlush(responseMessage.build());
    }

    public TCRoom getRoomByID(int roomId)
    {
        TCRoom room = rooms.get(roomId);
        if (room == null) {
            throw new RoomNotExist(roomId);
        }
        return  room;
    }


    public void AddPlayer(TCPlayer newPlayer)
    {
        if (!players.containsKey(newPlayer.getId()))
        {
            players.put(newPlayer.getId(), newPlayer);
        }
        else
        {
            players.replace(newPlayer.getId(), newPlayer);
        }

    }

    public void AddPlayerOnLobby(TCPlayer nPlayer)
    {
        User user =  nPlayer.getUser();
        if (!playersInLobby.containsKey(user.getId()))
        {
            TCPlayer player = CreatePlayer(user);
            playersInLobby.put(player.getId(), player);
        }

        else
        {
            TCPlayer newPlayer = GetPlayerLobbỵ̣(user.getId());
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

    TCPlayer CreatePlayer(User user)
    {
        TCPlayer player = playerPool.shiftSync();
        player.setUser(user);

        return  player;
    }

    private TCPlayer GetPlayer(User user)
    {
        if (!players.containsKey(user.getId()))
            throw new NotFoundPlayerInGame(user);

        return players.get(user.getId());
    }

    public TCPlayer GetPlayer(int userId)
    {
        if (players.containsKey(userId))
            return players.get(userId);

        return null;
    }

    public TCPlayer GetPlayerLobbỵ̣(int userId)
    {
        if (playersInLobby.containsKey(userId))
            return playersInLobby.get(userId);

        return null;
    }


    public void RemovePlayer(TCPlayer player)
            throws NotFoundPlayerInGame
    {
        playerPool.put(player);
        players.remove(player);
    }

    public void JoinRoom(TCPlayer player, TCRoom room)
    {
        room.AddPlayerToRoom(player);
        player.SetRoom(room);
    }

    public void LeaveRoom(TCRoom room, TCPlayer player, int userID)
            throws NotFoundPlayerInGame,
                   NotFoundPlayerInRoom,
                   CannotRemovePlayerInRoom
    {
        if (player.isMasterClient)
        {
            TCPlayer newPlayer = room.GetPlayerOfNextTurn(userID);
            if (newPlayer != null)
            {
                newPlayer.isMasterClient = true;
                room.masterClient = newPlayer;

                player.isMasterClient = false;
            }
        }

        room.RemovePlayerFromRoom(player);
    }


}
