package cn.xuanyuanli.core.util.snowflake;

/**
 * 分布式唯一ID
 *
 * @author John Li
 */
public class SnowFlakes {

    private static final SnowflakeIdWorker ID_WORKER = new SnowflakeIdWorker(0, 0);

    /**
     * 获得分布式唯一ID
     *
     * @return long
     */
    public static long nextId() {
        return ID_WORKER.nextId();
    }
}
