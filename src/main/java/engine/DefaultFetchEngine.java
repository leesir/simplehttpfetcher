package engine;

import model.HttpRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TimeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单线程模式的数据拉取。
 * Created by lijing on 2014/10/9.
 */
public class DefaultFetchEngine extends BaseFetchEngine {

    /**
     * 日志对象
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Map<HttpRequestModel, String> getResult(List<HttpRequestModel> modelList) {
        logger.info("DefaultFetchEngine start fetching data. List size: {}", modelList.size());
        long fetchStart = TimeUtil.getMillisTimestamp();
        Map<HttpRequestModel, String> returnValue = new HashMap<>();
        if (modelList.size() > 0) {
            for (HttpRequestModel httpRequestModel : modelList) {
                try {
                    returnValue.put(httpRequestModel, sendPostRequest(
                            httpRequestModel.getUrl(), httpRequestModel.getParams()));
                } catch (Exception e) {
                    logger.error("Error occured while fetching: {}", httpRequestModel);
                }
            }
        }
        long fetchFinish = TimeUtil.getMillisTimestamp();
        logger.info("Fetch finished after: {}ms. List size: {}", (fetchFinish - fetchStart), modelList.size());
        return returnValue;
    }
}
