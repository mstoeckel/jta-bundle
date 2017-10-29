package com.cognodyne.dw.jta;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

@Singleton
public class JtaBundle implements Bundle {
    private static final Logger logger = LoggerFactory.getLogger(JtaBundle.class);

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        logger.debug("initializing TransactionServiceProvider...");
    }

    @Override
    public void run(Environment environment) {
        logger.debug("running...");
        TransactionServiceProvider.getInstance().init();
    }
}
