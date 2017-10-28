package com.cognodyne.dw.jta;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.injection.spi.helpers.AbstractResourceServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognodyne.dw.common.DeployableWeldService;

public class ResourceInjectionServiceProvider extends AbstractResourceServices implements DeployableWeldService {
    private static final Logger                           logger   = LoggerFactory.getLogger(ResourceInjectionServiceProvider.class);
    private static final ResourceInjectionServiceProvider instance = new ResourceInjectionServiceProvider();
    private Context                                       ctx;

    private ResourceInjectionServiceProvider() {
        try {
            ctx = new InitialContext();
            logger.debug("ctx created");
        } catch (NamingException e) {
            logger.error("Unable to create initial context", e);
        }
    }

    public static ResourceInjectionServiceProvider getInstance() {
        return instance;
    }

    @Override
    protected Context getContext() {
        logger.debug("returning ctx:{}", ctx);
        return this.ctx;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Service> getType() {
        try {
            return (Class<Service>) Class.forName(org.jboss.weld.injection.spi.ResourceInjectionServices.class.getName());
        } catch (ClassNotFoundException e) {
            logger.error("this should not happen");
            return null;
        }
    }

    @Override
    public Service getService() {
        return this;
    }
}
