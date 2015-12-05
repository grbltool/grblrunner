package de.jungierek.grblrunner.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeResponse;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class TerminalPart {

    private static final Logger LOG = LoggerFactory.getLogger ( TerminalPart.class );

    private static final String JUSTIFY_PLACE = "                    ";

    private boolean showSuppressedLines = false;

    @Inject
    private IGcodeService gcode;

    @Inject
    private Display display;

    private Color WHITE, RED, GREEN, GRAY, LIGHT_GREEN, LIGHT_GRAY, YELLOW;

    // private Text terminalText;
    private StyledText terminalText;

    private boolean showGrblState = true;
    private boolean showGcodeState = true;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        WHITE = display.getSystemColor ( SWT.COLOR_WHITE );
        RED = display.getSystemColor ( SWT.COLOR_RED );
        GREEN = display.getSystemColor ( SWT.COLOR_DARK_GREEN );
        LIGHT_GREEN = display.getSystemColor ( SWT.COLOR_GREEN );
        GRAY = display.getSystemColor ( SWT.COLOR_DARK_GRAY );
        LIGHT_GRAY = display.getSystemColor ( SWT.COLOR_GRAY );
        YELLOW = display.getSystemColor ( SWT.COLOR_YELLOW );


        // terminalText = new Text ( parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
        // terminalText = new Text ( parent, SWT.MULTI | SWT.V_SCROLL );
        terminalText = new StyledText ( parent, SWT.MULTI | SWT.V_SCROLL );
        // TODO_PREF to pref
        terminalText.setFont ( new Font ( display, "Courier", 10, SWT.NONE ) );
        //terminalText.setEnabled ( false );
        terminalText.setEditable ( false );
        terminalText.setBackground ( display.getSystemColor ( SWT.COLOR_WHITE ) );

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

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvents.GRBL_ALARM) String line ) {
        
        LOG.trace ( "alarmNotified: line=" + line );

        appendText ( line, WHITE, RED, SWT.BOLD );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvents.PLAYER_START) String timestamp ) {

        LOG.trace ( "playerStartNotified: timestamp=" + timestamp );

        appendText ( "Gcode Player started at " + timestamp + "\n", null, YELLOW );
    
    }

    @Inject
    @Optional
    public void playerLineSegmentNotified ( @UIEventTopic(IEvents.PLAYER_SEGMENT) String gcodeSegment ) {

        LOG.trace ( "playerLineNotified: gcodeSegment=" + gcodeSegment );

        terminalText.append ( "- " + gcodeSegment + "\n" );
        scrollToEnd ();

    }

    @Inject
    @Optional
    public void playerLineNotified ( @UIEventTopic(IEvents.PLAYER_LINE) IGcodeLine gcodeLine ) {
    
        LOG.trace ( "playerLineNotified: gcodeLine=" + gcodeLine );

        if ( gcodeLine == null ) return;

        String terminalLine = gcodeLine.getLine ();

        if ( IPreferences.SHOW_GCODE_LINE ) {
            int alignLength = JUSTIFY_PLACE.length () - terminalLine.length ();
            if ( alignLength > 0 ) {
                terminalLine += JUSTIFY_PLACE.substring ( 0, alignLength );
            }
            terminalLine += gcodeLine;
        }

        terminalText.append ( terminalLine + "\n" );
        scrollToEnd ();
    
    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvents.PLAYER_STOP) String timestamp ) {

        LOG.trace ( "playerStopNotified: timestamp=" + timestamp );

        appendText ( "Gcode Player stopped at " + timestamp + "\n", null, YELLOW );
        terminalText.append ( "-------------------------------------------------------------------------------------\n" );
        scrollToEnd ();

    }

    @Inject
    @Optional
    public void sentNotified ( @UIEventTopic(IEvents.GRBL_SENT) IGcodeResponse command ) {

        LOG.trace ( "sentNotified: command=" + command );

        if ( command == null ) {
            LOG.warn ( "receivedNotified: response == null" );
            return;
        }

        if ( command.isReset () ) return;

        String line = command.getLine ();

        if ( !command.suppressInTerminal () ) {
            appendText ( line, SWT.BOLD );
        }
        else if ( showSuppressedLines ) {

            boolean show = true;

            if ( line.startsWith ( "$G" ) ) show = showGcodeState;

            if ( show ) appendText ( line, LIGHT_GRAY, null, SWT.BOLD );

        }

    }
    
    private boolean ignoreNextOk = false;

    @Inject
    @Optional
    public void receivedNotified ( @UIEventTopic(IEvents.GRBL_RECEIVED) IGcodeResponse response ) {

        LOG.trace ( "receivedNotified: response=" + response );

        if ( response == null || response.getLine () == null ) return;

        String line = response.getLine ();

        if ( !response.suppressInTerminal () ) {
            if ( line.startsWith ( "ok" ) ) {
                appendText ( line, GREEN, null, SWT.BOLD );
            }
            else if ( line.startsWith ( "error" ) ) {
                appendText ( line, RED, null, SWT.BOLD );
            }
            else if ( line.startsWith ( "Grbl" ) ) {
                appendText ( line, WHITE, GRAY );
            }
            else {
                terminalText.append ( line );
                scrollToEnd ();
            }
        }
        else if ( showSuppressedLines ) {
            if ( line.startsWith ( "ok" ) ) {
                if ( ignoreNextOk ) ignoreNextOk = false;
                else appendText ( line, LIGHT_GREEN, null, SWT.BOLD );
            }
            else if ( line.startsWith ( "error" ) ) {
                appendText ( line, RED, null, SWT.BOLD );
            }
            else if ( line.startsWith ( "Grbl" ) ) {
                appendText ( line, WHITE, LIGHT_GRAY );
            }
            else {

                boolean show = true;

                if ( line.startsWith ( "<" ) ) show = showGrblState;
                else if ( line.startsWith ( "[G54" ) ) {} // do nothing
                else if ( line.startsWith ( "[G55" ) ) {} // do nothing
                else if ( line.startsWith ( "[G56" ) ) {} // do nothing
                else if ( line.startsWith ( "[G57" ) ) {} // do nothing
                else if ( line.startsWith ( "[G58" ) ) {} // do nothing
                else if ( line.startsWith ( "[G59" ) ) {} // do nothing
                else if ( line.startsWith ( "[G28" ) ) {} // do nothing
                else if ( line.startsWith ( "[G30" ) ) {} // do nothing
                else if ( line.startsWith ( "[G92" ) ) {} // do nothing
                else if ( line.startsWith ( "[G" ) ) {
                    show = showGcodeState;
                    if ( !show ) ignoreNextOk = true;
                }

                if ( show ) appendText ( line, LIGHT_GRAY, null, SWT.BOLD );

            }
        }

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvents.AUTOLEVEL_START) String timestamp ) {

        LOG.trace ( "scanStartNotified:" );

        appendText ( "Probe Scanning started at " + timestamp + "\n", null, YELLOW );

    }

    @Inject
    @Optional
    public void updateProbeNotified ( @UIEventTopic(IEvents.AUTOLEVEL_UPDATE) IGcodePoint probe ) {

        LOG.trace ( "updateProbeNotified: probe=" + probe );

        // convert from machine to work coordinate system
        final IGcodePoint p = probe.sub ( gcode.getFixtureShift () );
        terminalText.append ( "" + probe + "    delta=" + String.format ( IGcodePoint.FORMAT_COORDINATE, p.getZ () ) + "\n" );

        scrollToEnd ();

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvents.AUTOLEVEL_STOP) String timestamp ) {

        LOG.trace ( "scanStopNotified:" );

        appendText ( "Probe Scanning stopped at " + timestamp + "\n", null, YELLOW );

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

        LOG.info ( "setSuppressGrblState: selected=" + show );

        showGrblState = show;

    }

    public void setShowGcodeState ( boolean show ) {

        LOG.info ( "setSuppressGcodState: selected=" + show );

        showGcodeState = show;

    }
        
}