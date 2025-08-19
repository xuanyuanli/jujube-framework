package cn.xuanyuanli.jdbc.base.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import cn.xuanyuanli.jdbc.exception.DaoInitializeException;
import org.junit.jupiter.api.Test;

class StringsTest {

    @Test
    void testSplitByAnd() {
        // Test case 1: Simple split
        String[] result = Strings.splitByAnd("FirstNameAndLastName");
        assertArrayEquals(new String[]{"FirstName", "LastName"}, result);

        // Test case 2: Multiple splits
        result = Strings.splitByAnd("FirstNameAndMiddleNameAndLastName");
        assertArrayEquals(new String[]{"FirstName", "MiddleName", "LastName"}, result);

        // Test case 3: No split
        result = Strings.splitByAnd("FirstName");
        assertArrayEquals(new String[]{"FirstName"}, result);

        // Test case 4: Edge case with leading "And"
        result = Strings.splitByAnd("AndFirstNameAndLastName");
        assertArrayEquals(new String[]{"AndFirstName", "LastName"}, result);

        // Test case 5: Edge case with trailing "And"
        result = Strings.splitByAnd("FirstNameAndLastNameAnd");
        assertArrayEquals(new String[]{"FirstName", "LastName"}, result);

        // Test case 6: Caching behavior
        Strings.splitByAnd("FirstNameAndLastName");
        assertTrue(Strings.SPLIT_BY_AND_CACHE.containsKey("FirstNameAndLastName"));

        result = Strings.splitByAnd("FirstName1AndLastName2AndAge");
        assertArrayEquals(new String[]{"FirstName1", "LastName2", "Age"}, result);

        result = Strings.splitByAnd("FirstName1AndLastName2AndAgeAndMing");
        assertArrayEquals(new String[]{"FirstName1", "LastName2", "Age", "Ming"}, result);

        List<String> list = Arrays.asList(Strings.splitByAnd("IdEqAndNameEqAndAgeGtAndAndroidEq"));
        assertThat(list).hasSize(4).containsExactly("IdEq", "NameEq", "AgeGt", "AndroidEq");

        list = Arrays.asList(Strings.splitByAnd("IdEqAndNameEqAndAgeGt"));
        assertThat(list).hasSize(3).containsExactly("IdEq", "NameEq", "AgeGt");

        list = Arrays.asList(Strings.splitByAnd("AndroidEqAndIdEqAndNameEqAndAgeGt"));
        assertThat(list).hasSize(4).containsExactly("AndroidEq", "IdEq", "NameEq", "AgeGt");

        list = Arrays.asList(Strings.splitByAnd("IdEqAndAndroidEqAndNameEqAndAgeGt"));
        assertThat(list).hasSize(4).containsExactly("IdEq", "AndroidEq", "NameEq", "AgeGt");

        list = Arrays.asList(Strings.splitByAnd("IdEq"));
        assertThat(list).hasSize(1).containsExactly("IdEq");

        list = Arrays.asList(Strings.splitByAnd("AndroidEq"));
        assertThat(list).hasSize(1).containsExactly("AndroidEq");
    }

    @Test
    public void testSplitByAndSpecial() {
        String[] result = Strings.splitByAnd("FirstName流AndLastName");
        assertArrayEquals(new String[]{"FirstName流", "LastName"}, result);

        result = Strings.splitByAnd("FirstName$And_LastName");
        assertArrayEquals(new String[]{"FirstName$", "_LastName"}, result);

        assertThrows(DaoInitializeException.class, () -> assertArrayEquals(new String[]{"FirstName流", "LastName"}, Strings.splitByAnd("FirstNameAAndLastName")),"Jpa方法名异常：FirstNameAAndLastName。请检查：1、字段名是否使用了sql的关键字；2、sql关键字在方法中是否保持了独立性，也就是前后没有歧义。例如And前面不能出现大写字母");
    }
}
