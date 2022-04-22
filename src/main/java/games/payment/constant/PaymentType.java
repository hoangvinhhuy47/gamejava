package games.payment.constant;

/**
 * Created by tuanhoang on 10/4/17.
 */
public enum PaymentType {
    IAP_APPLE(1, 2),
    IAP_GOOGLE(2, 2),
    VIDEO_ADS(3, 0),
    CARD (4, 1);

    int value;
    int channelId;
    PaymentType(int value, int channelId) {
        this.value = value;
        this.channelId = channelId;
    }

    public int getValue() {
        return this.value;
    }

    public int getChannelId() {
        return channelId;
    }
}
