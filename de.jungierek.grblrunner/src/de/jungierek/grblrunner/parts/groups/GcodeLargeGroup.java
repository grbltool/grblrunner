package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.IEvents;
import de.jungierek.grblrunner.tools.IPersistenceKeys;
import de.jungierek.grblrunner.tools.IPreferences;

public class GcodeLargeGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeLargeGroup.class );

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    Shell shell;

    private Label gcodeModeLabel;
    private Label gcodeXLabel;
    private Label gcodeYLabel;
    private Label gcodeZLabel;

    Font largeFont;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_GROUP_COLS )).intValue ();
        // Group group = GuiFactory.createGroup ( parent, "", groupCols, 1, true, true );
        Composite group = new Composite ( parent, SWT.NONE );
        group.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );

        final int cols = 7;
        group.setLayout ( new GridLayout ( cols, true ) );

        gcodeModeLabel = GuiFactory.createHeadingLabel ( group, "" );
        largeFont = FontDescriptor.createFrom ( gcodeModeLabel.getFont () ).setStyle ( IPreferences.GCODE_LARGE_FONT_STYLE ).setHeight ( IPreferences.GCODE_LARGE_FONT_SIZE ).createFont ( gcodeModeLabel.getDisplay () );
        gcodeModeLabel.setFont ( largeFont );

        gcodeXLabel = GuiFactory.createHeadingLabel ( group, "", 2 );
        gcodeXLabel.setFont ( largeFont );

        gcodeYLabel = GuiFactory.createHeadingLabel ( group, "", 2 );
        gcodeYLabel.setFont ( largeFont );

        gcodeZLabel = GuiFactory.createHeadingLabel ( group, "", 2 );
        gcodeZLabel.setFont ( largeFont );

    }

    @Inject
    @Optional
    public void stateUpdateNotified ( @UIEventTopic(IEvents.EVENT_GCODE_UPDATE_STATE) IGcodeGrblState state ) {

        LOG.debug ( "stateUpdateNotified: state="+state);
        
        if ( state == null ) return;

        IGcodePoint workCoordindates = state.getWorkCoordindates ();
        gcodeXLabel.setText ( "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, workCoordindates.getX () ) );
        gcodeYLabel.setText ( "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, workCoordindates.getY () ) );
        gcodeZLabel.setText ( "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, workCoordindates.getZ () ) );

    }

    @Inject
    @Optional
    public void updateCoordSelectNotified ( @UIEventTopic(IEvents.EVENT_GCODE_UPDATE_COORD_SELECT) String coordSelect ) {

        LOG.debug ( "updateCoordSelectNotified: coordSelect=" + coordSelect );

        gcodeModeLabel.setText ( coordSelect );
        if ( "G54".equals ( coordSelect ) ) {
            gcodeModeLabel.setForeground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_RED ) );
        }
        else {
            gcodeModeLabel.setForeground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_BLACK ) );
        }

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "disconnectedNotified: param=" + param );

        gcodeModeLabel.setText ( "" );

        gcodeXLabel.setText ( "" );
        gcodeYLabel.setText ( "" );
        gcodeZLabel.setText ( "" );

    }

}
