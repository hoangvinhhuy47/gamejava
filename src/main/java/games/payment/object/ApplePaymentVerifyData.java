package games.payment.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by tuanhoang on 8/24/17.
 */
@JsonIgnoreProperties
public class ApplePaymentVerifyData {
    public String						environment;
    public ApplePaymentVerifyReceipt	receipt;
    public int							status;

    public ApplePaymentVerifyData() {
    }
}
