package com.cognodyne.dw.jta;

import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.cognodyne.dw.common.DeployableWeldService;
import com.cognodyne.dw.common.JndiSupport;

public class TransactionServiceProvider implements TransactionServices, DeployableWeldService {
    private static final Logger                     logger   = LoggerFactory.getLogger(TransactionServiceProvider.class);
    private static final TransactionServiceProvider instance = new TransactionServiceProvider();
    private volatile JTAEnvironmentBean             jtaEnv;

    private TransactionServiceProvider() {
    }

    public static TransactionServiceProvider getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Service> getType() {
        try {
            return (Class<Service>) Class.forName(TransactionServices.class.getName());
        } catch (ClassNotFoundException e) {
            logger.error("this should not happen");
            return null;
        }
    }

    @Override
    public Service getService() {
        return this;
    }

    protected void init() {
        if (jtaEnv == null) {
            synchronized (instance) {
                if (jtaEnv == null) {
                    logger.debug("initializing...");
                    jtaEnv = jtaPropertyManager.getJTAEnvironmentBean();
                    jtaEnv.setTransactionManagerClassName(TransactionManagerImple.class.getName());
                    try {
                        JndiSupport.bind(jtaEnv.getUserTransactionJNDIContext(), jtaEnv.getUserTransaction());
                        JndiSupport.bind("java:comp/UserTransaction", jtaEnv.getUserTransaction());
                        JndiSupport.bind(jtaEnv.getTransactionManagerJNDIContext(), jtaEnv.getTransactionManager());
                        JndiSupport.bind(jtaEnv.getTransactionSynchronizationRegistryJNDIContext(), jtaEnv.getTransactionSynchronizationRegistry());
                        logger.info("Successfully intialized {}", this.getClass().getName());
                    } catch (NamingException ex) {
                        logger.error("Unable to init", ex);
                    }
                }
            }
        }
    }

    @Override
    public void cleanup() {
        logger.debug("cleanup called...");
        //nothing to do
    }

    @Override
    public void registerSynchronization(Synchronization synchronizedObserver) {
        logger.debug("registerSynchronization called");
        TransactionSynchronizationRegistry reg = (TransactionSynchronizationRegistry) JndiSupport.lookup(jtaEnv.getTransactionSynchronizationRegistryJNDIContext());
        if (reg != null) {
            reg.registerInterposedSynchronization(synchronizedObserver);
        }
    }

    @Override
    public boolean isTransactionActive() {
        try {
            return getUserTransaction().getStatus() == Status.STATUS_ACTIVE;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public UserTransaction getUserTransaction() {
        logger.debug("getUserTransaction called");
        return (UserTransaction) JndiSupport.lookup(jtaEnv.getUserTransactionJNDIContext());
    }
}
