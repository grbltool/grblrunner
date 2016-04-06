package de.jungierek.grblrunner.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static final Logger LOG = LoggerFactory.getLogger ( PreferenceInitializer.class );

    public PreferenceInitializer () {
        
        LOG.debug ( "initializeDefaultPreferences:" );

    }

    @Override
    public void initializeDefaultPreferences () {

        LOG.debug ( "initializeDefaultPreferences:" );

        IEclipsePreferences defaults = DefaultScope.INSTANCE.getNode ( IConstant.PREFERENCE_NODE );
        
        // --- Mill ---
        defaults.put ( IPreferenceKey.GCODE_PATH, "C:\\Users\\Andreas\\Documents\\eagle" );
        defaults.putDouble ( IPreferenceKey.WORK_AREA_MAX_X, 107.5 );
        defaults.putDouble ( IPreferenceKey.WORK_AREA_MAX_Y, 84.3 );
        defaults.putInt ( IPreferenceKey.SPINDLE_MIN, 0 );
        defaults.putInt ( IPreferenceKey.SPINDLE_MAX, 12000 );
        defaults.putInt ( IPreferenceKey.MAX_SEEK_FEEDRATE, 600 );
        defaults.putDouble ( IPreferenceKey.ACCELARATION, 20.0 );
        defaults.putDouble ( IPreferenceKey.Z_CLEARANCE, 15.0 );

        // --- GcodeViewGroup ---
        defaults.putDouble ( IPreferenceKey.FIT_TO_SIZE_MARGIN, 20.0 );
        defaults.putBoolean ( IPreferenceKey.FIT_TO_SIZE_WITH_Z, false );

        defaults.put ( IPreferenceKey.COLOR_AUTOLEVEL_GRID, "255,0,255" ); // magenta
        defaults.put ( IPreferenceKey.COLOR_GANTRY, "255,0,0" ); // red
        // defaults.put ( IPreferenceKey.COLOR_PROCESSED, "0,255,0" ); // green
        defaults.put ( IPreferenceKey.COLOR_PROCESSED, "0,187,0" ); // green
        // defaults.put ( IPreferenceKey.COLOR_SEEK, "192,192,192" ); // gray
        defaults.put ( IPreferenceKey.COLOR_SEEK, "128,128,128" ); // gray
        defaults.put ( IPreferenceKey.COLOR_LINEAR, "0,0,255" ); // blue
        defaults.put ( IPreferenceKey.COLOR_ARC, "0,0,255" ); // blue
        defaults.put ( IPreferenceKey.COLOR_OVERLAY_SEEK, "220,220,220" ); // gray
        defaults.put ( IPreferenceKey.COLOR_OVERLAY_LINEAR, "170,170,255" ); // blue
        defaults.put ( IPreferenceKey.COLOR_OVERLAY_ARC, "170,170,255" ); // blue
        defaults.put ( IPreferenceKey.COLOR_PROBE, "255,0,0" ); // red
        defaults.put ( IPreferenceKey.COLOR_MACHINE_ORIGIN, "255,0,255" ); // magenta
        defaults.put ( IPreferenceKey.COLOR_WORK_ORIGIN, "0,255,255" ); // cyan
        // defaults.put ( IPreferenceKey.COLOR_WORKAREA_BORDER, "128,0,0" ); // dark red
        defaults.put ( IPreferenceKey.COLOR_WORKAREA_BORDER, "200,0,0" ); // dark red
        defaults.put ( IPreferenceKey.COLOR_WORKAREA_MIDCROSS, "255,0,0" ); // red

        // --- SerialActionsGroup ---
        defaults.put ( IPreferenceKey.COLOR_CONNECT, "0,255,0" ); // green
        defaults.put ( IPreferenceKey.COLOR_DISCONNECT, "255,0,0" ); // red
        defaults.putInt ( IPreferenceKey.BAUDRATE, 115200 );

        // --- TerminalPart ---
        // defaults.put ( IPreferenceKey.TERMINAL_FONT_DATA, "1|Courier|9.75|0|WINDOWS|1|-13|0|0|0|400|0|0|0|0|1|2|1|49|Courier;" ); // Courier 10pt
        defaults.put ( IPreferenceKey.TERMINAL_FONT_DATA, "1|Courier New|9.75|0|WINDOWS|1|-13|0|0|0|400|0|0|0|0|3|2|1|49|Courier New;" ); // Courier New 10pt
        defaults.put ( IPreferenceKey.COLOR_TERMINAL_FOREGROUND, "0,0,0" ); // black
        defaults.put ( IPreferenceKey.COLOR_TERMINAL_BACKGROUND, "255,255,255" ); // white
        defaults.put ( IPreferenceKey.COLOR_ALARM_FOREGROUND, "255,255,255" ); // white
        defaults.put ( IPreferenceKey.COLOR_ALARM_BACKGROUND, "255,0,0" ); // red
        defaults.put ( IPreferenceKey.COLOR_TIMESTAMP_BACKGROUND, "255,255,0" ); // yellow
        defaults.put ( IPreferenceKey.COLOR_SUPPRESSED_LINE_FOREGROUND, "192,192,192" ); // gray
        defaults.put ( IPreferenceKey.COLOR_OK_FOREGROUND, "0,128,0" ); // dark green
        defaults.put ( IPreferenceKey.COLOR_ERROR_FOREGROUND, "255,0,0" ); // red
        defaults.put ( IPreferenceKey.COLOR_GRBL_FOREGROUND, "255,255,255" ); // white
        defaults.put ( IPreferenceKey.COLOR_GRBL_BACKGROUND, "128,128,128" ); // dark gray
        defaults.put ( IPreferenceKey.COLOR_OK_SUPPRESSED_FOREGROUND, "0,255,0" ); // green
        defaults.put ( IPreferenceKey.COLOR_ERROR_SUPPRESSED_FOREGROUND, "255,0,0" ); // red
        defaults.put ( IPreferenceKey.COLOR_GRBL_SUPPRESSED_FOREGROUND, "0,0,0" ); // white
        defaults.put ( IPreferenceKey.COLOR_GRBL_SUPPRESSED_BACKGROUND, "192,192,192" ); // gray

        // --- Probe ---
        defaults.putInt ( IPreferenceKey.PROBE_FEEDRATE, 40 );
        defaults.putDouble ( IPreferenceKey.PROBE_Z_MAX, +3.0 );
        defaults.putDouble ( IPreferenceKey.PROBE_DEPTH, -3.0 );
        defaults.putBoolean ( IPreferenceKey.PROBE_WITH_ERROR, false );

        // --- Macro ---
        defaults.putInt ( IPreferenceKey.MACRO_SPINDLE_SPEED, 7000 );
        defaults.putDouble ( IPreferenceKey.MACRO_Z_LIFTUP, +2.0 );

        defaults.putInt ( IPreferenceKey.HOBBED_BOLT_FEEDRATE, 10 );
        defaults.putInt ( IPreferenceKey.HOBBED_BOLT_ANGLE, 30 );
        defaults.putInt ( IPreferenceKey.HOBBED_BOLT_BOLT_DIAMETER, 8 );
        defaults.putDouble ( IPreferenceKey.HOBBED_BOLT_X_CLEARANCE, 7.0 );
        defaults.putInt ( IPreferenceKey.HOBBED_BOLT_COUNT_RETRACTION, 3 );
        defaults.putDouble ( IPreferenceKey.HOBBED_BOLT_RETRACTION, 1.0 );
        defaults.putInt ( IPreferenceKey.HOBBED_BOLT_WAIT_AT_TARGET, 5 );
        
        defaults.putInt ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE, 50 );
        defaults.putInt ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE, 150 );
        defaults.putDouble ( IPreferenceKey.POCKET_MILL_DIAMETER, 3.0 );
        defaults.putDouble ( IPreferenceKey.POCKET_MILL_DIMENSION, 10.0 );
        defaults.putDouble ( IPreferenceKey.POCKET_MILL_Z_DEPTH, -1.4 );
        defaults.putInt ( IPreferenceKey.POCKET_MILL_OVERLAP, 30 );
        
        // --- Large Gcode Coordinates
        defaults.put ( IPreferenceKey.GCODE_LARGE_FONT_DATA, "1|Segoe UI|24.75|1|WINDOWS|1|-33|0|0|0|700|0|0|0|1|0|0|0|0|Segoe UI;" ); // Segoe UI 25pt

        // --- Command History ---
        defaults.putInt ( IPreferenceKey.COMMAND_HISTORY_DEPTH, 20 );
        defaults.putBoolean ( IPreferenceKey.COMMAND_HISTORY_WITHNOWERROR, false );

        // -------------------------------

        try {
            defaults.flush ();
        }
        catch ( BackingStoreException exc ) {
            LOG.error ( "initializeDefaultPreferences: exc=" + exc );
            exc.printStackTrace ();
        }

    }

}
