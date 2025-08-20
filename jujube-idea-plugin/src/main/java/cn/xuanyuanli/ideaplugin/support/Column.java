package cn.xuanyuanli.ideaplugin.support;

import com.intellij.psi.PsiType;

/**
 * @author John Li
 */
public class Column {

    private PsiType psiType;
    private String field;


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public PsiType getPsiType() {
        return psiType;
    }

    public void setPsiType(PsiType psiType) {
        this.psiType = psiType;
    }
}
