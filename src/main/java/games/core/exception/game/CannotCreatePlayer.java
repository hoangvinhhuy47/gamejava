package games.core.exception.game;

import games.core.exception.BaseException;
import games.core.game.GameErrorCode;

public class CannotCreatePlayer extends BaseException {
    public CannotCreatePlayer() {

    }
    public CannotCreatePlayer(String name, String container) {
        super(GameErrorCode.CANNOT_CREATE_PLAYER,
                String.format("Cannot create {%s} in {%s}",
                        name, container));
    }
    public CannotCreatePlayer(Object obj, Object container) {
        super(GameErrorCode.CANNOT_CREATE_PLAYER,
                String.format("Cannot create {%s} in {%s}",
                        obj.getClass().getSimpleName(), container.getClass().getSimpleName()));
    }
}
