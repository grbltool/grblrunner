package de.jungierek.grblrunner.parts;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.parts.groups.GcodeViewGroup;

public class GcodeViewDirectToolItem {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeViewDirectToolItem.class );

    // toolbar items
    @Execute
    public void execute ( MPart part, MDirectToolItem item ) {

        LOG.debug ( "execute: part=" + part );

        GcodeViewGroup gcodeViewGroup = ((GcodeViewPart) part.getObject ()).getGcodeViewGroup ();
        String type = item.getPersistedState ().get ( "type" );

        if ( type != null ) {
            switch ( type ) {
                case "iso":
                    gcodeViewGroup.viewIso ();
                    break;
                case "grid":
                    gcodeViewGroup.toggleViewGrid ();
                    break;
                case "gcode":
                    gcodeViewGroup.toggleViewGcode ();
                    break;
                case "altitude":
                    gcodeViewGroup.toggleViewAltitude ();
                    break;
                case "workarea":
                    gcodeViewGroup.toggleViewWorkarea ();
                    break;
                case "label":
                    gcodeViewGroup.toggleViewLabel ();
                    break;
                case "xy":
                    gcodeViewGroup.viewPlaneXY ();
                    break;
                case "xz":
                    gcodeViewGroup.viewPlaneXZ ();
                    break;
                case "yz":
                    gcodeViewGroup.viewPlaneYZ ();
                    break;
                case "fittosize":
                    gcodeViewGroup.fitToSize ();
                    break;
                default:
                    break;
            }
        }

    }

}