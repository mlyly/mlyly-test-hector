/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.mlyly.testing;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlyly
 */
public class InsertAndPagingTest extends AbstractBaseTest {
    
    private static final Logger log = LoggerFactory.getLogger(InsertAndPagingTest.class);
    
    @Test
    public void testTest() {
        log.info("testTest(): cluster = {}, keyspace={}", _cluster, _keyspace);
    }
}
