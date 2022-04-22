package games.payment.object;

import games.core.proto.ProtoSerializer;
import games.payment.constant.PaymentActionType;
import message.XProtos;

public class PaymentItem implements ProtoSerializer<XProtos.PaymentItemResponseBean> {
    private int id = 0;
    private PaymentActionType actionType;
    private int value = 0;
    private int realValue = 0;

    public PaymentItem() {
        id = 0;
        actionType = PaymentActionType.NONE;
        value = 0;
        realValue = 0;
    }

    public PaymentItem(String[] configs) {
        if (configs.length >= 4)
        {
            this.id = Integer.parseInt(configs[0]);
            this.actionType = PaymentActionType.getById(Integer.parseInt(configs[1]));
            this.value = Integer.parseInt(configs[2]);
            this.realValue = Integer.parseInt(configs[3]);
        }

    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the realValue
     */
    public int getRealValue() {
        return realValue;
    }

    /**
     * @param realValue the realValue to set
     */
    public void setRealValue(int realValue) {
        this.realValue = realValue;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * @return the actionType
     */
    public PaymentActionType getActionType() {
        return actionType;
    }

    /**
     * @param actionType the actionType to set
     */
    public void setActionType(PaymentActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public XProtos.PaymentItemResponseBean.Builder parseProtoBuilder() {
        XProtos.PaymentItemResponseBean.Builder builder = XProtos.PaymentItemResponseBean.newBuilder();
        builder.setId(this.getId());
        builder.setRealValue(this.getRealValue());
        builder.setValue(this.getValue());
        builder.setType(this.getActionType().getValue());
        return builder;
    }

    @Override
    public String toString() {
        String pattern =
                "PaymentItem: %s, " +
                "ActionType: %s, " +
                "Value: %s, " +
                "RealValue: %s";
        return String.format(
                pattern,
                getId(),
                getActionType(),
                getValue(),
                getRealValue()
        );
    }
}