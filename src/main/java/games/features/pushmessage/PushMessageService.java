package games.features.pushmessage;

import games.core.constant.ServerCommandConstant;
import games.core.user.User;
import games.core.user.UserService;
import games.features.constant.ResponseBeanFeatureConst;
import games.features.database.document.TextRun;
import games.features.database.model.MTextRun;
import libs.util.LogFactory;
import message.FeatureProtos;
import message.XProtos;
import org.apache.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by tuanhoang on 8/22/17.
 */
public class PushMessageService implements ResponseBeanFeatureConst{
    private static PushMessageService instance;
    public static PushMessageService getInstance() {
        if (instance == null) instance = new PushMessageService();
        return instance;
    }

    BlockingQueue<TextRun> listTextRun = new ArrayBlockingQueue<TextRun>(5);
    List<TextRun> listTextRunInGame = new ArrayList<>();
    private int LIMIT_TEXT_RUN = 5;
    private MTextRun mTextRun;
    private Logger logger;

    private XProtos.XMessage responseTextRunCur;

    private PushMessageService() {
        mTextRun = MTextRun.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        listTextRun = mTextRun.getTextRunActives(LIMIT_TEXT_RUN);
        responseTextRunCur = buildTextRunCurrent(listTextRun, true);
    }

    public void sendTextRunCurrentForUserLogin(User user) {
        responseTextRunCur = buildTextRunCurrent(listTextRun, true);
        user.getCtx().writeAndFlush(responseTextRunCur);
    }

    public void pushTextRunFromGame(int userId, String nickname, String content, String gamename) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("Congratulations ").append(nickname);
            builder.append("  has just received ").append(content);
            builder.append(" from ").append(gamename);

            TextRun textRun = new TextRun();
            textRun.setContent(builder.toString());
            textRun.setLink("");
            int timeNow = (int)ZonedDateTime.now().toEpochSecond();
            textRun.setTimeStart(timeNow);
            textRun.setTimeEnd(timeNow + 3600);
            textRun.setLoopInterval(30);
            textRun.setActive(true);
            textRun.setAuto(true);
            textRun.setPriority(3);

            logger.info("Push text run - " + userId + " - " + builder.toString());
            listTextRun.offer(textRun);
            responseTextRunCur = buildTextRunCurrent(listTextRun, true);

            addTextRunFromGame(textRun);
        } catch (Exception e) {
            logger.error("pushTextRunFromGame error", e);
        }

    }

    //TextRun chính. Các sự kiện, các thông báo từ hệ thống. Nên cần lưu xuống db
    public void addTextRun(TextRun textRun) {
        try {
            mTextRun.addTextRun(textRun);
            BlockingQueue<TextRun> listTextRun = new ArrayBlockingQueue<TextRun>(1);
            listTextRun.add(textRun);
            forceTextRunForAllUser(listTextRun);
        } catch (Exception e) {
            logger.error("Add textRun error", e);
        }
    }

    //force textRun for client. Hàm này sẽ được gọi từ tools. Sau khi admin chỉnh sửa xong các text run thì gọi hàm này để update cho client
    public void forceTextRunCurrentForAllUser() {
        Collection<User> listActiveUser = UserService.getInstance().getListActiveUsers();
        BlockingQueue<TextRun> listTextRun = mTextRun.getTextRunActives(LIMIT_TEXT_RUN);
        responseTextRunCur = buildTextRunCurrent(listTextRun, true);
        for (User user : listActiveUser) {
            if (user.getCtx() != null) user.getCtx().writeAndFlush(responseTextRunCur);
        }
    }

    //region add các text run từ game

    //TextRun from game thì không cần phải lưu xuống db. Cứ thế mà gửi xuống client thôi.
    public void addTextRunFromGame(TextRun textRun) {
        BlockingQueue<TextRun> listTextRun = new ArrayBlockingQueue<TextRun>(1);
        listTextRun.add(textRun);
        forceTextRunForAllUser(listTextRun);
    }

    private void forceTextRunForAllUser(BlockingQueue<TextRun> listTextRun) {
        Collection<User> listActiveUser = UserService.getInstance().getListActiveUsers();
        forceTextRunForListUser(listTextRun, listActiveUser);
    }

    private void forceTextRunForListUser(BlockingQueue<TextRun> listTextRun, Collection<User> listUser) {
        XProtos.XMessage response = buildTextRunCurrent(listTextRun, true);
        for (User user : listUser) {
            if (user.getCtx() != null) user.getCtx().writeAndFlush(response);
        }
    }

    //endregion

    private XProtos.XMessage buildTextRunCurrent(BlockingQueue<TextRun> textRunList, boolean forceShow) {
        FeatureProtos.PushTextRunBean.Builder builder = FeatureProtos.PushTextRunBean.newBuilder();
        int timeCurrent = (int)ZonedDateTime.now().toEpochSecond() + 30;

        while (textRunList.size() > LIMIT_TEXT_RUN) {
            textRunList.poll();
        }
        Iterator<TextRun> iterator = textRunList.iterator();
        while (iterator.hasNext()) {
            TextRun textRun = iterator.next();
            if (textRun.isActive() && textRun.getTimeEnd() > timeCurrent) {
                builder.addPushMessage(textRun.parseResponseBeanBuilder());
            } else {
                iterator.remove();
                //textRunList.remove(textRun);
            }
        }
        builder.setForceShow(forceShow);

        XProtos.XMessage.Builder responseBuilder = XProtos.XMessage.newBuilder();
        responseBuilder.setCommand(ServerCommandConstant.PUSH_MESSAGE_COMMAND);
        responseBuilder.setBeanType(PUSH_MESSAGE_TEXT_RUN_RESPONSE_BEAN);
        responseBuilder.setData(builder.build().toByteString());

        return responseBuilder.build();
    }

    //Test blocking queue
    public static void main(String[] args) {
        BlockingQueue<Integer> list = new ArrayBlockingQueue<Integer>(5);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);

        Integer first = list.remove();
        System.out.println("Remove - " + first);

        while (!list.isEmpty()) {
            System.out.println("Poll: " + list.poll());
            System.out.println("Size: " + list.size());
        }

        for (int i = 1; i <= 5; i++) {
            list.offer(i);
        }

        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            Integer value = iterator.next();
            System.out.println("iterator - " + value);
            if (value == 4) {
                iterator.remove();
            }
        }
        System.out.println("Size: " + list.size());
        while (!list.isEmpty()) {
            System.out.println("Poll: " + list.poll());
            System.out.println("Size: " + list.size());
        }


    }
}
