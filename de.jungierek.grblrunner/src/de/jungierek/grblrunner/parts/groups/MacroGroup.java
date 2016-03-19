package de.jungierek.grblrunner.parts.groups;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.PartTools;

public abstract class MacroGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroGroup.class );

    @Inject
    protected PartTools partTools;

    @Inject
    private IGcodeProgram gcodeProgram;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private IEclipseContext context;

    // preferences
    protected int spindle_min_rpm;
    protected int spindle_max_rpm;
    protected double zClearance;

    @Inject
    public void setSpindleMinRpm ( @Preference(nodePath = IConstants.PREFERENCE_NODE, value = IPreferenceKey.SPINDLE_MIN) int min ) {

        spindle_min_rpm = min;

    }

    @Inject
    public void setSpindleMaxRpm ( @Preference(nodePath = IConstants.PREFERENCE_NODE, value = IPreferenceKey.SPINDLE_MAX) int max ) {

        spindle_max_rpm = max;

    }

    @Inject
    public void setZClearance ( @Preference(nodePath = IConstants.PREFERENCE_NODE, value = IPreferenceKey.Z_CLEARANCE) double value ) {

        zClearance = value;

    }

    @PostConstruct
    public void createGui ( Composite parent ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IContextKey.KEY_PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IContextKey.KEY_PART_GROUP_COLS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, getGroupName (), groupCols, 1, true );

        final int cols = getGridLayoutColumns ();
        group.setLayout ( new GridLayout ( cols, true ) );

        createGroupControls ( group, partCols, groupCols );

        generateGcodeProgram ();
        setControlsEnabled ( true );

    }

    abstract protected int getGridLayoutColumns ();
    abstract protected String getGroupName ();
    abstract public void setControlsEnabled ( boolean b );
    abstract protected void createGroupControls ( Group group, int partCols, int groupCols );

    abstract protected String getTitle ();
    abstract protected void generateGcodeCore ( IGcodeProgram gcodeProgram );

    private String getTimestamp () {

        return new SimpleDateFormat ( "dd.MM.yyyy HH.mm:ss" ).format ( new Date () );

    }

    private void generateGcodeProgram () {

        LOG.debug ( "generateGcodeProgram:" );

        gcodeProgram.clear ();

        gcodeProgram.appendLine ( "(Macro for " + getTitle () + ")" );
        gcodeProgram.appendLine ( "(generated " + getTimestamp () + ")" );
        gcodeProgram.appendLine ( "G21" );
        gcodeProgram.appendLine ( "G90" );

        generateGcodeCore ( gcodeProgram );

        gcodeProgram.appendLine ( "G0 Z" + String.format ( IConstants.FORMAT_COORDINATE, zClearance ) );
        gcodeProgram.appendLine ( "M5" );

        gcodeProgram.parse ();

        Text gcodeText = (Text) context.get ( IConstants.MACRO_TEXT_ID );
        if ( gcodeText != null ) partTools.gcodeToText ( gcodeText, gcodeProgram );

        eventBroker.send ( IEvents.GCODE_MACRO_GENERATED, null );
        eventBroker.send ( IEvents.REDRAW, null );

    }

    protected final ModifyListener textFieldModifyListener = new ModifyListener () {
        @Override
        public void modifyText ( ModifyEvent evt ) {
            generateGcodeProgram ();
        }
    };

    protected final SelectionListener buttonSelectionListener = new SelectionAdapter () {
        @Override
        public void widgetSelected ( SelectionEvent e ) {
            generateGcodeProgram ();
        }
    };

    // ------------------------------------------------------------------------------------------

    protected String formatCoordinate ( double value ) {

        return String.format ( IConstants.FORMAT_COORDINATE, value );
        
    }

    protected String generateXY ( double x, double y ) {
    
        return "X" + formatCoordinate ( x ) + " Y" + formatCoordinate ( y );
    
    }

    protected void motionSeekXY ( double x, double y ) {

        gcodeProgram.appendLine ( "G0 " + generateXY ( x, y ) );

    }

    protected void motionSeekZ ( double z ) {

        gcodeProgram.appendLine ( "G0 Z" + formatCoordinate ( z ) );

    }

    protected void motionLinearXY ( double x, double y, int feedrate ) {

        gcodeProgram.appendLine ( "G1 " + generateXY ( x, y ) + " F" + feedrate );

    }

    protected void motionLinearZ ( double z, int feedrate ) {

        gcodeProgram.appendLine ( "G1 Z" + formatCoordinate ( z ) + " F" + feedrate );

    }

    protected void spindleOn ( int spindleSpeed ) {

        gcodeProgram.appendLine ( "M3 S" + spindleSpeed );
        wait ( 2 );

    }

    protected void wait ( int sec ) {

        gcodeProgram.appendLine ( "G4 P" + sec );

    }

}
