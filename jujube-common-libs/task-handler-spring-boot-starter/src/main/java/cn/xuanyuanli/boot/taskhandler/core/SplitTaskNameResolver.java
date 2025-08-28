package cn.xuanyuanli.boot.taskhandler.core;


import cn.xuanyuanli.core.util.Texts;

/**
 * @author John Li
 */
public class SplitTaskNameResolver extends TaskNameResolver {

    private final String taskParamSeparator;
    private final String[] taskArr;

    public SplitTaskNameResolver(String body, String taskNameSeparator, String taskParamSeparator) {
        super(body);
        this.taskParamSeparator = taskParamSeparator;
        this.taskArr = body.split(Texts.escapeExprSpecialWord(taskNameSeparator));
    }

    @Override
    public String getTaskName() {
        return taskArr[0];
    }

    @Override
    public String[] getTaskParam() {
        if (taskArr.length > 1) {
            // 当参数分隔符为空（或全空白）时，不进行字符级拆分，直接返回整个参数片段
            if (taskParamSeparator == null || taskParamSeparator.isEmpty()) {
                return new String[]{taskArr[1]};
            }
            return taskArr[1].split(Texts.escapeExprSpecialWord(taskParamSeparator));
        }
        return new String[]{};
    }
}
