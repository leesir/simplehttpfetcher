package engine;

import model.HttpRequestModel;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HttpClientWrapper;
import utils.TimeUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 拉取数据的引擎。
 * Created by lijing on 2014/10/9.
 */
public abstract class BaseFetchEngine {
    /**
     * 日志对象
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 根据传入的请求结构体访问url，并返回结果。返回值的key是HttpRequestModel对象，value是http请求url的结果。
     *
     * @param modelList 请求的url列表。
     * @return 每个url对应的返回值。
     */
    public abstract Map<HttpRequestModel, String> getResult(List<HttpRequestModel> modelList);

    /**
     * 对于只请求单个url的任务，可以直接获取Json的String串。
     *
     * @param httpRequestModel 请求的url与参数对象。
     * @return 返回值。
     */
    public String getResult(HttpRequestModel httpRequestModel) {
        return this.getResult(Collections.singletonList(httpRequestModel)).get(httpRequestModel);
    }

    /**
     * 以给定接口参数访问给定接口，并记录下访问耗时。
     *
     * @param url 待访问url。
     * @param params 访问url的参数。
     * @return 访问url的结果。
     * @throws Exception 异常
     */
    protected String sendPostRequest(String url, List<BasicNameValuePair> params) throws Exception {
        long start = TimeUtil.getMillisTimestamp();
        String result = HttpClientWrapper.sendPostRequest(url, params);
        long finish = TimeUtil.getMillisTimestamp();
        logger.info("Fetch data from url: {}, param: {} finish. Using {}ms.", url, params, (finish - start));
        return result;
    }
}
