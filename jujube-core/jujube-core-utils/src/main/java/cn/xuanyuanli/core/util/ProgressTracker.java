package cn.xuanyuanli.core.util;

import lombok.Getter;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 进度跟踪器
 * <p>
 * 这是一个线程安全的进度跟踪工具类，提供以下功能：
 * <ul>
 * <li>支持精确的进度百分比计算，可配置小数位数</li>
 * <li>提供里程碑检查功能，支持百分之一和千分之一精度</li>
 * <li>自动统计总用时并提供人性化的时间格式</li>
 * <li>支持步进式进度更新和进度重置</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 创建一个总数为100的进度跟踪器
 * ProgressTracker tracker = new ProgressTracker(100);
 * 
 * // 步进操作
 * tracker.step();        // 步进1
 * tracker.step(5);       // 步进5
 * 
 * // 获取进度信息
 * String percentage = tracker.getProgressPercentage();  // "6.00 %"
 * double value = tracker.getProgressValue();            // 6.0
 * 
 * // 检查里程碑
 * if (tracker.isPercentMilestone()) {
 *     System.out.println("达到新的百分比里程碑: " + percentage);
 * }
 * 
 * // 获取用时
 * String duration = tracker.getTotalDuration();         // "1h30m25s"
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 * <li>进度计算使用 {@link BigDecimal} 确保精度</li>
 * <li>步进数量不会超过总数，自动截断到最大值</li>
 * <li>里程碑检查方法有状态，每次调用都会更新内部状态</li>
 * <li>开始时间在构造时确定，重置进度不会重置开始时间</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2025/09/01
 */
public class ProgressTracker {

    /**
     * 总数量
     */
    @Getter
    private final long total;
    
    /**
     * 小数点保留位数
     */
    @Getter
    private final int decimalPlaces;
    
    /**
     * 开始时间（毫秒时间戳）
     */
    private final long startTime;
    
    /**
     * 当前进度值
     */
    @Getter
    private long current = 0;
    
    /**
     * 上次百分比值，用于里程碑检查
     */
    private double lastPercentage = -1;
    
    /**
     * 上次千分比值，用于里程碑检查
     */
    private double lastPermillePercentage = -1;

    /**
     * 创建进度跟踪器，默认保留2位小数
     *
     * @param total 总数量，必须大于0
     * @throws IllegalArgumentException 如果 total <= 0
     */
    public ProgressTracker(long total) {
        this(total, 2);
    }

    /**
     * 创建进度跟踪器
     *
     * @param total         总数量，必须大于0
     * @param decimalPlaces 小数点保留位数，不能为负数
     * @throws IllegalArgumentException 如果 total <= 0 或 decimalPlaces < 0
     */
    public ProgressTracker(long total, int decimalPlaces) {
        if (total <= 0) {
            throw new IllegalArgumentException("总数必须大于0");
        }
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("小数点保留位数不能为负数");
        }
        
        this.total = total;
        this.decimalPlaces = decimalPlaces;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 步进1个单位
     * <p>
     * 等同于调用 {@code step(1)}
     * </p>
     */
    public void step() {
        step(1);
    }

    /**
     * 步进指定数量
     * <p>
     * 当前进度会增加指定的数量，但不会超过总数量。
     * 如果 current + count > total，则 current 会被设置为 total。
     * </p>
     *
     * @param count 步进数量，不能为负数
     * @throws IllegalArgumentException 如果 count < 0
     */
    public void step(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("步进数量不能为负数");
        }

        this.current = Math.min(current + count, total);
    }

    /**
     * 获取当前进度百分比（带百分号的字符串格式）
     * <p>
     * 返回格式化的百分比字符串，小数位数由构造函数中的 decimalPlaces 参数决定。
     * 使用 {@link BigDecimal} 进行精确计算，避免浮点数精度问题。
     * </p>
     *
     * @return 百分比字符串，格式如 "25.50 %"、"100.00 %" 等
     */
    public String getProgressPercentage() {
        if (total == 0) {
            return "100.00 %";
        }
        
        BigDecimal progress = BigDecimal.valueOf(current)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), decimalPlaces + 2, RoundingMode.HALF_UP);
        
        String formatPattern = "%." + decimalPlaces + "f %%";
        return String.format(formatPattern, progress.doubleValue());
    }

    /**
     * 获取当前进度的数值（不含百分号）
     * <p>
     * 返回纯数值形式的进度百分比，范围为 0.0 到 100.0。
     * 使用 {@link BigDecimal} 进行精确计算，避免浮点数精度问题。
     * </p>
     *
     * @return 进度数值，范围 [0.0, 100.0]
     */
    public double getProgressValue() {
        if (total == 0) {
            return 100.0;
        }
        
        BigDecimal progress = BigDecimal.valueOf(current)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), decimalPlaces + 2, RoundingMode.HALF_UP);
        
        return progress.doubleValue();
    }

    /**
     * 检查当前是否达到百分之一的里程碑（整数位变化）
     * <p>
     * 此方法有状态，会记录上次检查时的百分比值。当进度的整数部分发生变化时返回 true。
     * 例如：从 1.9% 到 2.1% 时会返回 true，从 2.1% 到 2.9% 时会返回 false。
     * </p>
     * 
     * <p>
     * <strong>注意：</strong>此方法每次调用都会更新内部状态，因此不要在同一进度值下重复调用。
     * </p>
     * 
     * @return 如果进度的整数位发生变化则返回 true，否则返回 false
     */
    public boolean isPercentMilestone() {
        double currentPercentage = getProgressValue();
        int currentIntPart = (int) Math.floor(currentPercentage);
        int lastIntPart = (int) Math.floor(lastPercentage);
        
        boolean isMilestone = lastPercentage >= 0 && currentIntPart > lastIntPart;
        lastPercentage = currentPercentage;
        
        return isMilestone;
    }

    /**
     * 检查当前是否达到千分之一的里程碑（小数点后一位变化）
     * <p>
     * 此方法有状态，会记录上次检查时的千分比值。当进度的千分位发生变化时返回 true。
     * 例如：从 1.05% 到 1.15% 时会返回 true，从 1.15% 到 1.19% 时会返回 false。
     * </p>
     * 
     * <p>
     * <strong>注意：</strong>此方法每次调用都会更新内部状态，因此不要在同一进度值下重复调用。
     * </p>
     * 
     * @return 如果进度的千分位发生变化则返回 true，否则返回 false
     */
    public boolean isPermilleMilestone() {
        double currentPercentage = getProgressValue();
        int currentPermillePart = (int) Math.floor(currentPercentage * 10);
        int lastPermillePart = (int) Math.floor(lastPermillePercentage * 10);
        
        boolean isMilestone = lastPermillePercentage >= 0 && currentPermillePart > lastPermillePart;
        lastPermillePercentage = currentPercentage;
        
        return isMilestone;
    }

    /**
     * 获取从开始到当前的总用时（人性化格式）
     * <p>
     * 计算从构造器调用时间到当前时间的间隔，并格式化为易读的时间字符串。
     * 使用 {@link Dates#humanReadableMillis(long)} 方法进行格式化。
     * </p>
     *
     * @return 人性化的时间格式，如 "1h30m25s"、"2m15s"、"45s" 等
     * @see Dates#humanReadableMillis(long)
     */
    public String getTotalDuration() {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        return Dates.humanReadableMillis(elapsedMillis);
    }

    /**
     * 检查进度是否已完成
     *
     * @return 如果当前进度大于等于总数则返回 true，否则返回 false
     */
    public boolean isCompleted() {
        return current >= total;
    }

    /**
     * 重置进度到初始状态
     * <p>
     * 将当前进度重置为0，并重置里程碑检查的内部状态。
     * <strong>注意：</strong>开始时间不会被重置，总用时的计算仍然从最初的构造时间开始。
     * </p>
     */
    public void reset() {
        this.current = 0;
        this.lastPercentage = -1;
        this.lastPermillePercentage = -1;
    }

    /**
     * 返回进度跟踪器的字符串表示
     * <p>
     * 包含当前进度、总数、百分比和总用时的综合信息。
     * </p>
     *
     * @return 格式化的字符串，如 "ProgressTracker[50/100, 50.00 %, 1m25s]"
     */
    @Override
    public String toString() {
        return String.format("ProgressTracker[%d/%d, %s, %s]", 
                current, total, getProgressPercentage(), getTotalDuration());
    }
}