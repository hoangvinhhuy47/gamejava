package games.slot;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import games.APISendMoney;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.constant.ServerCommandConstant;
import games.core.exception.BaseException;
import games.core.exception.game.NotFoundPlayerInGame;
import games.core.exception.game.PlayerNotEnoughMoney;
import games.core.game.GameErrorCode;
import games.slot.data.ResultBoard;
import games.slot.exception.ItemBonusIsOpened;
import games.slot.exception.SlotErrorCode;
import games.slot.room.SlotRoom;
import message.SlotGameProto;
import message.XProtos;

import java.io.IOException;


public abstract class SlotCommand extends ICommand
{

    private SlotGame gameInstance;

    SlotCommand(SlotGame gameInstance){
        this.gameInstance = gameInstance;
    }

    @Override
    public int getCommandCode() {
        return ServerCommandConstant.SLOT_OCEAN_COMMAND;
    }

    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        if (ctx.checkAuth()){
            int type = message.getBeanType();
            switch (type){
                case SlotActionConst.GAME_INFO:
                    this.handleGameInfo(ctx, message);
                    break;
                case SlotActionConst.PLAYER_HISTORY:
                    break;
                case SlotActionConst.PLAYER_UPDATE_LINES:
//                    this.handleUpdateLine(ctx, message);
                    break;
                case SlotActionConst.SPIN:
                    this.handleSpin(ctx, message);
                    break;
                case SlotActionConst.OPEN_BONUS:
                    this.handleOpenBonus(ctx, message);
                    break;
                case SlotActionConst.JOIN_ACTION:
                    this.handleJoin(ctx, message);
                    break;
                case SlotActionConst.LEAVE_ACTION:
                    this.handleLeave(ctx, message);
                    break;
                case SlotActionConst.CHANGE_ROOM:
                    this.handleChangeRoom(ctx, message);
                    break;
                case SlotActionConst.INCREASE_STAKE_STEP:
//                    this.handleIncreaseStakeStep(ctx, message);
                    break;
                case SlotActionConst.DECREASE_STAKE_STEP:
//                    this.handleDecreaseStakeStep(ctx, message);
                    break;
                case SlotActionConst.ROOM_POT_INFO:
                    break;
                case SlotActionConst.RANKING_MONEY_WIN:
                    break;
            }
        }
    }

    private void handleGameInfo(ChannelContextInfo ctx, XProtos.XMessage message){
        pushToClient(ctx, SlotActionConst.GAME_INFO, getGameInstance().parseProtoBuilder());
    }

    private void handleUpdateLine(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        SlotGameProto.PlayerUpdateLinesRequest request = SlotGameProto.PlayerUpdateLinesRequest.parseFrom(message.getData());
        gameInstance.updatePlayerLines(getPlayer(ctx), request.getLineList());

        SlotGameProto.PlayerUpdateLinesResponse.Builder response = SlotGameProto.PlayerUpdateLinesResponse.newBuilder();
        response.addAllLine(request.getLineList());
        pushToClient(ctx, SlotActionConst.PLAYER_UPDATE_LINES, response);
    }

    private void handleSpin(ChannelContextInfo ctx, XProtos.XMessage message){
        Message responseMessage;
        try {
            SlotGameProto.SpinSlotRequest request = SlotGameProto.SpinSlotRequest.parseFrom(message.getData());
            SlotPlayer player = getPlayer(ctx);

            // Nếu nó đang được freeSpin thì không cho updateLine
            if (!player.isFreeSpin()){
                player.updateLines(request.getLineList(), gameInstance.getLines());
            }
            player.setStakeStep(1);
            ResultBoard resultBoard = gameInstance.spin(getPlayer(ctx));
//            getLogger().info(resultBoard.toString());
            responseMessage = resultBoard.getProtoMessage();
        } catch (PlayerNotEnoughMoney e){
            responseMessage = ResultBoard.getErrorProto(e.getErrorCode());
        } catch (Exception e){
            getLogger().error(
                    String.format("User[%s] spin error", ctx.getUserId()),
                    e
            );
            responseMessage = ResultBoard.getErrorProto(SlotErrorCode.PLAYER_CANNOT_SPIN);
        } finally {
            getPlayer(ctx).resetJackpotAmount();
        }
        pushToClient(ctx, SlotActionConst.SPIN, responseMessage);
    }

    private void handleOpenBonus(ChannelContextInfo ctx, XProtos.XMessage message){
        Message responseMessage;
        try {
            SlotGameProto.OpenBonusItemRequest request = SlotGameProto.OpenBonusItemRequest.parseFrom(message.getData());
            SlotPlayer player = getPlayer(ctx);
            ResultBoard.BonusWinResult bonusWinResult = player.getBonusWinResult();
            if (player.getBonusWinResult() != null) {
                if (request.getOpenAll() != 0) {
                    bonusWinResult.openAll();
                    player.executeBonusWinResult();
                } else {
                    bonusWinResult.openItem(request.getRow(), request.getCol());
                    if (!bonusWinResult.canOpen()){
                        player.executeBonusWinResult();
                    }
                }
            }
            responseMessage = bonusWinResult.getOpenedItems().getProtoMessage();
        } catch (BaseException e){
            responseMessage = ResultBoard.getBonusResultAllErrorProto(e.getErrorCode());
        } catch (Exception e){
            getLogger().error(
                    String.format("User[%s] open bonus error", ctx.getUserId()),
                    e
            );
            responseMessage = ResultBoard.getBonusResultAllErrorProto(SlotErrorCode.PLAYER_CANNOT_OPEN_BONUS);
        }
        pushToClient(ctx, SlotActionConst.OPEN_BONUS, responseMessage);
    }


    private void handleJoin(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        SlotGameProto.SlotJoinRequest request = SlotGameProto.SlotJoinRequest.parseFrom(message.getData());
        SlotRoom room = gameInstance.getRoomById(request.getRoomId());
        long userGold = ctx.getUser().getMoneyGold();
        SlotGameProto.SlotRoomInfo.Builder roomInfo = room.parseProtoBuilder();
        //roomInfo.setErrorCode(GameErrorCode.GAME_NOT_ACTIVE);
        if (userGold < room.getRoomStake().getMoneyByStep(1)){
            roomInfo.setErrorCode(GameErrorCode.PLAYER_NOT_ENOUGH_MONEY);
        } else {
            try {
                gameInstance.addPlayer(ctx.getUser());
                gameInstance.joinRoom(ctx.getUser(), room);
                // Reset freeSpin khi join để tránh trường hợp nó cheat không leave mà joinRoom
                SlotPlayer slotPlayer = getPlayer(ctx);
                slotPlayer.resetFreeSpin();
            } catch (Exception e){
                roomInfo.setErrorCode(GameErrorCode.ERROR_NOT_DEFINE);
                getLogger().error(String.format(
                        "User[%d] join slot room[%d] fail",
                        ctx.getUserId(), request.getRoomId()), e
                );
            }
        }
        pushToClient(ctx, SlotActionConst.JOIN_ACTION, roomInfo);
    }

    private void handleLeave(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        APISendMoney.postWithJson(APISendMoney.urlAPI ,APISendMoney.JsonObjectToString(
                ctx.getUser().getUserName(),
                Long.toString(ctx.getUser().getMoneyGold()),0,""));
        SlotGameProto.LeaveGameResponse.Builder builder = SlotGameProto.LeaveGameResponse.newBuilder();
        builder.setErrorCode(0);
        try {
            // reset free spin khi leave
            SlotPlayer player = getPlayer(ctx);
            player.resetFreeSpin();

            gameInstance.leaveRoom(ctx.getUser());
            gameInstance.removePlayer(ctx.getUser());

        } catch (NotFoundPlayerInGame notFoundPlayerInGame){
            builder.setErrorCode(SlotErrorCode.PLAYER_NOT_FOUND_IN_GAME);
        } catch (Exception e){
            builder.setErrorCode(SlotErrorCode.ERROR_NOT_DEFINE);
            getLogger().error(String.format(
                    "User[%d] leave slot room fail",
                    ctx.getUserId()), e
            );
        }
        pushToClient(ctx, SlotActionConst.LEAVE_ACTION, builder);
    }

    private void handleChangeRoom(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        SlotGameProto.SlotJoinRequest request = SlotGameProto.SlotJoinRequest.parseFrom(message.getData());
        SlotRoom room = gameInstance.getRoomById(request.getRoomId());
        SlotPlayer slotPlayer = getPlayer(ctx);

        // Không cho changeRoom khi đang freeSpin
        if (slotPlayer.isFreeSpin())
        {
            SlotGameProto.SlotRoomInfo.Builder roomBuilder = room.parseProtoBuilder();
            roomBuilder.setErrorCode(SlotErrorCode.PLAYER_CANNOT_CHANGE_ROOM_IN_FREE_SPIN);
            pushToClient(ctx, SlotActionConst.CHANGE_ROOM, roomBuilder);
            return;
        }
        try
        {
            gameInstance.leaveRoom(ctx.getUser());
        }
        catch (Exception e)
        {
            getLogger().error(
                    String.format(
                            "Change room: Player[%s] Can not leave room before join",
                            ctx.getUserId()
                    ), e
            );
        }
        gameInstance.joinRoom(ctx.getUser(), room);
        pushToClient(ctx, SlotActionConst.CHANGE_ROOM, room.parseProtoBuilder());
    }

    private void handleIncreaseStakeStep(ChannelContextInfo ctx, XProtos.XMessage message){
        getPlayer(ctx).increaseStakeStep();
        pushToClient(ctx, SlotActionConst.INCREASE_STAKE_STEP);
    }

    private void handleDecreaseStakeStep(ChannelContextInfo ctx, XProtos.XMessage message){
        getPlayer(ctx).decreaseStakeStep();
        pushToClient(ctx, SlotActionConst.DECREASE_STAKE_STEP);
    }

    private SlotGame getGameInstance() {
        return gameInstance;
    }

    public SlotPlayer getPlayer(ChannelContextInfo ctxInfo) throws NotFoundPlayerInGame {
        return gameInstance.getPlayer(ctxInfo.getUser());//(SlotPlayer) super.getPlayer(ctxInfo);
    }
}
