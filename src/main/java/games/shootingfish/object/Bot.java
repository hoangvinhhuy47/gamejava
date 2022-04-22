package games.shootingfish.object;

import games.core.common.data.DataCommonManager;
import games.core.database.document.UserInfo;
import games.core.database.document.UserMiniGame;
import games.core.database.document.UserMoney;
import games.core.user.User;
import games.core.user.UserService;
import games.core.user.object.InfoUserItem;

/**
 * Created by tuanhoang on 9/28/17.
 */
public class Bot extends  FishingPlayer {
    public Bot(int id, int money) {
        //super(id);
        randomeInfo(id, money);
        this.defaultInit();
        isBot = true;
    }

    public void randomeInfo(int botId, int money) {
        UserInfo userInfo = new UserInfo();
        userInfo.setAvatar("");
        InfoUserItem infoUserItem = DataCommonManager.getInstance().getRandomeInfoUser();
        userInfo.setGender(infoUserItem.getGender());
        userInfo.setNickName(infoUserItem.getName());
        userInfo.setUserName("Player_" + botId);
        userInfo.setId(botId);

        UserMiniGame userMiniGame = new UserMiniGame();
        userMiniGame.setUserId(botId);

        UserMoney userMoney = new UserMoney();
        userMoney.setId(botId);
        userMoney.setMoneyGold(money);

        this.user = new User(userInfo, userMoney,  userMiniGame, 0, true);

        UserService.getInstance().addUser(this.user);
    }
}
