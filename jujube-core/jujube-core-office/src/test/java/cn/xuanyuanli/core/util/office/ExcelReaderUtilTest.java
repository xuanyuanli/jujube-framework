package cn.xuanyuanli.core.util.office;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExcelReaderUtilTest {

    @Test
    void testToNumber() {
        Number result = ExcelReaderUtil.toNumber("1.0");
        Assertions.assertEquals(1.0D, result);
    }

    @Test
    void testToExpectStringValue() {
        Assertions.assertEquals("100", ExcelReaderUtil.toExpectStringValue("100.0"));
        Assertions.assertEquals("100", ExcelReaderUtil.toExpectStringValue("100"));
    }

    @Test
    void testIsEmtpyRow() {
        Assertions.assertTrue(ExcelReaderUtil.isEmtpyRow(new ArrayList<>()));
        Assertions.assertFalse(ExcelReaderUtil.isEmtpyRow(new ArrayList<>(Arrays.asList("1", "2", "3"))));
    }

    @Test
    void testTemplateTitlesToStandardAttrs() {
        List<String> result = ExcelReaderUtil.templateTitlesToStandardAttrs(Collections.singletonList("$#%&12"));
        Assertions.assertEquals(Collections.singletonList("12"), result);
    }

    @Test
    void testRejectSpecialChar() {
        String result = ExcelReaderUtil.rejectSpecialChar("$#%&12");
        Assertions.assertEquals("12", result);
    }

    @Test
    void testParseToNumber() {
        Assertions.assertEquals(0, ExcelReaderUtil.parseToNumber(null));
        Assertions.assertEquals(0, ExcelReaderUtil.parseToNumber(""));
        Assertions.assertEquals(0, ExcelReaderUtil.parseToNumber("null"));
        Assertions.assertEquals(12234L, ExcelReaderUtil.parseToNumber("12,234.0"));
        Assertions.assertEquals(12234.2, ExcelReaderUtil.parseToNumber("12,234.2"));
    }

    @Test
    void testIsExcelFile() {
        Assertions.assertTrue(ExcelReaderUtil.isExcelFile("1.xls"));
        Assertions.assertTrue(ExcelReaderUtil.isExcelFile("1.xlsx"));
        Assertions.assertFalse(ExcelReaderUtil.isExcelFile("1.xlss"));
    }
}
