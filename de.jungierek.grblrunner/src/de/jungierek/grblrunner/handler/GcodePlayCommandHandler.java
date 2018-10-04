package de.jungierek.grblrunner.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.part.GcodeViewPart;
import de.jungierek.grblrunner.part.group.GcodeViewGroup;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tool.GuiFactory;

public class GcodePlayCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodePlayCommandHandler.class );

    // @formatter:off
    @SuppressWarnings("restriction")
    @Execute
    public void execute ( 
            Display display, 
            Shell shell, 
            IGcodeService gcodeService, 
            EPartService partService, 
            @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram,
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PLAY_GCODE_DIALOG_SHOW) boolean showDialog,
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PLAY_GCODE_DIALOG_FONT_DATA) String fontDataString,
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_DEPTH) double probeDepth, 
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_FEEDRATE) double probeFeedrate, 
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_WITH_ERROR) boolean probeWithError
            
    ) {
    // @formatter:on

        LOG.debug ( "execute: program=" + gcodeProgram );

        if ( !showDialog || showDialog && new GcodePlayDialog ( display, shell, gcodeService, partService, fontDataString ).open () == 0 ) { // 0 -> ok, 1 -> Cancel
            gcodeService.playGcodeProgram ( gcodeProgram, probeDepth, probeFeedrate, probeWithError );
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isAutolevelScan () );

        return serial.isOpen () && gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan ()
                && gcodeService.isGrblIdle ();

    }

    public static class GcodePlayDialog extends Dialog {

        private Display display;
        private Shell shell;
        private IGcodeService gcodeService;
        private EPartService partService;
        private Font font;

        public GcodePlayDialog ( Display display, Shell shell, IGcodeService gcodeService, EPartService partService, String fontDataString ) {

            super ( shell );

            this.display = display;
            this.shell = shell;
            this.gcodeService = gcodeService;
            this.partService = partService;
            this.font = new Font ( display, new FontData ( fontDataString ) );

        }

        @Override
        protected void configureShell ( Shell newShell ) {

            super.configureShell ( newShell );

            newShell.setText ( "Starting Gcode Job" );
            newShell.setSize ( IConstant.PLAY_GCODE_DIALOG_WIDTH, IConstant.PLAY_GCODE_DIALOG_HEIGHT );
            newShell.setLocation ( (shell.getSize ().x - IConstant.PLAY_GCODE_DIALOG_WIDTH) / 2, (shell.getSize ().y - IConstant.PLAY_GCODE_DIALOG_HEIGHT) / 2 );

        }

        @Override
        protected Control createDialogArea ( Composite parent ) {

            final int cols = 3;

            final Composite dialogArea = (Composite) super.createDialogArea ( parent ); // HACK
            ((GridLayout) dialogArea.getLayout ()).numColumns = cols;

            GuiFactory.createHeadingLabel ( dialogArea, "Fixture" );
            GuiFactory.createHeadingLabel ( dialogArea, "Metric" );
            GuiFactory.createHeadingLabel ( dialogArea, "Distance" );

            GuiFactory.createHeadingLabel ( dialogArea, gcodeService.getFixture () ).setFont ( font );
            GuiFactory.createHeadingLabel ( dialogArea, gcodeService.getMetricMode () ).setFont ( font );
            GuiFactory.createHeadingLabel ( dialogArea, gcodeService.getDistanceMode () ).setFont ( font );

            MPart part = partService.findPart ( IConstant.GCODE_VIEW_PART_ID );
            GcodeViewPart partObject = (GcodeViewPart) part.getObject ();
            GcodeViewGroup group = partObject.getGcodeViewGroup ();

            Canvas canvas = new Canvas ( dialogArea, SWT.NO_BACKGROUND );
            canvas.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, cols, 1 ) );
            canvas.setEnabled ( true );
            canvas.addPaintListener ( new PaintListener () {

                @Override
                public void paintControl ( PaintEvent evt ) {

                    Image gcodeImage = group.getGcodeImage ();
                    evt.gc.drawImage ( gcodeImage, gcodeImage.getBounds ().x, gcodeImage.getBounds ().y, gcodeImage.getBounds ().width, gcodeImage.getBounds ().height, 0, 0,
                            canvas.getBounds ().width, canvas.getBounds ().height );

                }
            } );

            return dialogArea;

        }

        @Override
        protected Button createButton ( Composite parent, int id, String label, boolean defaultButton ) {

            final Button button = super.createButton ( parent, id, label, defaultButton );
            if ( id == IDialogConstants.OK_ID ) {
                button.setText ( "Start" );
                button.setBackground ( display.getSystemColor ( SWT.COLOR_GREEN ) );
            }

            return button;

        }

    }

}
