package cn.xuanyuanli.ideaplugin.support;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import cn.xuanyuanli.ideaplugin.ConsoleToolWindow;
import cn.xuanyuanli.core.util.Texts;

/**
 * 控制台日志输出工具
 *
 * @author John Li
 */
public class Consoles {

    public static void info(String msg, Object... params) {
        Project project = ConsoleToolWindow.PROJECT;
        if (project == null) {
            return;
        }
        ConsoleView consoleView = project.getUserData(ConsoleToolWindow.MY_CONSOLE_VIEW_KEY);
        if (consoleView != null) {
            consoleView.print(Texts.format(msg + "\n", params), ConsoleViewContentType.NORMAL_OUTPUT);
        }
    }
}
