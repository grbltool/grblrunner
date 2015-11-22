package de.jungierek.grblrunner.service.gcode.impl;

import org.eclipse.e4.core.di.InjectorFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger ( Activator.class );

    @Override
    public void start ( BundleContext context ) throws Exception {

        LOG.debug ( "start:" );

        InjectorFactory.getDefault ().addBinding ( IGcodeProgram.class ).implementedBy ( GcodeProgramImpl.class );

    }

    @Override
    public void stop ( BundleContext context ) throws Exception {

        LOG.debug ( "stop:" );

    }

}
