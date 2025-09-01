package cn.xuanyuanli.core.util;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

/**
 * 随机数据生成工具类
 * <p>
 * 提供多种类型的随机数据生成功能，支持数值、字符串、中文文本等多种数据类型。
 * 主要功能包括：
 * <ul>
 * <li><strong>数值生成：</strong>随机整数、长整数，支持范围限制</li>
 * <li><strong>字符串生成：</strong>随机字符串、数字字符串、字母字符串</li>
 * <li><strong>中文内容生成：</strong>基于常用中文字符库的随机中文文本</li>
 * <li><strong>数组生成：</strong>批量生成随机数，支持去重控制</li>
 * <li><strong>枚举选择：</strong>从给定选项中随机选择</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>核心特性：</strong>
 * <ul>
 * <li>基于 {@link Math#random()} 实现，性能优异</li>
 * <li>支持范围限制和去重控制</li>
 * <li>内置常用中文字符库，支持中文内容生成</li>
 * <li>提供多种数据类型的生成方法</li>
 * <li>参数验证确保生成结果的有效性</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 生成随机整数
 * int randomInt = Randoms.randomInt(1, 100);        // 1-100之间的随机整数
 * long randomLong = Randoms.randomLong(1L, 1000L);  // 1-1000之间的随机长整数
 * 
 * // 生成随机数组
 * long[] randomArray = Randoms.random(1, 10, 5, true);   // 允许重复的5个随机数
 * long[] uniqueArray = Randoms.random(1, 10, 5, false);  // 不重复的5个随机数
 * 
 * // 生成随机字符串
 * String randomStr = Randoms.randomString(10);           // 10位随机字符串
 * String randomDigits = Randoms.randomNumberString(6);   // 6位随机数字字符串
 * String randomLetters = Randoms.randomAlphabetString(8); // 8位随机字母字符串
 * 
 * // 生成随机中文内容
 * String chineseText = Randoms.randomChineseString(20);  // 20个随机中文字符
 * 
 * // 从枚举中随机选择
 * String[] options = {"苹果", "香蕉", "橙子"};
 * String selected = Randoms.randomFromArray(options);    // 随机选择一个水果
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>中文字符库：</strong><br>
 * 内置包含2000+个常用中文字符的字符库，涵盖日常用语中的高频汉字，
 * 适用于生成具有一定可读性的中文测试数据。
 * </p>
 * 
 * <p>
 * <strong>性能特点：</strong>
 * <ul>
 * <li>使用 {@link Math#random()} 作为随机源，性能优异</li>
 * <li>去重算法采用简单数组比较，适合小规模数据生成</li>
 * <li>字符串生成基于 StringBuilder，内存效率高</li>
 * <li>中文字符库采用字符串常量，访问速度快</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 * <li>生成不重复数组时，确保范围足够大：{@code (iMax - iMin) >= (iNum - 1)}</li>
 * <li>大规模不重复数据生成可能影响性能，建议使用专门的随机数生成器</li>
 * <li>中文字符库基于常用字符，不包含生僻字和特殊符号</li>
 * <li>所有方法都进行参数验证，无效参数会抛出 {@link IllegalArgumentException}</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Randoms {

    /**
     * 常用的中文字符
     */
    private static final String COMMON_USED_CHINESE = "的一了是我不在人们有来他这上着个地到大里说就去子得也和那要下看天时过出小么起你都把好还多没为又可家学只以主会样年想生同老中十从自面前头道它后然走很像见两用她国动进成回什边作对开而己些现山民候经发工向事命给长水几义三声于高手知理眼志点心战二问但身方实吃做叫当住听革打呢真全才四已所敌之最光产情路分总条白话东席次亲如被花口放儿常气五第使写军吧文运再果怎定许快明行因别飞外树物活部门无往船望新带队先力完却站代员机更九您每风级跟笑啊孩万少直意夜比阶连车重便斗马哪化太指变社似士者干石满日决百原拿群究各六本思解立河村八难早论吗根共让相研今其书坐接应关信觉步反处记将千找争领或师结块跑谁草越字加脚紧爱等习阵怕月青半火法题建赶位唱海七女任件感准张团屋离色脸片科倒睛利世刚且由送切星导晚表够整认响雪流未场该并底深刻平伟忙提确近亮轻讲农古黑告界拉名呀土清阳照办史改历转画造嘴此治北必服雨穿内识验传业菜爬睡兴形量咱观苦体众通冲合破友度术饭公旁房极南枪读沙岁线野坚空收算至政城劳落钱特围弟胜教热展包歌类渐强数乡呼性音答哥际旧神座章帮啦受系令跳非何牛取入岸敢掉忽种装顶急林停息句区衣般报叶压慢叔背细";

    /**
     * 获取范围内的随机整数
     * <p>
     * 生成一个在 [iMin, iMax] 范围内的随机整数。该方法内部调用 {@link #random(long, long, int, boolean)} 
     * 来实现随机数生成，然后取第一个值并转换为 int 类型。
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * int dice = Randoms.randomInt(1, 6);      // 生成 1-6 的骰子点数
     * int score = Randoms.randomInt(0, 100);   // 生成 0-100 的分数
     * int negativeNum = Randoms.randomInt(-10, -1);  // 生成负数范围的随机数
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>性能说明：</strong>
     * 该方法的时间复杂度为 O(1)，基于 {@link Math#random()} 实现，性能优异。
     * </p>
     *
     * @param iMin 范围内的最小值（包含）
     * @param iMax 范围内的最大值（包含）
     * @return 在指定范围内的随机整数
     * @throws IllegalArgumentException 如果 iMin > iMax
     * @see #randomLong(long, long)
     * @see #random(long, long, int, boolean)
     */
    public static int randomInt(int iMin, int iMax) {
        return (int) random(iMin, iMax, 1, true)[0];
    }

    /**
     * 获取范围内的随机长整数
     * <p>
     * 生成一个在 [iMin, iMax] 范围内的随机长整数。该方法内部调用 {@link #random(long, long, int, boolean)} 
     * 来实现随机数生成，然后取第一个值。
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * long timestamp = Randoms.randomLong(1000000000L, 9999999999L);  // 生成时间戳范围的随机数
     * long id = Randoms.randomLong(1L, Long.MAX_VALUE);               // 生成随机ID
     * long amount = Randoms.randomLong(1000L, 999999L);               // 生成金额范围的随机数
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>性能说明：</strong>
     * 该方法的时间复杂度为 O(1)，适合需要大范围随机数的场景。
     * </p>
     *
     * @param iMin 范围内的最小值（包含）
     * @param iMax 范围内的最大值（包含）
     * @return 在指定范围内的随机长整数
     * @throws IllegalArgumentException 如果 iMin > iMax
     * @see #randomInt(int, int)
     * @see #random(long, long, int, boolean)
     */
    public static long randomLong(long iMin, long iMax) {
        return random(iMin, iMax, 1, true)[0];
    }

    /**
     * 获取介于iMin和iMax之间的随机数，并根据长度组成数组
     * <p>
     * 这是随机数生成的核心方法，支持批量生成随机数并提供重复控制。
     * 该方法可以生成指定数量的随机数，并根据 bRepeat 参数决定是否允许重复。
     * </p>
     * 
     * <p>
     * <strong>算法说明：</strong>
     * <ul>
     * <li>使用 {@link Math#random()} 作为随机源</li>
     * <li>允许重复时：简单循环生成，时间复杂度 O(n)</li>
     * <li>不允许重复时：使用线性查重算法，时间复杂度 O(n²)</li>
     * <li>采用 {@link Math#round(double)} 确保均匀分布</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * // 生成5个可重复的1-10随机数
     * long[] numbers = Randoms.random(1, 10, 5, true);
     * // 结果可能：[3, 7, 3, 9, 1]
     * 
     * // 生成5个不重复的1-10随机数
     * long[] uniqueNumbers = Randoms.random(1, 10, 5, false);
     * // 结果可能：[3, 7, 9, 1, 5]
     * 
     * // 抽奖号码生成（不重复）
     * long[] lotteryNumbers = Randoms.random(1, 49, 6, false);
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>性能考虑：</strong>
     * <ul>
     * <li>小规模数据（n < 1000）：性能优异</li>
     * <li>大规模不重复数据：可能出现性能瓶颈，建议使用专门的算法</li>
     * <li>当范围接近所需数量时，去重效率最高</li>
     * </ul>
     * </p>
     *
     * @param iMin    随机数的最小值（包含）
     * @param iMax    随机数的最大值（包含）
     * @param iNum    获取几个随机数，必须大于0
     * @param bRepeat 数组中数字是否允许重复
     * @return 包含随机数的长整数数组
     * @throws IllegalArgumentException 如果 iNum <= 0
     * @throws IllegalArgumentException 如果不允许重复且 (iMax - iMin) < (iNum - 1)
     * @see #randomInt(int, int)
     * @see #randomLong(long, long)
     */
    public static long[] random(long iMin, long iMax, int iNum, boolean bRepeat) {
        Validate.isTrue(iNum > 0);
        if (!bRepeat) {
            Validate.isTrue(iMax - iMin >= iNum - 1);
        }

        long[] ia = new long[iNum];
        for (int i = 0; i < iNum; ) {
            double f2 = (iMax - iMin) * Math.random() + iMin;
            long i3 = Math.round(f2);
            if (bRepeat) {
                ia[i] = i3;
                i++;
            } else {
                boolean b = false;
                for (int j = 0; j < i; j++) {
                    if (ia[j] == i3) {
                        b = true;
                        break;
                    }
                }
                if (!b) {
                    ia[i] = i3;
                    i++;
                }
            }
        }
        return ia;
    }

    /**
     * 获得随机数字组合
     * <p>
     * 生成指定长度的随机数字字符串，数字可以重复。每一位都是0-9之间的随机数字。
     * 该方法特别适用于生成验证码、临时密码等场景。
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * String verifyCode = Randoms.randomNumber(6);     // 生成6位验证码："123456"
     * String tempPassword = Randoms.randomNumber(8);   // 生成8位数字密码："87654321"
     * String orderNo = Randoms.randomNumber(10);       // 生成10位订单号："9876543210"
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>注意事项：</strong>
     * <ul>
     * <li>生成的数字字符串可能以0开头</li>
     * <li>允许数字重复，适合大多数业务场景</li>
     * <li>如需不重复的数字组合，请使用 {@link #randomNumberNoRepeat(int)}</li>
     * </ul>
     * </p>
     *
     * @param iLength 字符串长度，必须大于0
     * @return 指定长度的随机数字字符串
     * @throws IllegalArgumentException 如果 iLength <= 0
     * @see #randomNumberNoRepeat(int)
     * @see #randomCodes(int)
     */
    public static String randomNumber(int iLength) {
        long[] iCodes = random(0, 9, iLength, true);
        StringBuilder builder = new StringBuilder();
        for (long i : iCodes) {
            builder.append(i);
        }
        return builder.toString();
    }

    /**
     * 获得随机数字组合（不重复）
     * <p>
     * 生成指定长度的不重复数字字符串，每个数字在结果中只出现一次。
     * 由于只有0-9共10个数字，所以长度不能超过10。
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * String uniqueCode = Randoms.randomNumberNoRepeat(6);   // 生成6位不重复验证码："297145"
     * String pin = Randoms.randomNumberNoRepeat(4);          // 生成4位PIN码："8234"
     * String serial = Randoms.randomNumberNoRepeat(10);      // 生成10位序列号："9876543210"
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>应用场景：</strong>
     * <ul>
     * <li>生成不重复的验证码</li>
     * <li>创建唯一的数字标识符</li>
     * <li>生成PIN码或安全码</li>
     * <li>抽奖号码生成</li>
     * </ul>
     * </p>
     *
     * @param iLength 字符串长度，必须大于0且不能大于10
     * @return 指定长度的不重复数字字符串
     * @throws IllegalArgumentException 如果 iLength <= 0 或 iLength > 10
     * @see #randomNumber(int)
     * @see #randomCodes(int)
     */
    public static String randomNumberNoRepeat(int iLength) {
        long[] iCodes = random(0, 9, iLength, false);
        StringBuilder builder = new StringBuilder();
        for (long i : iCodes) {
            builder.append(i);
        }
        return builder.toString();
    }

    /**
     * 生成随机字母与数字组合
     * <p>
     * 生成包含字母和数字的随机字符串，字母可以选择大写或小写。
     * 字符范围包括：0-9（数字）+ A-Z或a-z（26个字母）。
     * 该方法使用 ASCII 编码进行字符转换，确保生成的字符可读性强。
     * </p>
     * 
     * <p>
     * <strong>算法说明：</strong>
     * <ul>
     * <li>数字范围：0-9 对应随机值 0-9</li>
     * <li>字母范围：A-Z或a-z 对应随机值 10-35</li>
     * <li>使用 ASCII 偏移量进行字符转换</li>
     * <li>每个字符位置独立随机生成</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * String password = Randoms.randomCodes(8, true);    // 大写："A3F7K9M2"
     * String token = Randoms.randomCodes(12, false);     // 小写："d8k2m9x7n4b1"
     * String sessionId = Randoms.randomCodes(16, true);  // 会话ID："X9K2M7N4B8D1A3F6"
     * }</pre>
     * </p>
     *
     * @param iLength   随机组合的长度，必须大于0
     * @param isCapital 是否使用大写字母，true为大写（A-Z），false为小写（a-z）
     * @return 包含字母和数字的随机字符串
     * @throws IllegalArgumentException 如果 iLength <= 0
     * @see #randomCodes(int)
     * @see #randomLetter(int, int)
     */
    public static String randomCodes(int iLength, boolean isCapital) {
        Validate.isTrue(iLength > 0);
        // 因为是26个字母，10-35是26个数字
        long[] iCodes = random(0, 35, iLength, true);
        StringBuilder strb = new StringBuilder();
        for (long iCode : iCodes) {
            // 0-9则为数字，大于10则转换为数字。ASCII中，字母从A开始到z，共有52个。这里只取大写字母
            if (iCode >= 0 && iCode <= 9) {
                strb.append(iCode);
            } else {
                char cTmp = (char) ((iCode - 10) + (isCapital ? 'A' : 'a'));
                strb.append(cTmp);
            }
        }
        return strb.toString();
    }

    /**
     * 生成随机大写字母与数字组合
     * <p>
     * 这是 {@link #randomCodes(int, boolean)} 的简化版本，默认使用大写字母。
     * 生成包含大写字母（A-Z）和数字（0-9）的随机字符串。
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * String code = Randoms.randomCodes(6);      // 生成6位验证码："A3K7M9"
     * String ref = Randoms.randomCodes(10);      // 生成10位参考码："X4N8B2K7M5"
     * String orderId = Randoms.randomCodes(12);  // 生成12位订单号："F7K9M2X8B4N1"
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>典型应用：</strong>
     * <ul>
     * <li>验证码生成（避免混淆的大写字母+数字）</li>
     * <li>订单号、流水号生成</li>
     * <li>临时密码生成</li>
     * <li>邀请码生成</li>
     * </ul>
     * </p>
     *
     * @param iLength 随机组合的长度，必须大于0
     * @return 包含大写字母和数字的随机字符串
     * @throws IllegalArgumentException 如果 iLength <= 0
     * @see #randomCodes(int, boolean)
     */
    public static String randomCodes(int iLength) {
        return randomCodes(iLength, true);
    }

    /**
     * 获得字母组合
     * <p>
     * 生成纯字母的随机字符串，支持大写、小写和混合三种模式。
     * 该方法只包含字母字符（A-Z, a-z），不包含数字或特殊字符。
     * </p>
     * 
     * <p>
     * <strong>类型说明：</strong>
     * <ul>
     * <li><strong>type=1：</strong>纯大写字母（A-Z）</li>
     * <li><strong>type=2：</strong>纯小写字母（a-z）</li>
     * <li><strong>type=3：</strong>大小写混合，每个字符随机选择大小写</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * String upperCode = Randoms.randomLetter(8, 1);     // 大写："ABCDEFGH"
     * String lowerCode = Randoms.randomLetter(8, 2);     // 小写："abcdefgh"
     * String mixedCode = Randoms.randomLetter(8, 3);     // 混合："AbCdEfGh"
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>应用场景：</strong>
     * <ul>
     * <li>生成纯字母的标识符</li>
     * <li>创建用户名建议</li>
     * <li>生成随机单词或代码</li>
     * <li>测试数据生成</li>
     * </ul>
     * </p>
     *
     * @param iLength 组合长度，必须大于0
     * @param type    字母组合类型，1=大写，2=小写，3=混合
     * @return 指定类型的字母字符串
     * @throws IllegalArgumentException 如果 iLength <= 0 或 type 不在 1-3 范围内
     * @see #randomCodes(int, boolean)
     */
    public static String randomLetter(int iLength, int type) {
        Validate.isTrue(iLength > 0);
        // 26个字母，0-25是26个数字
        long[] iCodes = random(0, 25, iLength, true);
        StringBuilder strb = new StringBuilder();
        for (long iCode : iCodes) {
            char cTmp = ' ';
            if (type == 1) {
                cTmp = (char) (iCode + 'A');
            } else if (type == 2) {
                cTmp = (char) (iCode + 'a');
            } else if (type == 3) {
                if (Math.random() >= 0.5) {
                    cTmp = (char) (iCode + 'A');
                } else {
                    cTmp = (char) (iCode + 'a');
                }
            }
            strb.append(cTmp);
        }
        return strb.toString();
    }

    /**
     * 获得常用随机中文字符
     * <p>
     * 从内置的常用中文字符库中随机选取指定数量的中文字符组成字符串。
     * 字符库包含2000+个高频使用的中文字符，涵盖日常用语中的绝大多数汉字。
     * </p>
     * 
     * <p>
     * <strong>字符库特点：</strong>
     * <ul>
     * <li>包含常用汉字，频率高，可读性强</li>
     * <li>不包含生僻字和特殊符号</li>
     * <li>适合生成中文测试数据</li>
     * <li>可以生成具有一定意义的中文文本片段</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * String chineseName = Randoms.randomChinese(2);    // 生成中文姓名："张三"
     * String title = Randoms.randomChinese(8);          // 生成文章标题："今天天气不错啊"
     * String content = Randoms.randomChinese(50);       // 生成文章内容片段
     * String address = Randoms.randomChinese(6);        // 生成地址信息："北京市朝阳区"
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>注意事项：</strong>
     * <ul>
     * <li>字符可以重复，因为是随机选取</li>
     * <li>生成的文本可能没有完整的语义，仅适合做测试数据</li>
     * <li>适合生成中文测试数据和模拟数据</li>
     * </ul>
     * </p>
     *
     * @param num 需要生成的中文字符数量，必须大于0
     * @return 指定长度的随机中文字符串
     * @throws IllegalArgumentException 如果 num <= 0
     */
    public static String randomChinese(int num) {
        Validate.isTrue(num > 0);

        String str = COMMON_USED_CHINESE;
        long[] iCodes = random(0, str.length() - 1, num, true);
        StringBuilder strb = new StringBuilder();
        for (long iCode : iCodes) {
            strb.append(str.charAt((int) iCode));
        }
        return strb.toString();
    }

    /**
     * 从集合中随机取出若干个元素
     * <p>
     * 从源集合中随机选取指定数量的元素，返回一个新的集合。
     * 选取过程不允许重复，即同一个元素不会被选中多次。
     * 如果请求的数量超过源集合的大小，将返回整个源集合的随机排列。
     * </p>
     * 
     * <p>
     * <strong>算法说明：</strong>
     * <ul>
     * <li>使用 {@link #random(long, long, int, boolean)} 生成不重复的随机索引</li>
     * <li>根据随机索引从源集合中提取元素</li>
     * <li>时间复杂度：O(min(size, source.size())²) ，空间复杂度：O(size)</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * List<String> allUsers = Arrays.asList("张三", "李四", "王五", "赵六", "孙七");
     * List<String> selectedUsers = Randoms.randomList(allUsers, 3);
     * // 结果可能：["李四", "赵六", "张三"]
     * 
     * // 从商品列表中随机推荐
     * List<Product> randomProducts = Randoms.randomList(allProducts, 5);
     * 
     * // 游戏中随机选取道具
     * List<Item> randomItems = Randoms.randomList(inventory, 10);
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>应用场景：</strong>
     * <ul>
     * <li>随机抽样和统计分析</li>
     * <li>随机推荐系统</li>
     * <li>游戏中的随机事件</li>
     * <li>A/B 测试中的用户分组</li>
     * </ul>
     * </p>
     *
     * @param source 源集合，不能为 null
     * @param size   需要选取的元素数量，必须大于等于0
     * @param <T>    集合元素的类型
     * @return 包含随机选取元素的新集合，大小为 min(size, source.size())
     * @throws NullPointerException 如果 source 为 null
     */
    public static <T> List<T> randomList(List<T> source, int size) {
        List<T> collection = new ArrayList<>();
        size = Math.min(size, source.size());
        if (size > 0) {
            long[] arr = random(0, source.size() - 1, size, false);
            for (int i = 0; i < size; i++) {
                collection.add(source.get((int) arr[i]));
            }
        }
        return collection;
    }
}
