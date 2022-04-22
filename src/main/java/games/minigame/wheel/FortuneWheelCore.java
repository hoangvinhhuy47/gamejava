package games.minigame.wheel;

import games.core.common.data.DataCommonManager;
import libs.util.CRandom;
import message.MiniGameProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanhoang on 8/17/17.
 * Logic quay của một vòng quay. Chứa list item trong vòng quay đó
 */
public class FortuneWheelCore {
    private int wheelId;
    private List<FortuneWheelItem> itemList;

    public void reload(int wheelId) {
        this.wheelId = wheelId;
        this.itemList = DataCommonManager.getInstance().getWheelItemArrayList().get(wheelId);
        System.out.print("List wheel - id = " + wheelId + " - size = " + itemList.size());
    }

    public FortuneWheelItem randomeItem() {
        int rand = new CRandom().randInt(0, 7);
        return getByIndex(rand);
    }

    public FortuneWheelItem getByIndex(int index) {
        for (FortuneWheelItem item : itemList) {
            if (index == item.getIndex()) {
                return item;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        new FortuneWheelCore().reload(2);
    }

    public FortuneWheelItem spin() {
        if (itemList != null && itemList.size() > 0) {
            FortuneWheelItem result;
            int randValue = 0;
            for (FortuneWheelItem item : itemList) {
                if (item.getQuantity() > 0) {
                    randValue += item.getRate();
                }
            }
            long rand = new CRandom().randInt(0, randValue);
            for (FortuneWheelItem item : itemList) {
                if (item.getQuantity() > 0) {
                    if (rand < item.getRate()) {
                        result = item;
                        if (item.getQuantity() != 9999) {
                            item.setQuantity(item.getQuantity() - 1);
                        }
                        return result;
                    } else {
                        rand -= item.getRate();
                    }
                }
            }
        }
        return null;
    }

    List<MiniGameProtos.WheelItemInfoBean> toListWheelItemBuilder() {
        List<MiniGameProtos.WheelItemInfoBean> builderList = new ArrayList<>();
        for (FortuneWheelItem item : itemList) {
            MiniGameProtos.WheelItemInfoBean.Builder itemBuilder = MiniGameProtos.WheelItemInfoBean.newBuilder();
            itemBuilder.setItemId(item.getItemId());
            itemBuilder.setName(item.getName());
            itemBuilder.setItemReward(item.getItem_reward());
            itemBuilder.setCurrency(item.getCurrency());
            itemBuilder.setItemProductId(item.getItemProductId());
            itemBuilder.setWheelId(wheelId);
            itemBuilder.setIndex(item.getIndex());
            builderList.add(itemBuilder.build());
            //itemBuilder.setItemId(item.getItemId());
        }
        return builderList;
    }
}
