package games.minigame.wheel;

/**
 * Created by tuanhoang on 8/17/17.
 * Info của một loại item trong vòng quay
 *
 */
public class FortuneWheelItem {
    private int		itemId;
    private  int index;
    private int     wheelId;
    private String	name;
    private String	itemProductId;
    private long    item_reward;
    private int     currency;
    private long     rate;
    private int     quantity;

    public FortuneWheelItem() {

    }

    public FortuneWheelItem(String[] configs) {
        if (configs.length >= 9) {
            this.itemId = Integer.parseInt(configs[0]);
            this.index = Integer.parseInt(configs[1]);
            this.wheelId = Integer.parseInt(configs[2]);
            this.name = configs[3];
            this.itemProductId = configs[4];
            this.item_reward = Long.parseLong(configs[5]);
            this.currency = Integer.parseInt(configs[6]);
            this.rate = Long.parseLong(configs[7]);
            this.quantity = Integer.parseInt(configs[8]);
        }

    }

    //region gette - setter
    public void setIndex(int index)
    {
        this.index = index;
    }
    public int getIndex()
    {
        return this.index;
    }
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getWheelId() {
        return wheelId;
    }

    public void setWheelId(int wheelId) {
        this.wheelId = wheelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getItemProductId() {
        return itemProductId;
    }

    public void setItemProductId(String itemProductId) {
        this.itemProductId = itemProductId;
    }

    public long getItem_reward() {
        return item_reward;
    }

    public void setItem_reward(long item_reward) {
        this.item_reward = item_reward;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public long getRate() {
        return rate;
    }

    public void setRate(long rate) {
        this.rate = rate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    //endregion
}
