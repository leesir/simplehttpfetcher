package engine;

/**
 * 获取数据拉取引擎。
 * <p/>
 * Created by lijing on 2014/10/9.
 */
public final class FetchEngineFactory {

    /**
     * 工厂类，构造函数私有化。
     */
    private FetchEngineFactory() {
    }

    /**
     * 默认单线程数据拉取。
     */
    public static final int DEFAULT_FETCH_ENGINE = 0;

    /**
     * FORK-JOIN模式的数据拉取。
     */
    public static final int FORK_JOIN_FETCH_ENGINE = 1;

    /**
     * 引擎工厂。
     * @param type 获取的类型。
     * @return 指定类型的拉取引擎。
     */
    public static BaseFetchEngine getFetchEngine(int type) {
        switch (type) {
            case DEFAULT_FETCH_ENGINE:
                return new DefaultFetchEngine();
            case FORK_JOIN_FETCH_ENGINE:
                return new ForkJoinFetchEngine();
            default:
                return new DefaultFetchEngine();
        }
    }
}
