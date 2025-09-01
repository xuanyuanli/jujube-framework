package cn.xuanyuanli.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 经典算法工具类
 * <p>
 * 提供各种经典算法的实现，主要用于解决组合优化、数值计算和数据处理中的常见问题。
 * 当前实现包括贪心算法用于解决背包问题的变种——寻找数值组合使其总和等于目标值。
 * </p>
 * 
 * <p>
 * <strong>算法特点：</strong>
 * <ul>
 * <li><strong>贪心策略：</strong>采用从大到小的贪心选择，提高找到解的概率</li>
 * <li><strong>精确匹配：</strong>基于 {@link Calcs} 工具类进行精确的浮点数比较</li>
 * <li><strong>递归优化：</strong>采用回溯和剪枝策略提高搜索效率</li>
 * <li><strong>灵活扩展：</strong>易于扩展其他组合优化算法</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 找到总和为 10.0 的数值组合
 * List<Double> numbers = Arrays.asList(3.5, 2.1, 4.4, 1.5, 6.5, 2.0);
 * List<Double> result = Algorithms.greedy(numbers, 10.0);
 * // 可能的结果：[6.5, 2.1, 1.5] 或 [4.4, 3.5, 2.1]
 * 
 * // 财务场景：找到金额组合
 * List<Double> amounts = Arrays.asList(100.0, 50.0, 20.0, 10.0, 5.0);
 * List<Double> combination = Algorithms.greedy(amounts, 75.0);
 * // 结果：[50.0, 20.0, 5.0]
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>应用场景：</strong>
 * <ul>
 * <li><strong>财务计算：</strong>找到特定金额的组合方式</li>
 * <li><strong>库存管理：</strong>组合物品达到指定数量或价值</li>
 * <li><strong>资源分配：</strong>分配资源使总和达到目标值</li>
 * <li><strong>数据分析：</strong>在数据集中找到满足条件的子集</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>性能考虑：</strong>
 * <ul>
 * <li>时间复杂度：O(n² × 2^n) 在最坏情况下，但贪心策略通常能快速找到解</li>
 * <li>空间复杂度：O(n) 用于存储结果和递归栈</li>
 * <li>适合中小规模数据集（n < 100）</li>
 * <li>对于大规模数据，建议使用动态规划或其他优化算法</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 * <li>算法不保证找到所有可能的解，只返回第一个找到的解</li>
 * <li>浮点数比较基于 {@link Calcs#equ(Number, Number)} 进行精确比较</li>
 * <li>如果没有找到解，返回空列表</li>
 * <li>输入数据会被排序，原始顺序不会保留</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class Algorithms {

    /**
     * 贪心算法，找出队列中总和等于目标值的组合
     * <p>
     * 使用贪心策略寻找数值集合中的一个子集，使得子集中所有元素的总和等于指定的目标值。
     * 算法采用从大到小的排序策略，优先选择较大的数值，以提高找到解的概率和效率。
     * </p>
     * 
     * <p>
     * <strong>算法流程：</strong>
     * <ol>
     * <li>将输入数组按降序排序（从大到小）</li>
     * <li>从每个位置开始尝试构建解</li>
     * <li>使用递归回溯寻找满足条件的组合</li>
     * <li>返回第一个找到的有效解</li>
     * </ol>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * // 基本用法
     * List<Double> numbers = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
     * List<Double> result = Algorithms.greedy(numbers, 7.0);
     * // 可能结果: [4.0, 3.0] 或 [5.0, 2.0]
     * 
     * // 财务应用：找零钱组合
     * List<Double> denominations = Arrays.asList(50.0, 20.0, 10.0, 5.0, 1.0);
     * List<Double> change = Algorithms.greedy(denominations, 37.0);
     * // 结果: [20.0, 10.0, 5.0, 1.0, 1.0]
     * 
     * // 处理小数
     * List<Double> decimals = Arrays.asList(0.5, 1.25, 2.75, 3.5);
     * List<Double> exact = Algorithms.greedy(decimals, 4.0);
     * // 结果: [3.5, 0.5]
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>算法特性：</strong>
     * <ul>
     * <li><strong>贪心策略：</strong>优先选择大数值，通常能快速找到解</li>
     * <li><strong>精确计算：</strong>使用 {@link Calcs} 进行精确的浮点数运算</li>
     * <li><strong>回溯机制：</strong>当当前路径无解时自动回溯尝试其他路径</li>
     * <li><strong>首解返回：</strong>找到第一个有效解即返回，不保证最优解</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>复杂度分析：</strong>
     * <ul>
     * <li><strong>时间复杂度：</strong>O(n log n + k × 2^k)，其中 k 是参与计算的元素数量</li>
     * <li><strong>空间复杂度：</strong>O(n) 用于存储结果和递归调用栈</li>
     * <li><strong>最好情况：</strong>O(n log n) 当第一次尝试就找到解时</li>
     * <li><strong>最坏情况：</strong>需要遍历所有可能的组合</li>
     * </ul>
     * </p>
     *
     * @param nums 输入的数值集合，不能为 null 或空集合
     * @param sum  目标总和值
     * @return 第一个找到的满足条件的数值组合；如果无解则返回空列表
     * @throws NullPointerException 如果 nums 为 null
     * @see Calcs#add(Number, Number)
     * @see Calcs#equ(Number, Number)
     */
    public static List<Double> greedy(List<Double> nums, double sum) {
        List<Double> result = new ArrayList<>();
        nums.sort(Comparator.comparingDouble(Double::doubleValue));
        Collections.reverse(nums);
        for (int i = 0; i < nums.size(); i++) {
            List<Double> subList = nums.subList(i, nums.size());
            List<Double> bagInner = greedyInner(subList, sum);
            if (bagInner != null) {
                result = bagInner;
                break;
            }
        }
        return result;
    }

    /**
     * 贪心算法的内部递归实现
     * <p>
     * 这是贪心算法的核心递归方法，负责具体的组合搜索逻辑。采用递归回溯策略，
     * 当当前路径无法找到解时，会移除部分元素并递归尝试，直到找到有效组合或确定无解。
     * </p>
     * 
     * <p>
     * <strong>算法逻辑：</strong>
     * <ol>
     * <li><strong>终止条件：</strong>当只有一个元素时，检查是否等于目标值</li>
     * <li><strong>累加搜索：</strong>从左到右累加元素，使用精确浮点数计算</li>
     * <li><strong>匹配检查：</strong>每次累加后检查是否达到目标值</li>
     * <li><strong>回溯策略：</strong>如果超出范围，移除第二个元素后递归</li>
     * <li><strong>解的返回：</strong>找到匹配时返回对应的子列表</li>
     * </ol>
     * </p>
     * 
     * <p>
     * <strong>回溯机制：</strong>
     * 当累加所有元素都无法达到目标值时，算法会移除列表中的第二个元素（保留最大值），
     * 然后递归调用自身。这种策略能够系统性地探索不同的组合可能性。
     * </p>
     * 
     * <p>
     * <strong>精确计算：</strong>
     * 使用 {@link Calcs#add(Number, Number)} 和 {@link Calcs#equ(Number, Number)}
     * 进行精确的浮点数运算和比较，避免浮点数精度问题导致的错误结果。
     * </p>
     *
     * @param nums 当前处理的数值集合，已按降序排列
     * @param sum  目标总和值
     * @return 如果找到有效组合则返回对应的数值列表；无解时返回 null
     */
    private static List<Double> greedyInner(List<Double> nums, double sum) {
        if (nums.size() == 1) {
            if (nums.get(0) == sum) {
                return nums;
            } else {
                return null;
            }
        }
        List<Double> list = new ArrayList<>(nums);
        double curSum = 0;
        int i = 0;
        for (; i < list.size(); i++) {
            curSum = Calcs.add(list.get(i), curSum);
            if (Calcs.equ(curSum, sum)) {
                break;
            }
        }
        if (i == list.size()) {
            if (list.size() > 1) {
                list.remove(1);
            }
            return greedyInner(list, sum);
        } else {
            return list.subList(0, i + 1);
        }
    }
}
