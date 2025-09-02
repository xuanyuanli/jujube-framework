package cn.xuanyuanli.core.util.office;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Excel读取器工具类测试")
class ExcelReaderUtilTest {

    @Nested
    @DisplayName("数值转换功能测试")
    class NumberConversionTests {

        @Test
        @DisplayName("字符串转数值应返回正确类型")
        void toNumber_shouldReturnCorrectNumber_whenValidStringProvided() {
            // Arrange
            String input = "1.0";

            // Act
            Number result = ExcelReaderUtil.toNumber(input);

            // Assert
            assertThat(result).isEqualTo(1.0D);
        }

        @Test
        @DisplayName("解析带逗号分隔符的数值应返回正确结果")
        void parseToNumber_shouldReturnCorrectNumber_whenCommaSeparatedNumber() {
            // Arrange
            String longValue = "12,234.0";
            String doubleValue = "12,234.2";

            // Act
            Object longResult = ExcelReaderUtil.parseToNumber(longValue);
            Object doubleResult = ExcelReaderUtil.parseToNumber(doubleValue);

            // Assert
            assertThat(longResult).isEqualTo(12234L);
            assertThat(doubleResult).isEqualTo(12234.2);
        }

        @Test
        @DisplayName("解析空值和无效值应返回0")
        void parseToNumber_shouldReturnZero_whenNullOrInvalidValue() {
            // Arrange & Act & Assert
            assertThat(ExcelReaderUtil.parseToNumber(null)).isEqualTo(0);
            assertThat(ExcelReaderUtil.parseToNumber("")).isEqualTo(0);
            assertThat(ExcelReaderUtil.parseToNumber("null")).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("字符串转换功能测试")
    class StringConversionTests {

        @Test
        @DisplayName("期望字符串值转换应去除不必要的小数点")
        void toExpectStringValue_shouldRemoveUnnecessaryDecimalPoint_whenIntegerValue() {
            // Arrange
            String decimalInput = "100.0";
            String integerInput = "100";

            // Act
            String decimalResult = ExcelReaderUtil.toExpectStringValue(decimalInput);
            String integerResult = ExcelReaderUtil.toExpectStringValue(integerInput);

            // Assert
            assertThat(decimalResult).isEqualTo("100");
            assertThat(integerResult).isEqualTo("100");
        }

        @Test
        @DisplayName("拒绝特殊字符应只保留数字字符")
        void rejectSpecialChar_shouldRetainOnlyNumericCharacters_whenSpecialCharsPresent() {
            // Arrange
            String input = "$#%&12";

            // Act
            String result = ExcelReaderUtil.rejectSpecialChar(input);

            // Assert
            assertThat(result).isEqualTo("12");
        }
    }

    @Nested
    @DisplayName("行数据处理功能测试")
    class RowDataProcessingTests {

        @Test
        @DisplayName("空行判断应正确识别空行和非空行")
        void isEmtpyRow_shouldCorrectlyIdentifyEmptyAndNonEmptyRows_whenDifferentRowTypes() {
            // Arrange
            List<String> emptyRow = new ArrayList<>();
            List<String> nonEmptyRow = new ArrayList<>(Arrays.asList("1", "2", "3"));

            // Act
            boolean emptyResult = ExcelReaderUtil.isEmtpyRow(emptyRow);
            boolean nonEmptyResult = ExcelReaderUtil.isEmtpyRow(nonEmptyRow);

            // Assert
            assertThat(emptyResult).isTrue();
            assertThat(nonEmptyResult).isFalse();
        }
    }

    @Nested
    @DisplayName("模板标题处理功能测试")
    class TemplateTitleProcessingTests {

        @Test
        @DisplayName("模板标题转标准属性应过滤特殊字符")
        void templateTitlesToStandardAttrs_shouldFilterSpecialCharacters_whenProcessingTitles() {
            // Arrange
            List<String> input = Collections.singletonList("$#%&12");

            // Act
            List<String> result = ExcelReaderUtil.templateTitlesToStandardAttrs(input);

            // Assert
            assertThat(result).isEqualTo(Collections.singletonList("12"));
        }
    }

    @Nested
    @DisplayName("文件类型判断功能测试")
    class FileTypeValidationTests {

        @Test
        @DisplayName("Excel文件判断应正确识别有效和无效扩展名")
        void isExcelFile_shouldCorrectlyIdentifyExcelFiles_whenDifferentExtensions() {
            // Arrange
            String xlsFile = "1.xls";
            String xlsxFile = "1.xlsx";
            String invalidFile = "1.xlss";

            // Act
            boolean xlsResult = ExcelReaderUtil.isExcelFile(xlsFile);
            boolean xlsxResult = ExcelReaderUtil.isExcelFile(xlsxFile);
            boolean invalidResult = ExcelReaderUtil.isExcelFile(invalidFile);

            // Assert
            assertThat(xlsResult).isTrue();
            assertThat(xlsxResult).isTrue();
            assertThat(invalidResult).isFalse();
        }
    }
}
