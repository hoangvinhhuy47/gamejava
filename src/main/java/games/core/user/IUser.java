package games.core.user;

import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.Money;
import games.core.common.money.MoneyBag;
import games.core.common.money.MoneyType;
import games.core.common.money.TransactionStatus;

public interface IUser
{
    int getId();
    long getMoneyGold();
    long getMoneySilver();
    long getMoneyByType(MoneyType moneyType);
    void resetMoneyTrial();
    long getMoneyTrial();
    MoneyBag getMoneyBag();
    String getUserName();
    String getUserPass();
    String getNickName();
    String getAvatar();
    ChannelContextInfo getCtx();
    void addMoneyWin(Money moneyWin);
    TransactionStatus addMoney(Money moneyAdd);
    TransactionStatus subMoney(Money moneySub, boolean subAnyway);
    void saveUserInfo() throws NoSuchFieldException, IllegalAccessException;
    void saveUserMoney() throws NoSuchFieldException, IllegalAccessException;
    void saveUserMiniGame() throws NoSuchFieldException, IllegalAccessException;
    void plusNumOfTrial(int numPlus);
    void subNumOfTrial(int numSub);
    void SetMoney(long newMoney);
    long GetMoney();
}
