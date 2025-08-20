package cn.xuanyuanli.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 算法集合
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class Algorithms {

    /**
     * 贪心算法，找出队列中总和等于sum的组合
     *
     * @param nums 数字集合
     * @param sum  总和
     * @return {@link List}<{@link Double}>
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
     * 贪婪（内部）
     *
     * @param nums 数字集合
     * @param sum  总和
     * @return {@link List}<{@link Double}>
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
