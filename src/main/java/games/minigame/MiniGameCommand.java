package games.minigame;

import com.google.protobuf.InvalidProtocolBufferException;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.minigame.constant.RequestBeanMiniGameConst;
import games.minigame.giftcode.GiftCodeService;
import games.minigame.wheel.FortuneWheelService;
import libs.util.LogFactory;
import message.MiniGameProtos;
import message.XProtos;
import org.apache.log4j.Logger;

/**
 * Created by tuanhoang on 8/17/17.
 */
public class MiniGameCommand extends ICommand implements RequestBeanMiniGameConst {
    private static MiniGameCommand instance;
    private GiftCodeService giftCodeService;
    private FortuneWheelService fortuneWheelService;
    private static Logger logger;
    public static MiniGameCommand getInstance() {
        if (instance == null) instance = new MiniGameCommand();
        return instance;
    }

    private MiniGameCommand(){
        fortuneWheelService = FortuneWheelService.getInstance();
        giftCodeService = GiftCodeService.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }
    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        if (ctx.checkAuth()) {
            switch (message.getBeanType())
            {
                case FORTURN_WHEEL_INFO_REQUEST:
                    fortuneWheelService.fortuneWheelRequestInfo(ctx);
                    break;
                case FORTURN_WHEEL_SPIN_REQUEST:
                    fortuneWheelService.fortuneWheelRequestSpin(ctx);
                    break;
                case FORTURN_WHEEL_GET_HISTORY_REQUEST:
                    fortuneWheelService.fortuneWheelRequestHistory(ctx);
                    break;
                case FORTURN_WHEEL_VERIFY_CARD_REQUEST:
                    MiniGameProtos.CardTypeRequest cardTypeRequest = MiniGameProtos.CardTypeRequest.parseFrom(message.getData());
                    fortuneWheelService.verifyCard(ctx, cardTypeRequest.getCardType());
                    break;
                case FORTURN_WHEEL_TEST_ACCEPT:
                    //fortuneWheelService.testAccept(MCashoutReciept.getInstance().getReceiptIdCur(), "TuanHoang");
                    break;
                case GIFT_CODE_USE_REQUEST:
                    MiniGameProtos.GiftCodeRequestUse requestUse = MiniGameProtos.GiftCodeRequestUse.parseFrom(message.getData());
                    giftCodeService.useGiftCode(ctx, requestUse.getCode());
                    break;
                case PROMOTE_CODE_REQUEST:
                    MiniGameProtos.PromoteCodeRequestBean codeRequestBean = MiniGameProtos.PromoteCodeRequestBean.parseFrom(message.getData());
                    giftCodeService.usePromoteCode(ctx, codeRequestBean.getPromoteCode());
                    break;

            }
        }
    }
}
