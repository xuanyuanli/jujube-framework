package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class CamelCaseTest {

    @Test
    public void testToUnderlineName() {
        assertThat(CamelCase.toUnderlineName("cardId")).isEqualTo("card_id");
    }

    @Test
    public void testToCamelCase() {
        assertThat(CamelCase.toCamelCase("card_id")).isEqualTo("cardId");
    }

    @Test
    public void testToSpecilCamelCase() {
        assertThat(CamelCase.toSpecilCamelCase("Card_id")).isEqualTo("cardId");
    }

    @Test
    public void testToCapitalizeCamelCase() {
        assertThat(CamelCase.toCapitalizeCamelCase("card_id")).isEqualTo("CardId");
    }

}
