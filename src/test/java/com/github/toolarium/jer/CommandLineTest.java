/*
 * MyApplicationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.jer;

import com.github.toolarium.jer.info.ProcessStartInformation;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * MyApplicationTest.
 *
 * <p>! This is just a sample please remove it. !</p>
 */
public class CommandLineTest {
    private static final Logger LOG = LoggerFactory.getLogger(CommandLineTest.class);
    
    /**
     * Test Version.
     */
    @Test void testVersion() {
        LOG.debug("==>" + new ProcessStartInformation(new String[] {}).getCommandLine("myjar.jar", false, false, false, false));
    }
}
