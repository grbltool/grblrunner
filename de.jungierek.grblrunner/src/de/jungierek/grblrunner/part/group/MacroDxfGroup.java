package de.jungierek.grblrunner.part.group;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.kabeja.DraftDocument;
import org.kabeja.common.DraftEntity;
import org.kabeja.common.Layer;
import org.kabeja.common.LineType;
import org.kabeja.common.Type;
import org.kabeja.dxf.parser.DXFParserBuilder;
import org.kabeja.entities.Arc;
import org.kabeja.entities.Line;
import org.kabeja.entities.Polyline;
import org.kabeja.entities.Vertex;
import org.kabeja.math.Point3D;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tool.GuiFactory;

public class MacroDxfGroup extends MacroGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroDxfGroup.class );

    private static final String GROUP_NAME = "Dxf";
    private static final int PART_COLS = 9;

    private Text xyFeedrateText;
    private Text spindleSpeedText;
    private Text zClearanceText;
    private Text zLiftupText;
    private Text zDepthText;
    private Text zFeedrateText;
    private Text scaleText;

    @Override
    protected int getGridLayoutColumns () {
        return PART_COLS;
    }

    @Override
    protected String getGroupName () {
        return GROUP_NAME;
    }

    @Override
    public void createGroupControls ( Group group, int partCols, int groupCols ) {

        LOG.debug ( "createGroupControls:" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z liftup", 1 );
        zLiftupText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.MACRO_Z_LIFTUP ) ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "feedrate xy", 1 );
        xyFeedrateText = GuiFactory.createIntegerText ( group, "" + getIntPreference ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE ), 1, true, 0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm/min" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z clearance", 1 );
        zClearanceText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.Z_CLEARANCE ) ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "spindle speed", 1 );
        spindleSpeedText = GuiFactory.createIntegerText ( group, "" + getIntPreference ( IPreferenceKey.MACRO_SPINDLE_SPEED ), 1, true,
                getIntPreference ( IPreferenceKey.SPINDLE_MIN ), getIntPreference ( IPreferenceKey.SPINDLE_MAX ) );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "rpm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "feedrate z", 1 );
        zFeedrateText = GuiFactory.createIntegerText ( group, "" + getIntPreference ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE ), 1, true, 0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm/min" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z depth", 1 );
        zDepthText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_Z_DEPTH ) ), 1, true, -5.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "scale", 1 );
        scaleText = GuiFactory.createDoubleText ( group, "1.0", 1, true, -5.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "" );

        xyFeedrateText.addModifyListener ( textFieldModifyListener );
        zFeedrateText.addModifyListener ( textFieldModifyListener );
        spindleSpeedText.addModifyListener ( textFieldModifyListener );
        zClearanceText.addModifyListener ( textFieldModifyListener );
        zLiftupText.addModifyListener ( textFieldModifyListener );
        zDepthText.addModifyListener ( textFieldModifyListener );
        scaleText.addModifyListener ( textFieldModifyListener );

    }

    @Override
    public void restorePreferenceData () {
        
        zLiftupText.setText ( formatCoordinate ( getDoublePreference ( IPreferenceKey.MACRO_Z_LIFTUP ) ) );
        xyFeedrateText.setText ( "" + getIntPreference ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE ) );
        zClearanceText.setText ( formatCoordinate ( getDoublePreference ( IPreferenceKey.Z_CLEARANCE ) ) );
        spindleSpeedText.setText ( "" + getIntPreference ( IPreferenceKey.MACRO_SPINDLE_SPEED ) );
        zDepthText.setText ( formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_Z_DEPTH ) ) );
        zFeedrateText.setText ( "" + getIntPreference ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE ) );

    }

    @Override
    protected String getTitle () {

        return "gcode generation from dxf";

    }

    private Point3D scalePoint ( double scale, Point3D p ) {
        
        return new Point3D ( scale* p.getX (), scale*p.getY (), p.getZ () );
        
    }

    // http://www.programcreek.com/java-api-examples/index.php?api=org.kabeja.parser.Parser
    @Override
    public void generateGcodeCore ( IGcodeProgram gcodeProgram ) {

        LOG.debug ( "generateGcodeCore: start" );
        
        int xyFeedrate = toolbox.parseIntegerField ( xyFeedrateText, getIntPreference ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE ) );
        int spindleSpeed = toolbox.parseIntegerField ( spindleSpeedText, getIntPreference ( IPreferenceKey.MACRO_SPINDLE_SPEED ) );
        double zClearance = toolbox.parseDoubleField ( zClearanceText, getDoublePreference ( IPreferenceKey.Z_CLEARANCE ) );
        double zLiftup = toolbox.parseDoubleField ( zLiftupText, getDoublePreference ( IPreferenceKey.MACRO_Z_LIFTUP ) );
        double zDepth = toolbox.parseDoubleField ( zDepthText, getDoublePreference ( IPreferenceKey.POCKET_MILL_Z_DEPTH ) );
        int zFeedrate = toolbox.parseIntegerField ( zFeedrateText, getIntPreference ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE ) );
        double scale = toolbox.parseDoubleField ( scaleText, 1.0 );

        motionSeekZ ( zClearance );
        motionSeekXY ( 0.0, 0.0 );
        spindleOn ( spindleSpeed );

        final String dxfFileName = (String) context.get ( IContextKey.MACRO_DXF_FILE_NAME );
        LOG.debug ( "generateGcodeCore: dxfFile=" + dxfFileName );
        File file = new File ( dxfFileName );
        Parser parser = DXFParserBuilder.createDefaultParser ();

        try {

            LOG.debug ( "generateGcodeCore: ----------------------------------------" );

            DraftDocument document = parser.parse ( new FileInputStream ( file ), new HashMap<String, Object> () );

            Point3D lastP2 = null;

            Collection<Layer> layers = document.getLayers ();
            // for ( Layer layer : layers ) {
            // LOG.debug ( "generateGcodeCore: layer=" + layer.getName () );
            // }

            Collection<LineType> lineTypes = document.getLineTypes ();
            for ( LineType lineType : lineTypes ) {
                LOG.debug ( "generateGcodeCore: type=" + lineType.getName () );
            }

            LOG.debug ( "generateGcodeCore: TYPE_LINE" );
            for ( Layer layer : layers ) {
                LOG.debug ( "generateGcodeCore: layer=" + layer.getName () );
                List<Line> lines = layer.getEntitiesByType ( Type.TYPE_LINE );
                for ( Line line : lines ) {
                    final Point3D p1 = scalePoint ( scale, line.getStartPoint () );
                    final Point3D p2 = scalePoint ( scale, line.getEndPoint () );
                    LOG.debug ( "generateGcodeCore: p1=" + point ( p1 ) + " p2=" + point ( p2 ) + " lastP2=" + point ( lastP2 ) );
                    if ( !p1.equals ( lastP2 ) ) {
                        // this is valid for the first point with lastP2 == null and also for every start of a new contour
                        motionSeekZ ( zLiftup );
                        motionSeekXY ( p1.getX (), p1.getY () );
                        motionLinearZ ( zDepth, zFeedrate );
                    }
                    motionLinearXY ( p2.getX (), p2.getY (), xyFeedrate );
                    lastP2 = p2;
                }
                motionSeekZ ( zLiftup );
                List<Arc> arcs = layer.getEntitiesByType ( Type.TYPE_ARC );
                for ( Arc arc : arcs ) {
                    final Point3D p1 = scalePoint ( scale, arc.getStartPoint () );
                    final Point3D p2 = scalePoint ( scale, arc.getEndPoint () );
                    LOG.debug ( "generateGcodeCore: p1=" + point ( p1 ) + " p2=" + point ( p2 ) + " lastP2=" + point ( lastP2 ) );
                    if ( !p1.equals ( lastP2 ) ) {
                        // this is valid for the first point with lastP2 == null and also for every start of a new contour
                        motionSeekZ ( zLiftup );
                        motionSeekXY ( p1.getX (), p1.getY () );
                        motionLinearZ ( zDepth, zFeedrate );
                    }
                    motionLinearXY ( p2.getX (), p2.getY (), xyFeedrate );
                    lastP2 = p2;
                }
            }
            motionSeekZ ( zLiftup );

            LOG.debug ( "generateGcodeCore: TYPE_POLYLINE" );
            for ( Layer layer : layers ) {
                LOG.debug ( "generateGcodeCore: layer=" + layer.getName () );
                Collection<Type<? extends DraftEntity>> entityTypes = layer.getEntityTypes ();
                for ( Type<? extends DraftEntity> entityType : entityTypes ) {
                    LOG.debug ( "generateGcodeCore: type=" + entityType.getName () );
                }
                List<Polyline> plines = layer.getEntitiesByType ( Type.TYPE_POLYLINE );
                for ( Polyline pline : plines ) {
                    List<Vertex> vertices = pline.getVertices ();
                    Point3D p1 = null;
                    Point3D p2 = null;
                    for ( Vertex vertex : vertices ) {
                        if ( p1 == null ) {
                            p1 = scalePoint ( scale, vertex.getPoint () );
                            if ( !p1.equals ( lastP2 ) ) {
                                // this is valid for the first point with lastP2 == null and also for every start of a new contour
                                motionSeekZ ( zLiftup );
                                motionSeekXY ( p1.getX (), p1.getY () );
                                motionLinearZ ( zDepth, zFeedrate );
                            }
                        }
                        else {
                            p2 = scalePoint ( scale, vertex.getPoint () );
                            motionLinearXY ( p2.getX (), p2.getY (), xyFeedrate );
                            lastP2 = p2;
                        }
                        LOG.debug ( "generateGcodeCore: p1=" + point ( p1 ) + " p2=" + point ( p2 ) + " lastP2=" + point ( lastP2 ) );
                    }
                }
            }
            motionSeekZ ( zLiftup );

            layers = document.getLayers ();
            for ( Layer layer : layers ) {
                LOG.debug ( "generateGcodeCore: layer=" + layer.getName () );
                Collection<Type<? extends DraftEntity>> entityTypes = layer.getEntityTypes ();
                for ( Type<? extends DraftEntity> entityType : entityTypes ) {
                    LOG.debug ( "generateGcodeCore: type=" + entityType.getName () );
                }
            }

        }
        catch ( FileNotFoundException | ParseException exc ) {

            LOG.error ( "generateGcodeCore:" + exc );

            gcodeGenerationErrorOmiited ();

            StringBuilder sb = new StringBuilder ( "File not founf " + file.getName () + "!\n\n" );
            sb.append ( "Cause:\n" );
            sb.append ( exc + "\n\n" );
            MessageDialog.openError ( shell, "Internal Error", "" + sb );

        }

        LOG.debug ( "generateGcodeCore: end" );

    }

    private String point ( Point3D p ) {

        if ( p == null ) return "null";

        return "[" + p.getX () + "," + p.getY () + "]";

    }

    @Override
    public void setControlsEnabled ( boolean enabled ) {

        spindleSpeedText.setEnabled ( enabled );
        zClearanceText.setEnabled ( enabled );
        xyFeedrateText.setEnabled ( enabled );
        zLiftupText.setEnabled ( enabled );
        zDepthText.setEnabled ( enabled );
        zFeedrateText.setEnabled ( enabled );
        scaleText.setEnabled ( enabled );

    }

}
