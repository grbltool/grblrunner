package de.jungierek.grblrunner.part;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPersistenceKey;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.gcode.IGrblRequest;
import de.jungierek.grblrunner.service.gcode.IGrblResponse;
import de.jungierek.grblrunner.tool.Toolbox;

@SuppressWarnings("restriction")
public class TerminalPart {

    private static final Logger LOG = LoggerFactory.getLogger ( TerminalPart.class );

    @Inject
    private IGcodeService gcodeService;

    @Inject
    private Display display;

    @Inject
    private Toolbox toolbox;

    private StyledText terminalText;

    private boolean showSuppressedLines = false;
    private boolean showGrblStateLines = true;
    private boolean showGcodeModeLines = true;

    // set from preferences
    private Font terminalFont;
    private Color terminalForegroundColor;
    private Color terminalBackgroundColor;
    private Color alarmForegroundColor;
    private Color alarmBackgroundColor;
    private Color timestampBackgroundColor;
    private Color suppressedLineForegroundColor;
    private Color okForegroundColor;
    private Color errorForegroundColor;
    private Color grblForegroundColor;
    private Color grblBackgroundColor;
    private Color suppressedOkForegroundColor;
    private Color supppressedErrorForegroundColor;
    private Color msgForegroundColor;
    private Color msgBackgroundColor;

    @Inject
    public void setTerminalFontData ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.TERMINAL_FONT_DATA) String fontDataString ) {
    
        LOG.debug ( "setFontData: fontDataString=" + fontDataString );
    
        terminalFont = new Font ( display, new FontData ( fontDataString ) );
        if ( terminalText != null && !terminalText.isDisposed () ) {
            terminalText.setFont ( terminalFont );
            LOG.info ( "setFontData: fontdata=" + fontDataString );
        }
    
    }

    @Inject
    public void setTerminalForegroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_TERMINAL_FOREGROUND) String rgbText ) {

        terminalForegroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        if ( terminalText != null && !terminalText.isDisposed () ) terminalText.setForeground ( terminalForegroundColor );

    }

    @Inject
    public void setTerminalBackgroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_TERMINAL_BACKGROUND) String rgbText ) {

        terminalBackgroundColor= new Color ( display, StringConverter.asRGB ( rgbText ) );
        if ( terminalText != null && !terminalText.isDisposed () ) terminalText.setBackground ( terminalBackgroundColor );

    }

    @Inject
    public void setAlarmForegroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_ALARM_FOREGROUND) String rgbText ) {

        alarmForegroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setAlarmBackgroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_ALARM_BACKGROUND) String rgbText ) {

        alarmBackgroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setTimestampBackgroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_TIMESTAMP_BACKGROUND) String rgbText ) {

        timestampBackgroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setSuppressedLineForegroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_SUPPRESSED_LINE_FOREGROUND) String rgbText ) {

        suppressedLineForegroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setOkForegroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_OK_FOREGROUND) String rgbText ) {

        okForegroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setErrorForegroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_ERROR_FOREGROUND) String rgbText ) {

        errorForegroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setGrblForegroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_GRBL_FOREGROUND) String rgbText ) {

        grblForegroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setGrblBackgroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_GRBL_BACKGROUND) String rgbText ) {

        grblBackgroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setSuppressedOkForegroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_OK_SUPPRESSED_FOREGROUND) String rgbText ) {

        suppressedOkForegroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setSuppressedErrorForegroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_ERROR_SUPPRESSED_FOREGROUND) String rgbText ) {

        supppressedErrorForegroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setMsgForegroundColor ( Display display, @Preference ( nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_MSG_FOREGROUND) String rgbText ) {

        msgForegroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @Inject
    public void setMsgBackgroundColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_MSG_BACKGROUND) String rgbText ) {

        msgBackgroundColor = new Color ( display, StringConverter.asRGB ( rgbText ) );

    }

    @PostConstruct
    public void createGui ( Composite parent, MPart part, IEclipseContext context, MApplication application ) {

        LOG.debug ( "createGui:" );

        terminalText = new StyledText ( parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
        terminalText.setEditable ( false );
        terminalText.setForeground ( terminalForegroundColor );
        terminalText.setBackground ( terminalBackgroundColor );
        terminalText.setFont ( terminalFont );

        restorePersistedState ( application, part );

    }

    private void scrollToEnd () {

        terminalText.setTopIndex ( terminalText.getLineCount () - 1 );

    }

    private void appendText ( String line, final Color foreground, final Color background, final int textStyle ) {
    
        int start = terminalText.getCharCount ();
        terminalText.append ( line );
        int len = terminalText.getCharCount () - start;
        terminalText.setStyleRange ( new StyleRange ( start, len, foreground, background, textStyle ) );

        scrollToEnd ();

    }

    private void appendText ( String line, final Color foreground, final Color background ) {
    
        appendText ( line, foreground, background, SWT.NONE );
    
    }

    private void appendText ( String line, final int textStyle ) {
    
        appendText ( line, null, null, textStyle );
    
    }

    private void restorePersistedState ( MApplication application, MPart part ) {

        final Map<String, String> persistedState = application.getPersistedState ();
        showSuppressedLines = toolbox.parseBoolean ( persistedState.get ( IPersistenceKey.TERMINAL_SUPPRESS_LINES ) );
        showGrblStateLines = toolbox.parseBoolean ( persistedState.get ( IPersistenceKey.TERMINAL_GRBL_STATE ) );
        showGcodeModeLines = toolbox.parseBoolean ( persistedState.get ( IPersistenceKey.TERMINAL_GRBL_MODES ) );

        // set the state of the direct menu items according to persisted state
        // find the two direct menu items
        final MToolBar toolbar = part.getToolbar ();
        List<MToolBarElement> toolBarChildren = toolbar.getChildren ();
        for ( MToolBarElement child : toolBarChildren ) {
            if ( child instanceof MHandledToolItem && child.getElementId ().equals ( "de.jungierek.grblrunner.handledtoolitem.terminal.togglesuppresslines" ) ) {
                LOG.debug ( "restorePersistedState: child=" + child.getElementId () + " class=" + child.getClass () );
                MMenu menu = ((MHandledToolItem) child).getMenu ();
                if ( menu != null ) {
                    List<MMenuElement> items = menu.getChildren ();
                    for ( MMenuElement item : items ) {
                        LOG.debug ( "restorePersistedState: item=" + item.getElementId () + "class=" + child.getClass () );
                        switch ( item.getElementId () ) {
                            case "de.jungierek.grblrunner.directmenuitem.togglesuppresslines.grblstate":
                                ((MMenuItem) item).setSelected ( showGrblStateLines );
                                break;
                            case "de.jungierek.grblrunner.directmenuitem.togglesuppresslines.gcodestate":
                                ((MMenuItem) item).setSelected ( showGcodeModeLines );
                                break;
                            default:
                                break;
                        }
                    }
                }

            }
        }


    }

    @PersistState
    public void savePersistState ( MApplication application ) {
        
        LOG.debug ( "persistState:" );

        final Map<String, String> persistedState = application.getPersistedState ();

        persistedState.put ( IPersistenceKey.TERMINAL_SUPPRESS_LINES, "" + showSuppressedLines );
        persistedState.put ( IPersistenceKey.TERMINAL_GRBL_STATE, "" + showGrblStateLines );
        persistedState.put ( IPersistenceKey.TERMINAL_GRBL_MODES, "" + showGcodeModeLines );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvent.PLAYER_START) String timestamp ) {

        LOG.trace ( "playerStartNotified: timestamp=" + timestamp );

        appendText ( "Gcode Player started at " + timestamp + "\n", null, timestampBackgroundColor );
    
    }

    @Inject
    @Optional
    public void playerLineSegmentNotified ( @UIEventTopic(IEvent.PLAYER_SEGMENT) String gcodeSegment ) {

        LOG.trace ( "playerLineNotified: gcodeSegment=" + gcodeSegment );

        terminalText.append ( "- " + gcodeSegment + "\n" );
        scrollToEnd ();

    }

    @Inject
    @Optional
    public void playerLineNotified ( @UIEventTopic(IEvent.PLAYER_LINE) IGcodeLine gcodeLine ) {
    
        LOG.trace ( "playerLineNotified: gcodeLine=" + gcodeLine );

        if ( gcodeLine == null ) return;

        terminalText.append ( gcodeLine.getLine () + "\n" );
        scrollToEnd ();
    
    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvent.PLAYER_STOP) String timestamp ) {

        LOG.trace ( "playerStopNotified: timestamp=" + timestamp );

        appendText ( "Gcode Player stopped at " + timestamp + "\n", null, timestampBackgroundColor );
        terminalText.append ( "-------------------------------------------------------------------------------------\n" );
        scrollToEnd ();

    }

    @Inject
    @Optional
    public void sentNotified ( @UIEventTopic(IEvent.GRBL_SENT) IGrblRequest command, EModelService modelService, MApplication application ) {

        LOG.trace ( "sentNotified: command=" + command );

        if ( command == null ) {
            LOG.warn ( "receivedNotified: response == null" );
            return;
        }

        if ( command.isReset () ) return;

        String line = command.getMessage ();

        if ( !command.isSuppressInTerminal () ) {
            appendText ( line, SWT.BOLD );
        }
        else if ( showSuppressedLines ) {

            boolean show = true;

            if ( line.startsWith ( "$G" ) ) show = showGcodeModeLines;

            if ( show ) appendText ( line, suppressedLineForegroundColor, null, SWT.BOLD );

        }

    }
    
    private boolean ignoreNextOk = false;

    @Inject
    @Optional
    public void receivedNotified ( @UIEventTopic(IEvent.GRBL_RECEIVED) IGrblResponse response ) {

        LOG.trace ( "receivedNotified: response=" + response );

        if ( response == null || response.getMessage () == null ) return;

        String line = response.getMessage ();

        if ( !response.isSuppressInTerminal () ) {
            if ( line.startsWith ( "ok" ) ) {
                appendText ( line, okForegroundColor, null, SWT.BOLD );
            }
            else if ( line.startsWith ( "error:" ) ) {
                appendText ( line, errorForegroundColor, null, SWT.BOLD );
            }
            else if ( line.startsWith ( "ALARM" ) ) {
                appendText ( line, alarmForegroundColor, alarmBackgroundColor, SWT.BOLD );
            }
            else if ( line.startsWith ( "Grbl" ) ) {
                appendText ( line, grblForegroundColor, grblBackgroundColor );
            }
            else if ( line.startsWith ( "[MSG:" ) ) {
                appendText ( line, msgForegroundColor, msgBackgroundColor, SWT.BOLD );
            }
            else {
                terminalText.append ( line );
                scrollToEnd ();
            }
        }
        else if ( showSuppressedLines ) {
            if ( line.startsWith ( "ok" ) ) {
                if ( ignoreNextOk ) ignoreNextOk = false;
                else appendText ( line, suppressedOkForegroundColor, null, SWT.BOLD );
            }
            else if ( line.startsWith ( "error:" ) ) {
                appendText ( line, supppressedErrorForegroundColor, null, SWT.BOLD );
            }
            else {

                boolean show = true;

                if ( line.startsWith ( "<" ) ) show = showGrblStateLines;
                else if ( line.startsWith ( "[G54" ) ) {} // do nothing
                else if ( line.startsWith ( "[G55" ) ) {} // do nothing
                else if ( line.startsWith ( "[G56" ) ) {} // do nothing
                else if ( line.startsWith ( "[G57" ) ) {} // do nothing
                else if ( line.startsWith ( "[G58" ) ) {} // do nothing
                else if ( line.startsWith ( "[G59" ) ) {} // do nothing
                else if ( line.startsWith ( "[G28" ) ) {} // do nothing
                else if ( line.startsWith ( "[G30" ) ) {} // do nothing
                else if ( line.startsWith ( "[G92" ) ) {} // do nothing
                else if ( line.startsWith ( "[GC:" ) ) {
                    show = showGcodeModeLines;
                    if ( !show ) ignoreNextOk = true;
                }

                if ( show ) appendText ( line, suppressedLineForegroundColor, null, SWT.BOLD );

            }
        }
        else if ( line.startsWith ( "error:" ) ) {
            // show eeror line ever
            appendText ( line, supppressedErrorForegroundColor, null, SWT.NONE );
        }

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvent.AUTOLEVEL_START) String timestamp ) {

        LOG.trace ( "scanStartNotified:" );

        appendText ( "Probe Scanning started at " + timestamp + "\n", null, timestampBackgroundColor );

    }

    @Inject
    @Optional
    public void updateProbeNotified ( @UIEventTopic(IEvent.AUTOLEVEL_UPDATE) IGcodePoint probe ) {

        LOG.trace ( "updateProbeNotified: probe=" + probe );

        // convert from machine to work coordinate system
        final IGcodePoint p = probe.sub ( gcodeService.getFixtureShift () );
        terminalText.append ( "" + probe + "    delta=" + String.format ( IGcodePoint.FORMAT_COORDINATE, p.getZ () ) + "\n" );

        scrollToEnd ();

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvent.AUTOLEVEL_STOP) String timestamp ) {

        LOG.trace ( "scanStopNotified:" );

        appendText ( "Probe Scanning stopped at " + timestamp + "\n", null, timestampBackgroundColor );

    }

    // delegate routines for command handlers

    public void toggleSuppressLines () {

        LOG.debug ( "toggleSuppressLines:" );
        showSuppressedLines = !showSuppressedLines;

    }

    public void clearText () {

        LOG.debug ( "clearText:" );
        terminalText.setText ( "" );

    }

    public void setShowGrblState ( boolean show ) {

        LOG.debug ( "setSuppressGrblState: selected=" + show );

        showGrblStateLines = show;

    }

    public void setShowGcodeState ( boolean show ) {

        LOG.debug ( "setSuppressGcodState: selected=" + show );

        showGcodeModeLines = show;

    }
        
}