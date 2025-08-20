package cn.xuanyuanli.ideaplugin;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * Message bundle for Jujube plugin internationalization
 * 
 * @author John Li
 */
public final class JujubeBundle extends AbstractBundle {
    
    private static final String BUNDLE = "messages";
    private static final JujubeBundle INSTANCE = new JujubeBundle();

    private JujubeBundle() {
        super(BUNDLE);
    }

    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    @NotNull
    public static String getText(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key) {
        return INSTANCE.getMessage(key);
    }
}