package games.payment.object;

/**
 * Created by tuanhoang on 8/24/17.
 */
public class ApplePaymentVerifyInApp {
    public boolean	is_trial_period;
    public String	original_purchase_date;
    public long		original_purchase_date_ms;
    public String	original_purchase_date_pst;
    public String	original_transaction_id;
    public String	product_id;
    public String	purchase_date;
    public long		purchase_date_ms;
    public String	purchase_date_pst;
    public int		quantity;
    public String	transaction_id;

    public ApplePaymentVerifyInApp() {
    }
}
