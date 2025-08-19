package cn.xuanyuanli.core.util.snowflake;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * twitter Snowflake 算法，提供uid生成器
 * <pre>
 * twitter Snowflake 算法，提供uid生成器：
 * SnowFlake的结构如下(每部分用-分开):
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * 这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。
 * 41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId<br>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
 * <br>
 * 加起来刚好64位，为一个Long型。<br>
 * </pre>
 *
 * @author 庄梦蝶殇 linhuaichuan1989@126.com
 */
public class SnowflakeIdWorker {
    // ==============================
    // Constants===========================================
    /**
     * 回拨超时错误
     */
    private final static String ERROR_CLOCK_BACK = "时间回拨，拒绝为超出%d毫秒生成ID";

    private final static String ERROR_ATTR_LIMIT = "%s属性的范围为0-%d";

    private final static String MSG_UID_PARSE = "{\"UID\":\"%s\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"dataCenterId\":\"%d\",\"sequence\":\"%d\"}";

    private final static String DATE_PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    // ==============================Fields===========================================
    /** 开始时间截 (2017-12-25)，用于用当前时间戳减去这个时间戳，算出偏移量 */
    private final static long TWEPOCH = 1514131200000L;

    /** 机器id所占的位数(表示只允许workId的范围为：0-1023) */
    private final static long WORKER_ID_BITS = 5L;

    /** 数据标识id所占的位数 */
    private final static long DATACENTER_ID_BITS = 5L;

    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    public final static long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /** 支持的最大数据标识id，结果是31 */
    private final static long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /** 序列在id中占的位数 (表示只允许sequenceId的范围为：0-4095) */
    private final static long SEQUENCE_BITS = 12L;

    /** 机器ID向左移12位 */
    private final static long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 数据标识id向左移17位(12+5) */
    private final static long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /** 时间截向左移22位(5+5+12) */
    private final static long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /** 生成序列的掩码，(防止溢出:位与运算保证计算的结果范围始终是 0-4095，0b111111111111=0xfff=4095) */
    private final static long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /** 工作机器ID(0~31) */
    private final long workerId;

    /** 数据中心ID(0~31) */
    private final long datacenterId;

    /** 毫秒内序列(0~4095) */
    private long sequence = 0L;

    /** 上次生成ID的时间截 */
    private long lastTimestamp = -1L;

    private boolean isClock = false;

    public void setClock(boolean clock) {
        isClock = clock;
    }

    /**
     * 构造函数
     *
     * @param workerId
     *            工作ID (0~31)
     * @param datacenterId
     *            数据中心ID (0~31)
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format(ERROR_ATTR_LIMIT, "workerId", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format(ERROR_ATTR_LIMIT, "datacenterId", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // ==============================Methods==========================================
    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        // 闰秒：如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    // 时间偏差大小小于5ms，则等待两倍时间
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        // 还是小于，抛异常并上报
                        throw new RuntimeException(String.format(ERROR_CLOCK_BACK, lastTimestamp - timestamp));
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException(String.format(ERROR_CLOCK_BACK, lastTimestamp - timestamp));
            }
        }

        // 解决跨毫秒生成ID序列号始终为偶数的缺陷:如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            // 通过位与运算保证计算的结果范围始终是 0-4095
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        /*
         * 1.左移运算是为了将数值移动到对应的段(41、5、5，12那段因为本来就在最右，因此不用左移)
         * 2.然后对每个左移后的值(la、lb、lc、sequence)做位或运算，是为了把各个短的数据合并起来，合并成一个二进制数
         * 3.最后转换成10进制，就是最终生成的id(64位的ID)
         */
        return ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT) | (datacenterId << DATACENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
    }

    /**
     * 反解析UID
     *
     * @param uid uid
     * @return {@link String}
     */
    public String parseUid(Long uid) {
        // 总位数
        long totalBits = 64L;
        // 标识
        long signBits = 1L;
        // 时间戳
        long timestampBits = 41L;
        // 解析Uid：标识 -- 时间戳 -- 数据中心 -- 机器码 --序列
        long sequence = (uid << (totalBits - SEQUENCE_BITS)) >>> (totalBits - SEQUENCE_BITS);
        long dataCenterId = (uid << (timestampBits + signBits)) >>> (totalBits - DATACENTER_ID_BITS);
        long workerId = (uid << (timestampBits + signBits + DATACENTER_ID_BITS)) >>> (totalBits - WORKER_ID_BITS);
        long deltaSeconds = uid >>> (DATACENTER_ID_BITS + WORKER_ID_BITS + SEQUENCE_BITS);
        // 时间处理(补上开始时间戳)
        Date thatTime = new Date(TWEPOCH + deltaSeconds);
        String date = new SimpleDateFormat(DATE_PATTERN_DEFAULT).format(thatTime);
        // 格式化输出
        return String.format(MSG_UID_PARSE, uid, date, workerId, dataCenterId, sequence);
    }

    /**
     * 反解析UID(字符串截取，性能相对差；不推荐使用)
     *
     * @param uid uid
     * @return {@link String}
     */
    public String parseUid(String uid) {
        uid = Long.toBinaryString(Long.parseLong(uid));
        int len = uid.length();
        /* 解析Uid：标识 -- 时间戳 -- 数据中心 -- 机器码 --序列 */
        // sequence起始数
        int sequenceStart = len < WORKER_ID_SHIFT ? 0 : (int) (len - WORKER_ID_SHIFT);
        // worker起始数
        int workerStart = len < DATACENTER_ID_SHIFT ? 0 : (int) (len - DATACENTER_ID_SHIFT);
        // 时间起始数
        int timeStart = len < TIMESTAMP_LEFT_SHIFT ? 0 : (int) (len - TIMESTAMP_LEFT_SHIFT);
        String sequence = uid.substring(sequenceStart, len);
        String workerId = sequenceStart == 0 ? "0" : uid.substring(workerStart, sequenceStart);
        String dataCenterId = workerStart == 0 ? "0" : uid.substring(timeStart, workerStart);
        // 时间处理(补上开始时间戳)
        String time = timeStart == 0 ? "0" : uid.substring(0, timeStart);
        Date timeDate = new Date(Long.parseLong(time, 2) + TWEPOCH);
        String date = new SimpleDateFormat(DATE_PATTERN_DEFAULT).format(timeDate);
        // 格式化输出
        return String.format(MSG_UID_PARSE, uid, date, Integer.valueOf(workerId, 2), Integer.valueOf(dataCenterId, 2), Integer.valueOf(sequence, 2));
    }

    /**
     * 保证返回的毫秒数在参数之后(阻塞到下一个毫秒，直到获得新的时间戳)
     *
     * @param lastTimestamp
     *            上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获得系统当前毫秒数
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        if (isClock) {
            // 解决高并发下获取时间戳的性能问题
            return SystemClock.now();
        } else {
            return System.currentTimeMillis();
        }
    }
}
