package games.payment.object;

import java.util.List;

/**
 * Created by tuanhoang on 8/24/17.
 */
public class ApplePaymentVerifyReceipt {
    public String							receipt_type;
    public int								adam_id;
    public int								app_item_id;
    public String							bundle_id;
    public String							application_version;
    public long								download_id;
    public int								version_external_identifier;
    public String							request_date;
    public long								request_date_ms;
    public String							request_date_pst;
    public String							original_purchase_date;
    public long								original_purchase_date_ms;
    public String							original_purchase_date_pst;
    public String							original_application_version;
    public List<ApplePaymentVerifyInApp>    in_app;

    public ApplePaymentVerifyReceipt() {
    }
}
