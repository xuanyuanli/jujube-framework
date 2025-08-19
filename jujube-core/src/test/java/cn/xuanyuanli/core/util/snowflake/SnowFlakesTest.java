package cn.xuanyuanli.core.util.snowflake;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SnowFlakesTest {

    @Test
    public void nextId() {
        Assertions.assertThat(SnowFlakes.nextId()).isGreaterThan(0);
    }
}
