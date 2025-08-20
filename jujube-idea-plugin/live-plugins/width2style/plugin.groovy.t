package width2style

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.SelectionModel

import static liveplugin.PluginUtil.*

// One of the most fundamental things you can do in an Action is to modify text
// in the current editor using `com.intellij.openapi.editor.Document` API.
// See also https://plugins.jetbrains.com/docs/intellij/documents.html.

registerAction("width2style","ctrl alt Q", "EditorPopupMenu","width2style") { AnActionEvent event ->
    def project = event?.project
    def editor = CommonDataKeys.EDITOR.getData(event.dataContext)
    if (project == null || editor == null) return

    // 获取选中的文本
    SelectionModel selectionModel = editor.getSelectionModel();
    String selectedText = selectionModel.getSelectedText();

    String searchText = 'width="([^"]*)"'
    String replacementText = 'style="width:$1;"'
    String replacedText = selectedText.replaceAll(searchText,replacementText)
    if (replacementText == selectedText){
        searchText = 'width=\'([^"]*)\''
        replacementText = 'style=\'width:$1;\''
        replacedText = selectedText.replaceAll(searchText,replacementText)
    }

    int start = selectionModel.getSelectionStart();
    int end = selectionModel.getSelectionEnd();

    // Document modifications must be done inside "commands" which will support undo/redo functionality.
    runDocumentWriteAction(event.project, editor.document, "width2style") { document ->
        // 用替换后的文本更新选中的内容
        document.replaceString(start, end, replacedText);
    }

    // This is related to the topic of threading rules:
    //  - it's ok to read data with a read lock or on event dispatch thread (aka EDT or UI thread)
    //  - it's ok to modify data with a write lock and only on EDT

    // In practice, it's not very complicated because actions run on EDT and can read any data.
    // So the only thing to remember is to use commands for modifications.

    // See also https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html
    // and javadoc for the Application class https://upsource.jetbrains.com/idea-ce/file/idea-ce-73bd72cb4bb9b64d0b1f44c3f6f22246e2850921/platform/core-api/src/com/intellij/openapi/application/Application.java
    // Note that IntelliJ documentation is talking about read/write "actions" which is
    // an overloaded term and is not directly related to AnAction class.
}

//
// See next popup-menu example.
//          ^^^^^^^^^^
