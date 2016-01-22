package model;

import org.apache.http.message.BasicNameValuePair;

import java.util.List;

/**
 * http请求的简单封装。
 * @author lijing
 * @since 2015/9/25
 */
public class HttpRequestModel {
    private String url;
    private List<BasicNameValuePair> params;

    public HttpRequestModel() {
    }

    public HttpRequestModel(String url, List<BasicNameValuePair> params) {
        this.url = url;
        this.params = params;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<BasicNameValuePair> getParams() {
        return params;
    }

    public void setParams(List<BasicNameValuePair> params) {
        this.params = params;
    }

    /**
     * 通过参数的name获取参数的value
     * @param name
     * @return
     */
    public String getParamByName(String name) {
        for (BasicNameValuePair param : params) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "HttpRequestModel{" +
                "url='" + url + '\'' +
                ", params=" + params +
                '}';
    }
}
