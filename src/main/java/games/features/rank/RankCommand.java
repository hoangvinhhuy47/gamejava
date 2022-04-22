package games.features.rank;

import com.google.protobuf.InvalidProtocolBufferException;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.features.constant.RequestBeanFeatureConst;
import games.features.constant.ResponseBeanFeatureConst;
import message.XProtos;

/**
 * Created by tuanhoang on 8/21/17.
 */
public class RankCommand extends ICommand implements RequestBeanFeatureConst, ResponseBeanFeatureConst {
    private static RankCommand instance;
    public static RankCommand getInstance() {
        if (instance == null) instance = new RankCommand();
        return instance;
    }

    private RankService rankService;

    RankCommand() {
        rankService = RankService.getInstance();
    }

    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        switch (message.getBeanType()) {
            case REQUEST_RANKING_TOP_GOLD:
                rankService.getTopGold(ctx);
                break;
            case REQUEST_RANKING_TOP_WIN_GOLD:
                rankService.getTopWinGold(ctx);
                break;
        }
    }
}
