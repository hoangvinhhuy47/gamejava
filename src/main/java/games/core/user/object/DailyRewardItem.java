package games.core.user.object;

import message.IResponseBean;
import message.UserProtos;

/**
 * Created by tuanhoang on 8/24/17.
 */
public class DailyRewardItem implements IResponseBean<UserProtos.InfoDailyRewardResponseBean> {
    private int day = 0;
    private int moneyReward = 0;

    DailyRewardItem() {

    }

    public DailyRewardItem(String[] strs) {
        try {
            if (strs.length >= 2) {
                day = Integer.parseInt(strs[0]);
                moneyReward = Integer.parseInt(strs[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMoneyReward() {
        return moneyReward;
    }

    public void setMoneyReward(int moneyReward) {
        this.moneyReward = moneyReward;
    }

    @Override
    public UserProtos.InfoDailyRewardResponseBean parseResponseBeanBuilder() {
        UserProtos.InfoDailyRewardResponseBean.Builder responseBean = UserProtos.InfoDailyRewardResponseBean.newBuilder();
        responseBean.setDay(this.day);
        responseBean.setGoldReward(this.moneyReward);
        return responseBean.build();
    }
}
