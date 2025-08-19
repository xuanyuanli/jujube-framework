package cn.xuanyuanli.core.util;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

/**
 * 随机数生成工具
 *
 * @author John Li
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
     *
     * @param iMin 范围内的最小值
     * @param iMax 范围内的最大值
     * @return int
     */
    public static int randomInt(int iMin, int iMax) {
        return (int) random(iMin, iMax, 1, true)[0];
    }

    /**
     * 获取范围内的随机整数
     *
     * @param iMin 范围内的最小值
     * @param iMax 范围内的最大值
     * @return long
     */
    public static long randomLong(long iMin, long iMax) {
        return random(iMin, iMax, 1, true)[0];
    }

    /**
     * 获取介于iMin和iMax之间的随机数，并根据长度组成数组
     *
     * @param iMin    随机数的最小值
     * @param iMax    随机数的最大值
     * @param iNum    获取几个随机数
     * @param bRepeat 数组中数字是否允许重复
     * @return {@link long[]}
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
     *
     * @param iLength 长度
     * @return {@link String}
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
     * 获得随机数字组合(不重复)
     *
     * @param iLength 长度。不能大于10
     * @return {@link String}
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
     *
     * @param iLength   随机组合的长度
     * @param isCapital 是否是大写字母
     * @return {@link String}
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
     *
     * @param iLength 随机组合的长度
     * @return {@link String}
     */
    public static String randomCodes(int iLength) {
        return randomCodes(iLength, true);
    }

    /**
     * 获得字母组合
     *
     * @param iLength 组合长度
     * @param type    字母组合类型。1：大写，2：小写，3：混合
     * @return {@link String}
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
     *
     * @param num 几个字符
     * @return {@link String}
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
     *
     * @param source 源
     * @param size   大小
     * @return {@link List}<{@link T}>
     * @param <T> 泛型
     */
    public static <T> List<T> randomList(List<T> source, int size) {
        List<T> collection = Lists.newArrayList();
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
