package cn.xuanyuanli.ideaplugin.provider;

import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.SqlClassSearch;

/**
 * @author John Li
 */
public class GotoSqlRelatedProvider extends GotoRelatedProvider {

    @Override
    public @NotNull List<? extends GotoRelatedItem> getItems(@NotNull PsiElement element) {
        PsiElement target = SqlClassSearch.getSqlElementFromJavaMethodOrClass(element);
        if (target != null) {
            ArrayList<GotoRelatedItem> gotoRelatedItems = new ArrayList<>();
            gotoRelatedItems.add(new GotoRelatedItem(target, "SQL File"));
            return gotoRelatedItems;
        }
        return super.getItems(element);
    }
}
