package games.core.exception.game;

import games.core.exception.BaseException;

import static games.core.game.GameErrorCode.ROOM_NOT_EXIST_IN_GAME;

public class RoomNotExist extends BaseException {
    public RoomNotExist(int roomId){
        super(ROOM_NOT_EXIST_IN_GAME, String.format("Room[%s] not exist in Games", roomId));
    }
}
