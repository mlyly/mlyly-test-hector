/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.mlyly.testing;

import java.util.UUID;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.CountQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlyly
 */
public class InsertAndPagingTest extends AbstractBaseTest {
    
    private static final Logger log = LoggerFactory.getLogger(InsertAndPagingTest.class);
    
    int NUM_ROWS_TO_INSERT = 101;
    int NUM_COLS_TO_INSERT = 11;
    String CF = "Standard1";
    
    @Test
    public void testTest() {
        log.info("testTest(): cluster = {}, keyspace={}", _cluster, _keyspace);
    }
    
    @Test
    public void testInsertSome() {
        log.info("testInsertSome()...");
        
        log.info("  Create Mutator...");
        Mutator<String> mutator = HFactory.createMutator(_keyspace, _ss);
        
        for (int i = 0; i < NUM_ROWS_TO_INSERT; i++) {
            String key = UUID.randomUUID().toString() + "_" + i;
            
            for (int j = 0; j < NUM_COLS_TO_INSERT; j++) {
                mutator.addInsertion(key, CF, HFactory.createStringColumn("column_name_" + j, "Value: i=" + i + ", j=" + j));
            }
            
            if (i % ((NUM_ROWS_TO_INSERT / 10) + 1) == 0) {
                log.info("  execute mutator... at i={}", i);
                mutator.execute();
            }
        }
        
        log.info("  Execute mutator or the last time...");
        mutator.execute();
        
        log.info("testInsertSome()... done.");
    }
    
    
//    @Test
    public void testCount() {
        log.info("testCount()...");
        
        CountQuery<String, String> cq = HFactory.createCountQuery(_keyspace, _ss, _ss);
        cq.setColumnFamily(CF);
        cq.setKey("");

        //        cq.setColumnFamily(CF).setKey("");

        cq.setRange("", "", 1); // columns to select
        QueryResult<Integer> r = cq.execute();        
        
        log.info("  count={}", r.get());
        
        log.info("testCount()... done.");
    }
    
    
    @Test
    public void testFindPaging() {
        log.info("testFindPaging()...");
        
        int totalCount = 0;
        int pageSize = 157;
        String keyStart = "";
        
        RangeSlicesQuery<String, String, String> rangeSlicesQuery = HFactory.createRangeSlicesQuery(_keyspace, _ss, _ss, _ss);
        rangeSlicesQuery.setColumnFamily("Standard1");
        rangeSlicesQuery.setRange("", "", false, 20);
        rangeSlicesQuery.setRowCount(pageSize + 1);  // +1 used for paging, ie. next key start

        while (keyStart != null) {
            rangeSlicesQuery.setKeys(keyStart, "");
            
            QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
            OrderedRows<String, String, String> orderedRows = result.get();
                        
            // Next pages first entry, key used to seek to correct location
            Row<String, String, String> lastRow = orderedRows.peekLast();

            // At the end / last page?
            keyStart = (orderedRows.getCount() != (pageSize + 1)) ? null : lastRow.getKey();
            if (keyStart != null) {
                // nope, drop last result from counts since this is not last page
                totalCount += pageSize;
            } else {
                // yep, include all since this was actually last page
                totalCount += orderedRows.getCount();
                
                log.info("  Content for the last page...");
                for (Row<String, String, String> r : orderedRows) {
                    log.info("  -- " + r);
                }
            }
            
            log.info("  got page: count={}, totalCount={}", orderedRows.getCount(), totalCount);
        }
        
        log.info("TOTAL 'rows' = " + totalCount);
        
        log.info("testFindPaging()... done.");
    }
    
    
}
