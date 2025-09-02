package cn.xuanyuanli.core.util.office;

import java.io.File;
import java.io.FileInputStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Microsoft Word 文档读取工具类
 * <p>
 * 基于 Apache POI 实现的 Word 文档内容提取工具，支持多种 Word 文档格式的文本内容读取。
 * 提供简单易用的 API 来提取 Word 文档中的纯文本内容，适用于文档内容分析、搜索等场景。
 * </p>
 * 
 * <p>
 * <strong>支持的文档格式：</strong>
 * <ul>
 * <li><strong>.doc：</strong>Word 97-2003 格式文档（使用 HWPF）</li>
 * <li><strong>.docx：</strong>Word 2007+ 格式文档（使用 XWPF）</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 读取 Word 文档内容
 * String content = WordReader.getWordContent("document.docx");
 * 
 * // 处理提取的文本
 * if (!content.isEmpty()) {
 *     System.out.println("文档内容：" + content);
 *     // 进行文本分析、搜索等操作
 * }
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>功能特性：</strong>
 * <ul>
 * <li><strong>自动格式识别：</strong>自动检测 .doc 和 .docx 格式并使用相应的解析器</li>
 * <li><strong>纯文本提取：</strong>提取文档的纯文本内容，去除格式信息</li>
 * <li><strong>异常处理：</strong>完善的异常处理机制，确保程序稳定性</li>
 * <li><strong>资源管理：</strong>自动管理文件流和解析器资源</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>技术实现：</strong>
 * <ul>
 * <li>优先尝试使用 HWPF WordExtractor 解析 .doc 格式</li>
 * <li>如果解析失败，自动切换到 XWPF XWPFWordExtractor 解析 .docx 格式</li>
 * <li>使用 try-with-resources 确保资源正确释放</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 * <li>仅提取纯文本内容，不包含图片、表格格式等信息</li>
 * <li>对于受密码保护的文档，需要先解密再读取</li>
 * <li>大文件读取时可能消耗较多内存</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WordReader {

    /**
     * 日志记录器
     * <p>
     * 用于记录 Word 文档读取过程中的错误和异常信息，
     * 特别是文档格式不兼容或文件损坏时的处理情况。
     * </p>
     */
    private static final Logger logger = LoggerFactory.getLogger(WordReader.class);

    /**
     * 读取 Word 文档内容
     * <p>
     * 从指定路径的 Word 文档中提取纯文本内容。支持 .doc 和 .docx 两种格式，
     * 采用自动格式识别技术，优先尝试 .doc 格式解析，失败后自动切换到 .docx 格式。
     * </p>
     * 
     * <p>
     * <strong>处理流程：</strong>
     * <ol>
     * <li>检查文件是否存在</li>
     * <li>优先使用 HWPF WordExtractor 解析（.doc 格式）</li>
     * <li>如果失败，切换到 XWPF XWPFWordExtractor（.docx 格式）</li>
     * <li>返回提取的纯文本内容</li>
     * </ol>
     * </p>
     * 
     * <p>
     * <strong>注意事项：</strong>
     * <ul>
     * <li>如果文件不存在或无法读取，返回空字符串</li>
     * <li>仅提取文本内容，不包含格式、图片、表格等信息</li>
     * <li>对于受密码保护的文档，可能无法正确读取</li>
     * </ul>
     * </p>
     *
     * @param filePath Word 文档的绝对路径或相对路径
     * @return 文档的纯文本内容，如果读取失败或文件不存在则返回空字符串
     * @see WordExtractor
     * @see XWPFWordExtractor
     */
    public static String getWordContent(String filePath) {
        String result = "";
        File wordFile = new File(filePath);
        if (wordFile.exists()) {
            // word2003版本
            try (WordExtractor wordExtractor = new WordExtractor(new FileInputStream(wordFile))) {
                result = wordExtractor.getText();
            } catch (Exception e) {
                // word2007版本
                try (XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(new XWPFDocument(new FileInputStream(wordFile)))) {
                    result = xwpfWordExtractor.getText();
                } catch (Exception e1) {
                    logger.error("getWordContent", e1);
                }
            }
        }
        return result;
    }

}
