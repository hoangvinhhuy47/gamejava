package games.core.application.handler;

import games.GameManager;
import games.Poker.PokerCommand;
import games.ThirteenKiller.ThirteenKillerCommand;
import games.ThreeCard.ThreeCardCommand;
import games.core.application.channel.ChannelService;
import games.core.application.constant.ICommand;
import games.core.common.CommonCommand;
import games.core.constant.ServerCommandConstant;
import games.core.user.UserCommand;
import games.features.friends.FriendsCommand;
import games.features.inbox.InboxCommand;
import games.features.rank.RankCommand;
import games.minigame.MiniGameCommand;
import games.payment.PaymentCommand;
import games.shootingfish.GameCommand;
import games.slot.SlotCommand;
import games.slot.SlotOceanCommand;
import games.slot.SlotThreeKingdomsCommand;
import games.tx.TXCommand;
import games.tx.TaiXiuCommand;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import libs.util.LogFactory;
import message.XProtos.XMessage;
import org.apache.log4j.Logger;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Created by Cuong Nguyen Cao on 8/8/17.
 */
@ChannelHandler.Sharable
public class HandlerChannelContext extends SimpleChannelInboundHandler<XMessage> {
    private static HandlerChannelContext instance;
    private ChannelService channelService;
    private static Logger logger = LogFactory.getLogger(HandlerChannelContext.class.getSimpleName());
    public static HandlerChannelContext getInstance() {
        if (instance == null) instance = new HandlerChannelContext();
        return instance;
    }

    private UserCommand userCommand;
    private GameCommand gameCommand;
    private RankCommand rankCommand;
    private InboxCommand inboxCommand;
    private MiniGameCommand miniGameCommand;
    private PaymentCommand paymentCommand;
    private FriendsCommand friendsCommand;
    private CommonCommand commonCommand;
    private SlotOceanCommand slotOceanCommand;
    private SlotThreeKingdomsCommand slotThreeKingdomsCommand;
    private TaiXiuCommand txCommand;
    private ThirteenKillerCommand thirteenKillerCommand;
    private ThreeCardCommand threeCardCommand;
    private PokerCommand pokerCommand;

    private HandlerChannelContext() {
        channelService = ChannelService.getInstance();
        userCommand = UserCommand.getInstance();
        gameCommand = GameCommand.getInstance();
        rankCommand = RankCommand.getInstance();
        inboxCommand = InboxCommand.getInstance();
        miniGameCommand = MiniGameCommand.getInstance();
        paymentCommand = PaymentCommand.getInstance();
        friendsCommand = FriendsCommand.getInstance();
        commonCommand = CommonCommand.getInstance();
        txCommand = txCommand.getInstance();

        txCommand.Start();

        slotOceanCommand = SlotOceanCommand.getInstance();
        slotThreeKingdomsCommand = SlotThreeKingdomsCommand.getInstance();

        thirteenKillerCommand = ThirteenKillerCommand.GetInstance();
        threeCardCommand = ThreeCardCommand.GetInstance();

        pokerCommand = pokerCommand.getInstance();
        pokerCommand.Start();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, XMessage msg) throws Exception {
        try {
            Map<ChannelHandlerContext, ChannelContextInfo> listActiveChannel = channelService.getListActiveChannel();
            if (!listActiveChannel.containsKey(ctx)){
                listActiveChannel.put(ctx, new ChannelContextInfo(ctx, false));
            }

            processMessage(listActiveChannel.get(ctx), msg);
        } catch (Exception e){
            logger.error(
                    String.format("channelRead0: Command %d, action %d", msg.getCommand(), msg.getBeanType()),
                    e
            );
        }

    }

    public void onWebChannelForwarder(ChannelHandlerContext ctx, XMessage message)
    {
        try
        {
            Map<ChannelHandlerContext, ChannelContextInfo> listActiveChannel = channelService.getListActiveChannel();
            if (!listActiveChannel.containsKey(ctx)){
                listActiveChannel.put(ctx, new ChannelContextInfo(ctx, true));
            }

            processMessage(listActiveChannel.get(ctx), message);
        } catch (Exception e){
            logger.error(
                    String.format("channelRead0: Command %d, action %d", message.getCommand(), message.getBeanType()),
                    e
            );
        }

    }

    private void processMessage(ChannelContextInfo ctx, XMessage message) {
        if (ctx != null)
        {
            //Save lại time tương tác của channle
            ctx.setLastTimeInteractive((int) ZonedDateTime.now().toEpochSecond());
            int commandID = message.getCommand();
            ICommand command = null;
            switch (commandID) {
                case ServerCommandConstant.USER_COMMAND:
                    command = userCommand;
                    break;
                case ServerCommandConstant.GAME_COMMAND:
                    command = gameCommand;
                    break;
                case ServerCommandConstant.MINI_GAME_COMMAND:
                    command = miniGameCommand;
                    break;
                case ServerCommandConstant.RANK_COMMAND:
                    command = rankCommand;
                    break;
                case ServerCommandConstant.INBOX_COMMAND:
                    command = inboxCommand;
                    break;
                case ServerCommandConstant.PAYMENT_COMMAND:
                    command = paymentCommand;
                    break;
                case ServerCommandConstant.COMMON_COMMAND:
                    command = commonCommand;
                    break;
                case ServerCommandConstant.FRIENDS_COMMAND:
                    command = friendsCommand;
                    break;
                case ServerCommandConstant.TAI_XIU_COMMAND:
                    command = txCommand;
                    break;
                case ServerCommandConstant.SLOT_OCEAN_COMMAND:
                    command = slotOceanCommand;
                    break;
                case ServerCommandConstant.SLOT_THREE_KINGDOMS_COMMAND:
                    command = slotThreeKingdomsCommand;
                    break;

                case ServerCommandConstant.THIRTEENKILLER_COMMAND:
                    command = thirteenKillerCommand;
                    break;
                case ServerCommandConstant.POKER_COMMAND:
                    command = pokerCommand;
                    break;

                case ServerCommandConstant.THREE_CARD_COMMAND:
                    command = threeCardCommand;
                    break;
            }

            if (command != null)
            {
                try
                {
                    command.process(ctx, message);
                }
                catch (Exception e)
                {
                    command.getLogger().error(
                            String.format("processMessage - UserId: %d, command: %d, action: %d", ctx.getUserId(), commandID, message.getBeanType()),
                            e
                    );
                }
            }
        }
    }

    @Override
    public void channelReadComplete(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        // logger.info("channelReadComplete {}", ctx.channel().remoteAddress().toString());

        ctx.flush();
    }

    @Override
    public void channelInactive(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        // logger.info("channelInactive {}", ctx.channel().remoteAddress().toString());
        ChannelContextInfo ctxInfo = channelService.getChannel(ctx);
        if (ctxInfo != null) {
            GameManager.getInstance().disconnectCtx(ctxInfo);
            //GameCommand.getInstance().leaveTable(ctxInfo);
            if (ctxInfo.isAuth()) {
                int userId = ctxInfo.getUserId();

                //
                userCommand.userOffline(userId);
            }
        }

        channelService.removeChannel(ctx);
    }
}
