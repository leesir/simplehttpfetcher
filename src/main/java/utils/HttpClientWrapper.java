package utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HttpClient的包装类，版本为4.3.4。
 * @author derrick.li
 * @since 2015/9/15
 */
public final class HttpClientWrapper {

    /**
     * 工具类，私有化构造函数，不允许实例化。
     */
    private HttpClientWrapper() {
    }

    /**
     * 默认处理字符集。
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * GBK字符集。
     */
    public static final String GBK_CHARSET = "GBK";

    /**
     * 默认获取请求对象超时时间connectionRequestTimeout，单位是毫秒。
     */
    public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 5 * 1000;

    /**
     * 默认请求超时时间connectTimeout，单位是毫秒。
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;

    /**
     * 默认数据超时时间socketTimeout，单位是毫秒。
     */
    public static final int DEFAULT_SOCKET_TIMEOUT = 30 * 1000;

    /**
     *  通过制定参数，访问制定url，并返回json格式的结果。连接建立和请求过程中的超时参数将会被设置成默认值。返回结果的字符集为utf-8。
     * @param url 访问的地址。
     * @param params 访问地址时的参数。
     * @param requestCharset 请求数据编码（HttpClientWrapper.GBK_CHARSET  or HttpClientWrapper.DEFAULT_CHARSET）
     * @return json格式的返回结果。
     * @throws IOException 请求过程中的IO异常。
     * @see HttpClientWrapper#DEFAULT_CONNECTION_REQUEST_TIMEOUT
     * @see HttpClientWrapper#DEFAULT_CONNECT_TIMEOUT
     * @see HttpClientWrapper#DEFAULT_SOCKET_TIMEOUT
     * @throws Exception 抛出异常
     */
    public static String sendPostRequest(String url, List<BasicNameValuePair> params, String ... requestCharset)
            throws Exception {
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpPost httpPost = null;
        String jsonResult = null;
        try {
            httpPost = getHttpPost(url, params, new TimeoutConfig());
            //设置请求数据编码，如果有传，则使用传递的，反之使用默认UTF-8
            String charset = DEFAULT_CHARSET;
            if (null != requestCharset && requestCharset.length > 0) {
                charset = requestCharset[0];
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params, charset));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            jsonResult = EntityUtils.toString(entity, DEFAULT_CHARSET);
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return jsonResult;
    }

    /**
     *  通过制定参数，访问制定url，并返回json格式的结果。连接建立和请求过程中的超时参数将由调用端制定。返回结果的字符集为utf-8。
     * @param url 访问的地址。
     * @param params 访问地址时的参数。
     * @param timeoutConfig 请求超时配置。
     * @return json格式的返回结果。
     * @throws IOException 请求过程中的IO异常。
     */
    public static String sendPostRequest(String url, List<BasicNameValuePair> params, TimeoutConfig timeoutConfig)
            throws IOException {
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpPost httpPost = null;
        String jsonResult = null;
        try {
            if (timeoutConfig != null) {
                httpPost = getHttpPost(url, params, new TimeoutConfig(timeoutConfig.getConnectionRequestTimeout(),
                        timeoutConfig.getConnectTimeout(), timeoutConfig.getSocketTimeout()));
            } else {
                httpPost = getHttpPost(url, params, new TimeoutConfig());
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            jsonResult = EntityUtils.toString(entity, DEFAULT_CHARSET);
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return jsonResult;
    }

    /**
     * 根据参数获取一个HttpPost对象，此方法只负责生产HttpPost对象，资源释放的工作将由调用端负责。
     *
     * @param url 访问的地址。
     * @param params 访问地址时的参数。
     * @param timeoutConfig 请求超时配置。
     * @return 根据参数生成的HttpPost对象。
     * @throws IOException 请求过程中的IO异常。
     */
    public static HttpPost getHttpPost(String url, List<BasicNameValuePair> params, TimeoutConfig timeoutConfig)
            throws IOException {
        HttpPost httpPost = new HttpPost(url);
        //配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeoutConfig.getConnectionRequestTimeout())
                .setConnectTimeout(timeoutConfig.getConnectTimeout())
                .setSocketTimeout(timeoutConfig.getSocketTimeout()).build();
        httpPost.setConfig(requestConfig);
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        return httpPost;
    }

    /**
     * 超时配置，所有超时参数的单位都是毫秒。
     */
    public static class TimeoutConfig {
        private int connectionRequestTimeout = -1;
        private int connectTimeout = -1;
        private int socketTimeout = -1;

        public TimeoutConfig() {
            this.connectionRequestTimeout = HttpClientWrapper.DEFAULT_CONNECTION_REQUEST_TIMEOUT;
            this.connectTimeout = HttpClientWrapper.DEFAULT_CONNECT_TIMEOUT;
            this.socketTimeout = HttpClientWrapper.DEFAULT_SOCKET_TIMEOUT;
        }

        public TimeoutConfig(int connectionRequestTimeout, int connectTimeout, int socketTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            this.connectTimeout = connectTimeout;
            this.socketTimeout = socketTimeout;
        }

        public int getConnectionRequestTimeout() {
            return connectionRequestTimeout;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public int getSocketTimeout() {
            return socketTimeout;
        }

        @Override
        public String toString() {
            return "TimeoutConfig{" +
                    "connectionRequestTimeout=" + connectionRequestTimeout +
                    ", connectTimeout=" + connectTimeout +
                    ", socketTimeout=" + socketTimeout +
                    '}';
        }
    }
}
