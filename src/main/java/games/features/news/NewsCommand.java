package games.features.news;

import com.google.protobuf.InvalidProtocolBufferException;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.features.constant.RequestBeanFeatureConst;
import games.features.constant.ResponseBeanFeatureConst;
import message.XProtos;

/**
 * Created by tuanhoang on 9/13/17.
 */
public class NewsCommand extends ICommand implements ResponseBeanFeatureConst, RequestBeanFeatureConst {
    private static NewsCommand instance;
    public static NewsCommand getInstance() {
        if (instance == null) instance = new NewsCommand();
        return instance;
    }

    private NewsService newsService;
    NewsCommand() {
        newsService = NewsService.getInstance();
    }

    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        if (!ctx.checkAuth()) {
            return;
        }
        int userId = ctx.getUserId();
        switch (message.getBeanType()) {
            case LIST_NEWS_REQUEST_BEAN:
                break;
            case READ_NEWS_REQUEST_BEAN:
                break;
        }
    }
}
