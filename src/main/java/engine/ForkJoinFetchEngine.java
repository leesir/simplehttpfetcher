package engine;

import model.HttpRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TimeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/**
 * 利用Java内置Fork & Join框架实现的并发请求http的引擎。
 * Created by lijing on 2014/10/8.
 */
public class ForkJoinFetchEngine extends BaseFetchEngine {

    /**
     * 虚拟机可用cpu核数
     */
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    /**
     * log对象
     */
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Map<HttpRequestModel, String> getResult(List<HttpRequestModel> modelList) {
        log.info("ForkJoinFetchEngine start fetching data. List size: {}", modelList.size());
        long fetchStart = TimeUtil.getMillisTimestamp();
        Map<HttpRequestModel, String> returnValue = new HashMap<>();
        //执行任务的线程是任务树的叶子节点，需要让所有节点一起执行，需要把整棵树纳入到工作线程池中
        //即线程池的并发数等于树的节点数
        ForkJoinPool forkJoinPool = new ForkJoinPool(2 * AVAILABLE_PROCESSORS - 1);
        log.info("initialize pool size: {}", forkJoinPool.getParallelism());
        ForkJoinFetchEngineTask fetchTask = new ForkJoinFetchEngineTask(0, modelList.size() - 1, modelList);
        Future<Map<HttpRequestModel, String>> fetchResult = forkJoinPool.submit(fetchTask);
        try {
            returnValue = fetchResult.get();
        } catch (CancellationException cancellationException) {
            log.info("{} was cancelled.", fetchTask.getTaskName());
            log.error(cancellationException.getMessage(), cancellationException);
            throw cancellationException;
        } catch (Exception e) {
            log.error("{} get error. Exception msg: {}", fetchTask.getTaskName(), e.getMessage());
            log.error(e.getMessage(), e);
        } finally {
            forkJoinPool.shutdown();
        }
        long fetchFinish = TimeUtil.getMillisTimestamp();
        log.info("Fetch finished after: {}ms", (fetchFinish - fetchStart));
        return returnValue;
    }

    /**
     * ForkJoinFetchEngine引擎内部执行时的任务。
     */
    private class ForkJoinFetchEngineTask extends RecursiveTask<Map<HttpRequestModel, String>> {

        /**
         * 任务范围起始下标。
         */
        private int start = 0;

        /**
         * 任务范围结束下标。
         */
        private int end = 0;

        /**
         * 该任务需要执行的所有task的http参数。
         */
        private List<HttpRequestModel> modelList;

        /**
         * 任务名称。
         */
        private String taskName;

        /**
         * 利用待执行task的http参数列表范围，构造fork-join框架任务。
         * @param start 范围起始位置。
         * @param end 范围结束位置。
         * @param modelList 待执行task的http参数列表。
         */
        public ForkJoinFetchEngineTask(int start, int end, List<HttpRequestModel> modelList) {
            this.start = start;
            this.end = end;
            this.modelList = modelList;
            taskName = "ForkJoinFetchEngineTask_" + start + "_" + end;
            log.info("new ForkJoinFetchEngineTask " + start + " " + end + " " + modelList.size() + " " + taskName);
        }

        @Override
        protected Map<HttpRequestModel, String> compute() {
            /**
             * 阀值
             */
            Map<HttpRequestModel, String> returnValue = new HashMap<>();
            if (modelList == null || modelList.isEmpty()) {
                return returnValue;
            }
            //每个task只负责至多threshHold个操作。
            int threshHold = modelList.size() / AVAILABLE_PROCESSORS;
            boolean startCompute = (end - start) <= threshHold;
            if (startCompute) {
                //该任务的操作数量小于阀值，则开始计算。
                log.info("{} start.", taskName);
                for (int i = start; i <= end; i++) {
                    HttpRequestModel currentModel = modelList.get(i);
                    try {
                        returnValue.put(currentModel,
                                ForkJoinFetchEngine.this.sendPostRequest(currentModel.getUrl(),
                                        currentModel.getParams()));
                    } catch (Exception e) {
                        log.error("Error occured while fetching: {}, exception message: {}",
                                currentModel, e.getMessage());
                    }
                }
            } else {
                //否则继续生成任务树下一层级。
                int middle = (start + end) / 2;
                //该任务的左孩子。
                ForkJoinFetchEngineTask leftEngine = new ForkJoinFetchEngineTask(start, middle, modelList);
                //该任务的右孩子。
                ForkJoinFetchEngineTask rightEngine = new ForkJoinFetchEngineTask(middle + 1, end, modelList);
                //等到左右孩子任务返回。
                leftEngine.fork();
                rightEngine.fork();
                //孩子任务返回后，汇总结果，返回上层。调用join线程会阻塞，即blocked。
                Map<HttpRequestModel, String> leftResult = leftEngine.join();
                log.info("{} completed.", leftEngine.getTaskName());
                Map<HttpRequestModel, String> rightResult = rightEngine.join();
                log.info("{} completed.", rightEngine.getTaskName());
                returnValue.putAll(leftResult);
                returnValue.putAll(rightResult);
            }
            return returnValue;
        }

        /**
         * 获取fork-join任务名称。
         * @return 任务名称。
         */
        public String getTaskName() {
            return taskName;
        }
    }
}
