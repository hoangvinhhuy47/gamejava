package libs.util;

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by tuanhoang on 4/21/17.
 */
public class GooglePaymentSecurity {
    private static Logger log = LogFactory.getLogger(GooglePaymentSecurity.class.getSimpleName());
    private static final String	TAG	= "Util/Security";

    private static final String	KEY_FACTORY_ALGORITHM	= "RSA";
    private static final String	SIGNATURE_ALGORITHM		= "SHA1withRSA";

    /**
     * Verifies that the data was signed with the given signature, and returns the verified purchase. The data is in JSON format and signed with a private key. The data also contains the {@link //PurchaseState} and product ID of the purchase.
     *
     * @param base64PublicKey the base64-encoded public key to use for verifying.
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     */
    public static boolean verifyPurchase(String base64PublicKey, String signedData, String signature) {
        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey) ||
                TextUtils.isEmpty(signature)) {
            log.error(TAG + "Purchase verification failed: missing data.");
            return false;
        }

        PublicKey key = GooglePaymentSecurity.generatePublicKey(base64PublicKey);
        return GooglePaymentSecurity.verify(key, signedData, signature);
    }

    /**
     * Generates a PublicKey instance from a string containing the MyBase64-encoded public key.
     *
     * @param encodedPublicKey MyBase64-encoded public key
     * @throws IllegalArgumentException if encodedPublicKey is invalid
     */
    public static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            byte[] decodedKey = MyBase64.decode(encodedPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            log.error(TAG + "Invalid key specification.");
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            log.error(TAG, e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Verifies that the signature from the server matches the computed signature on the data. Returns true if the data is correctly signed.
     *
     * @param publicKey public key associated with the developer account
     * @param signedData signed data from server
     * @param signature server signature
     * @return true if the data and signature match
     */
    private static boolean verify(PublicKey publicKey, String signedData, String signature) {
        Signature sig;
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (!sig.verify(MyBase64.decode(signature))) {
                //log.error(TAG, "Signature verification failed.");
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            log.error(TAG + "NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            log.error(TAG + "Invalid key specification.");
        } catch (SignatureException e) {
            log.error(TAG + "Signature exception.");
        } catch (Exception e) {
            log.error(TAG, e);
        }
        return false;
    }
}