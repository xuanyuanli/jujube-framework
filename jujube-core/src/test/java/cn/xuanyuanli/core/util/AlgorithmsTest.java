package cn.xuanyuanli.core.util;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AlgorithmsTest {

    @Test
    void testGreedy() {
        List<Double> result = Algorithms.greedy(Arrays.asList(1d, 3d, 4d, 3d, 4d, 30d, 41d), 75d);
        Assertions.assertEquals(Lists.newArrayList(41.0d, 30.0d, 4.0d), result);
    }
}

