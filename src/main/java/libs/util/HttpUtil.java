package libs.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class HttpUtil {
    private static Logger log = Logger.getLogger(HttpUtil.class.getSimpleName());


    public static SmileeHttpResponse callPOSTWithJSON(String url, Object objectToJSON, boolean useSSL) throws Exception {
        try {
            StringEntity entity = new StringEntity(JsonUtil.generateJson(objectToJSON));
            entity.setContentType("application/json");
            return callPOSTWithEntity(url, entity, useSSL, "");
        } catch (Exception e) {
            //log.error("callPOSTWithJSON ", e);
            throw e;
        }
    }

    public static SmileeHttpResponse callPOSTWithParameters(String url, Map<String, String> parameters, boolean useSSL) throws Exception {
        try {
            List<NameValuePair> requestParameters = new ArrayList<>();
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                requestParameters.add(new BasicNameValuePair(parameter.getKey(), parameter.getValue()));
            }
            return callPOSTWithEntity(url, new UrlEncodedFormEntity(requestParameters, "UTF-8"), useSSL, "");
        } catch (Exception e) {
            //log.error("callPOSTWithParameters ", e);
            throw e;
        }
    }

    public static SmileeHttpResponse callPOSTWithParametersWithAuthen(String url, Map<String, String> parameters, boolean useSSL, String token) throws Exception {
        try {
            List<NameValuePair> requestParameters = new ArrayList<>();
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                requestParameters.add(new BasicNameValuePair(parameter.getKey(), parameter.getValue()));
            }
            return callPOSTWithEntity(url, new UrlEncodedFormEntity(requestParameters, "UTF-8"), useSSL, token);
        } catch (Exception e) {
            //log.error("callPOSTWithParameters ", e);
            throw e;
        }
    }

    public static SmileeHttpResponse callGET(String url, String token) throws Exception {
        try {
            CloseableHttpClient httpclient = null;
            httpclient = buildClient(token);
            HttpGet request = new HttpGet(url);

            HttpResponse response = httpclient.execute(request);

            SmileeHttpResponse result = new SmileeHttpResponse();
            result.setStatus(response.getStatusLine().getStatusCode());
            if (result.getStatus() == ServerConstant.HTTP_STATUS.SUCCESS) {
                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();
                String responseText = IOUtils.toString(inputStream);
//				EntityUtils.getContentCharSet(entity);
                // String responseText = EntityUtils.toString(entity, "UTF-8");
                result.setData(responseText);
            }
            return result;
        } catch (Exception e) {
            //log.error("callPOSTWithEntity ", e);
            throw e;
        }
    }

    public static SmileeHttpResponse callPOSTWithEntity(String url, HttpEntity entity, boolean useSSL, String token) throws Exception {
        try {
            CloseableHttpClient httpclient = null;
            if (useSSL/* url.toLowerCase().contains("https") */)
                httpclient = buildSSLClient();
            else
                httpclient = buildClient(token);

            HttpPost httpost = new HttpPost(url);

            httpost.setEntity(entity);


            HttpResponse response = httpclient.execute(httpost);

            SmileeHttpResponse result = new SmileeHttpResponse();
            result.setStatus(response.getStatusLine().getStatusCode());
            if (result.getStatus() == ServerConstant.HTTP_STATUS.SUCCESS) {
                entity = response.getEntity();
                InputStream inputStream = entity.getContent();
                String responseText = IOUtils.toString(inputStream);
//				EntityUtils.getContentCharSet(entity);
                // String responseText = EntityUtils.toString(entity, "UTF-8");
                result.setData(responseText);
            }
            return result;
        } catch (Exception e) {
            //log.error("callPOSTWithEntity ", e);
            throw e;
        }
    }

    public static CloseableHttpClient buildSSLClient() throws KeyManagementException, NoSuchAlgorithmException {
        HttpClientBuilder builder = HttpClientBuilder.create();

        SSLContext sslcontext = SSLContexts.custom().build();
        sslcontext.init(null, new X509TrustManager[]{new HttpsTrustManager()}, new SecureRandom());
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());
        builder.setSSLSocketFactory(sslConnectionFactory);

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslConnectionFactory).build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        RequestConfig defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        builder.setDefaultRequestConfig(defaultRequestConfig).build();

        return builder.build();
    }

    public static CloseableHttpClient buildClient(String token) {
        HttpClientBuilder builder = HttpClientBuilder.create();

        RequestConfig defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        builder.setDefaultRequestConfig(defaultRequestConfig).build();

        if (!token.equals("")) {
            final List<BasicHeader> headers = new ArrayList<>();
            headers.add(new BasicHeader("Authorization", "Bearer " + token));
            builder.setDefaultHeaders(headers);
        }
        return builder.build();
    }

    public static class HttpsTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
