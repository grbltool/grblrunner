package de.jungierek.grblrunner.part.group;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.tool.GuiFactory;

@SuppressWarnings("restriction")
public class GcodeLargeGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeLargeGroup.class );

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    Shell shell;

    @Inject
    private Display display;

    private Label gcodeModeLabel;
    private Label gcodeXLabel;
    private Label gcodeYLabel;
    private Label gcodeZLabel;

    // preferences
    private Font coordinateFont;
    
    @Inject
    public void setTerminalFontData ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.GCODE_LARGE_FONT_DATA) String fontDataString ) {
    
        LOG.debug ( "setFontData: fontDataString=" + fontDataString );
    
        coordinateFont = new Font ( display, new FontData ( fontDataString ) );
        if ( gcodeModeLabel != null && !gcodeModeLabel.isDisposed () ) {
            setLabelFont ();
        }
    
    }


    private void setLabelFont () {

        gcodeModeLabel.setFont ( coordinateFont );
        gcodeXLabel.setFont ( coordinateFont );
        gcodeYLabel.setFont ( coordinateFont );
        gcodeZLabel.setFont ( coordinateFont );

    }

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Composite group = new Composite ( parent, SWT.NONE );
        group.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );

        final int cols = 7;
        group.setLayout ( new GridLayout ( cols, true ) );

        gcodeModeLabel = GuiFactory.createHeadingLabel ( group, "" );
        gcodeXLabel = GuiFactory.createHeadingLabel ( group, "", 2 );
        gcodeYLabel = GuiFactory.createHeadingLabel ( group, "", 2 );
        gcodeZLabel = GuiFactory.createHeadingLabel ( group, "", 2 );
        
        setLabelFont ();

    }

    @Inject
    @Optional
    public void stateUpdateNotified ( @UIEventTopic(IEvent.UPDATE_STATE) IGcodeGrblState state ) {

        LOG.debug ( "stateUpdateNotified: state="+state);
        
        if ( state == null ) return;

        IGcodePoint workCoordindates = state.getWorkCoordindates ();
        gcodeXLabel.setText ( "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, workCoordindates.getX () ) );
        gcodeYLabel.setText ( "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, workCoordindates.getY () ) );
        gcodeZLabel.setText ( "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, workCoordindates.getZ () ) );

    }

    @Inject
    @Optional
    public void updateCoordSelectNotified ( @UIEventTopic(IEvent.UPDATE_FIXTURE) String coordSelect ) {

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
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "disconnectedNotified: param=" + param );

        gcodeModeLabel.setText ( "" );

        gcodeXLabel.setText ( "" );
        gcodeYLabel.setText ( "" );
        gcodeZLabel.setText ( "" );

    }

}
