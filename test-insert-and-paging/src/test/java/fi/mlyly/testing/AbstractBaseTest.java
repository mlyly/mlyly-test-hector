package fi.mlyly.testing;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlyly
 */
public abstract class AbstractBaseTest {
    
    private static final Logger log = LoggerFactory.getLogger(AbstractBaseTest.class);
    
    protected static Cluster _cluster;
    protected static Keyspace _keyspace;
    
    @BeforeClass
    public static void beforeClass() {
        log.info("beforeClass()");
        
        _cluster = HFactory.getOrCreateCluster("TestCluster", "localhost:9160");
        _keyspace = HFactory.createKeyspace("Keyspace1", _cluster);
    }
    
    @AfterClass
    public static void afterClass() {
        log.info("afterClass()");
        
        HFactory.shutdownCluster(_cluster);
        _cluster = null;
        _keyspace = null;
    }
    
    
}
