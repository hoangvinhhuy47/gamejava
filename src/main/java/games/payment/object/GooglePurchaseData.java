package games.payment.object;

import com.fasterxml.jackson.core.type.TypeReference;
import libs.util.Utility;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by tuanhoang on 8/24/17.
 */
public class GooglePurchaseData {
    String mItemType;  // ITEM_TYPE_INAPP or ITEM_TYPE_SUBS
    String mOrderId;// transactionId
    String mPackageName;
    String mSku;// productId
    long mPurchaseTime;
    int mPurchaseState;
    String mDeveloperPayload;
    String mToken;
    String mOriginalJson;
    String mSignature;

    public GooglePurchaseData(String itemType, String jsonPurchaseInfo, String signature) throws IOException {
        mItemType = itemType;
        mOriginalJson = jsonPurchaseInfo;

        HashMap<String, Object> data = Utility.getObjectMapper().readValue(jsonPurchaseInfo, new TypeReference<HashMap<String, Object>>(){});


        mOrderId = (String)data.get("orderId");
        mPackageName = (String)data.get("packageName");
        mSku = (String)data.get("productId");
        mPurchaseTime = (Long)data.get("purchaseTime");
        mPurchaseState = (Integer)data.get("purchaseState");
        mDeveloperPayload = (String)data.get("developerPayload");
        // FIXME
        mToken = (String)data.get("purchaseToken");
        mSignature = signature;
    }

    public String getItemType() { return mItemType; }
    public String getOrderId() { return mOrderId; }
    public String getPackageName() { return mPackageName; }
    public String getSku() { return mSku; }
    public long getPurchaseTime() { return mPurchaseTime; }
    public int getPurchaseState() { return mPurchaseState; }
    public String getDeveloperPayload() { return mDeveloperPayload; }
    public String getToken() { return mToken; }
    public String getOriginalJson() { return mOriginalJson; }
    public String getSignature() { return mSignature; }

    @Override
    public String toString() { return "PurchaseInfo(type:" + mItemType + "):" + mOriginalJson; }
}
