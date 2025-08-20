package cn.xuanyuanli.core.util.office;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Objects;
import cn.xuanyuanli.core.util.Resources;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

class WordReaderTest {

    @Test
    void getWordContent() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/office/testWord.docx");
        assertEquals("""
                hello
                world
                123
                """, WordReader.getWordContent(Objects.requireNonNull(resource).getFile().getAbsolutePath()));
    }
}
