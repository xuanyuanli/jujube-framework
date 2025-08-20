package cn.xuanyuanli.ideaplugin;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author John Li
 */
public class ConsoleToolWindow implements ToolWindowFactory {

    public static final Key<ConsoleView> MY_CONSOLE_VIEW_KEY = new Key<>("MyConsoleView");

    public static Project PROJECT = null;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PROJECT = project;
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        // 将 ConsoleView 实例保存到项目用户数据中
        project.putUserData(MY_CONSOLE_VIEW_KEY, consoleView);
        // 输出日志到控制台
        consoleView.print("Hello, this is my console!\n", ConsoleViewContentType.NORMAL_OUTPUT);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(consoleView.getComponent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
