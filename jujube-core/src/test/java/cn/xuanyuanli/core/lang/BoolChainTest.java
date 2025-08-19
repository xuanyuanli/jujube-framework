package cn.xuanyuanli.core.lang;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoolChainTest {

    @Test
    public void eqThen() {
        AtomicInteger n = new AtomicInteger();
        BoolChain.of(null).eqThen(null, () -> n.set(1)).eqThen(1, () -> n.set(2));
        Assertions.assertThat(n.get()).isEqualTo(1);

        BoolChain.of(1).eqThen(null, () -> n.set(1)).eqThen(1, () -> n.set(2));
        Assertions.assertThat(n.get()).isEqualTo(2);

        BoolChain.of(3).eqThen(1, () -> n.set(1)).eqThen(2, () -> n.set(2)).orElse(() -> n.set(3));
        Assertions.assertThat(n.get()).isEqualTo(3);
    }

    @Test
    public void then() {
        AtomicInteger n = new AtomicInteger();
        BoolChain.of("ab").then(StringUtils::isBlank, () -> n.set(1)).then(s -> s.endsWith("b"), () -> n.set(2));
        Assertions.assertThat(n.get()).isEqualTo(2);

        BoolChain.of("abc").then(StringUtils::isBlank, () -> n.set(1)).then(s -> s.endsWith("b"), () -> n.set(2)).then(s -> s.endsWith("c"), () -> n.set(3));
        Assertions.assertThat(n.get()).isEqualTo(3);
    }
}
