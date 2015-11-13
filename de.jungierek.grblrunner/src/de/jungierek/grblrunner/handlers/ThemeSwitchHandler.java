package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;

public class ThemeSwitchHandler {
    private static final String DEFAULT_THEME = "de.jungierek.grblrunner.theme.default";
    private static final String RAINBOW_THEME = "de.jungierek.grblrunner.theme.rainbow";
    private static final String DARK_THEME = "org.eclipse.e4.ui.css.theme.e4_dark";
    private static final String WIN_THEME = "org.eclipse.e4.ui.css.theme.e4_default";
    private static final String CLASSIC_THEME = "org.eclipse.e4.ui.css.theme.e4_classic";

    @SuppressWarnings("restriction")
    @Execute
    public void switchTheme ( IThemeEngine engine ) {
        String theme = engine.getActiveTheme ().getId ();
        System.out.println ( "theme=" + theme );
        
        if ( theme.startsWith ( DEFAULT_THEME ) ) {
            engine.setTheme ( RAINBOW_THEME, true );
        }
        else if ( theme.startsWith ( RAINBOW_THEME ) ) {
            engine.setTheme ( DARK_THEME, true );
        }
        else if ( theme.startsWith ( DARK_THEME ) ) {
            engine.setTheme ( WIN_THEME, true );
        }
        else if ( theme.startsWith ( WIN_THEME ) ) {
            engine.setTheme ( CLASSIC_THEME, true );
        }
        else {
            engine.setTheme ( DEFAULT_THEME, true );
        }
        String theme1 = engine.getActiveTheme ().getId ();
        System.out.println ( "new theme=" + theme1 );
    }
}