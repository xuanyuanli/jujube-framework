package cn.xuanyuanli.ideaplugin.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import java.util.ArrayList;
import java.util.List;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import cn.xuanyuanli.ideaplugin.JujubeBundle;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElementFactory;
import org.intellij.plugins.markdown.lang.psi.MarkdownRecursiveElementVisitor;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeader;
import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.support.Consoles;

/**
 * @author John Li
 */
public class OptimizeMarkdownTocAction extends AnAction {

    public static class TocEntry {

        int level;
        String title;
        String newTitle;
        MarkdownHeader header;
        List<TocEntry> childToc;

        public TocEntry(int level, String title, MarkdownHeader header) {
            this.level = level;
            this.title = title;
            this.header = header;
            this.childToc = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "TocEntry{" + "level=" + level + ", title='" + title + '\'' + ", newTitle='" + newTitle + '\'' + '}';
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null || virtualFile.getFileType() != MarkdownFileType.INSTANCE) {
            return;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (!(psiFile instanceof MarkdownFile markdownFile)) {
            return;
        }

        Document document = editor.getDocument();
        // 1. 获取目录结构
        List<TocEntry> toc = getToc(markdownFile, document);

        // 2. 计算新编号
        normalizeTocEntries(toc);

        // 3. 替换旧编号并更新文档
        WriteCommandAction.runWriteCommandAction(project, () -> updateDocument(toc, project));
    }

    private void updateDocument(List<TocEntry> tocEntries, Project project) {
        for (TocEntry entry : tocEntries) {
            if (entry.newTitle != null) {
                Consoles.info("+".repeat(entry.level) + "\t" + entry);
                entry.header.replace(MarkdownPsiElementFactory.createHeader(project, entry.newTitle, entry.level));
            }
            if (entry.childToc != null) {
                updateDocument(entry.childToc, project);
            }
        }
    }

    static void normalizeTocEntries(List<TocEntry> tocEntries) {
        normalizeTocEntries(tocEntries, new int[6], 0);
    }

    private List<TocEntry> getToc(MarkdownFile markdownFile, Document ignoredDocument) {
        List<TocEntry> tocEntries = new ArrayList<>();
        MarkdownRecursiveElementVisitor visitor = new MarkdownRecursiveElementVisitor() {
            // 上一个处理的 TocEntry
            TocEntry lastEntry = null;

            @Override
            public void visitHeader(@NotNull MarkdownHeader header) {
                super.visitHeader(header);

                int level = header.getLevel();
                String title = header.getName();

                // 创建新的 TocEntry
                TocEntry currentEntry = new TocEntry(level, title, header);
                // 构建层级关系
                if (lastEntry == null) {
                    // 第一个标题
                    tocEntries.add(currentEntry);
                } else {
                    if (currentEntry.level > lastEntry.level) {
                        // 作为子标题
                        lastEntry.childToc.add(currentEntry);
                    } else {
                        // 找到 currentEntry 的父级
                        TocEntry parent = findParent(tocEntries, currentEntry);
                        if (parent != null) {
                            parent.childToc.add(currentEntry);
                        } else {
                            // 没有父级，添加到顶层
                            tocEntries.add(currentEntry);
                        }
                    }
                }
                lastEntry = currentEntry;
            }

            // 辅助方法：找到父级 TocEntry
            private TocEntry findParent(List<TocEntry> tocList, TocEntry entry) {
                for (int i = tocList.size() - 1; i >= 0; i--) {
                    TocEntry potentialParent = tocList.get(i);
                    if (entry.level > potentialParent.level) {
                        return findParentInChild(potentialParent, entry);
                    }
                }
                return null;
            }

            private TocEntry findParentInChild(TocEntry parent, TocEntry entry) {
                if (parent.level < entry.level) {
                    if (parent.childToc.isEmpty()) {
                        return parent;
                    }
                    for (int i = parent.childToc.size() - 1; i >= 0; i--) {
                        TocEntry child = parent.childToc.get(i);
                        if (child.level < entry.level) {
                            return findParentInChild(child, entry);
                        }
                    }
                }
                return parent;
            }
        };
        markdownFile.accept(visitor);
        return tocEntries;
    }

    static void normalizeTocEntries(List<TocEntry> entries, int[] counters, int depth) {
        if (entries == null || entries.isEmpty()) {
            return;
        }

        // 重置当前级别及更低级别的计数器
        for (int i = depth; i < counters.length; i++) {
            counters[i] = 0;
        }

        // 遍历当前级别的条目
        for (TocEntry entry : entries) {
            // 增加当前级别的计数器
            counters[depth]++;
            String newTitle = generateNewTitle(counters, depth, entry.title);
            if (!newTitle.equals(entry.title)) {
                entry.newTitle = newTitle;
            }

            // 递归处理子条目
            normalizeTocEntries(entry.childToc, counters, depth + 1);
        }
    }

    private static String generateNewTitle(int[] counters, int depth, String originalTitle) {
        StringBuilder sb = new StringBuilder();
        switch (depth) {
            // 二级标题
            case 0:
                sb.append(toChineseNumber(counters[depth])).append("、");
                break;
            // 三级标题
            case 1:
                sb.append(counters[depth]).append("、");
                break;
            // 四级标题
            case 2:
                sb.append(counters[depth - 1]).append(".").append(counters[depth]).append("、");
                break;
            // 五级标题
            case 3:
                sb.append((char) ('a' + counters[depth] - 1)).append("、");
                break;
            default:
                // 对于更深层次的标题，可以自定义逻辑
                break;
        }

        // 移除原标题中可能存在的旧编号
        String cleanTitle = originalTitle.replaceFirst("^\\s*([\\d.]+|[a-zA-Z]+|[IVXLCDM]+|[一二三四五六七八九十]+)[、.\\s]\\s*", "").trim();

        sb.append(cleanTitle);
        return sb.toString();
    }

    // 将阿拉伯数字转换为中文数字
    private static String toChineseNumber(int num) {
        String[] chineseNums = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        if (num <= 10) {
            return chineseNums[num - 1];
        } else if (num < 20) {
            return "十" + chineseNums[num - 11];
        } else if (num % 10 == 0) {
            return chineseNums[num / 10 - 1] + "十";
        } else {
            return chineseNums[num / 10 - 1] + "十" + chineseNums[num % 10 - 1];
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // ... (与之前相同的部分) ...
        e.getPresentation().setText(JujubeBundle.getText("action.optimize.md.toc"));
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

        boolean isMarkdownFile = project != null && editor != null && virtualFile != null && virtualFile.getFileType() == MarkdownFileType.INSTANCE;
        e.getPresentation().setEnabledAndVisible(isMarkdownFile);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
