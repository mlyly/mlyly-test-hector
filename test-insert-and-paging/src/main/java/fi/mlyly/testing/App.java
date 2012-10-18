package fi.mlyly.testing;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;

/**
 * Simple testing for Hector & Cassandra.
 *
 * How to inserts and select all / paging.
 *
 */
public class App {

    private static final Logger log = Logger.getAnonymousLogger();

    private static StringSerializer _ss = StringSerializer.get();
    private static LongSerializer _ls = LongSerializer.get();

    public static void main(String[] args) {
        log.info("main()...");

        log.info("  Create cluster...");
        Cluster cluster = HFactory.getOrCreateCluster("TestCluster", "localhost:9160");

        log.info("  Create Keyspace...");
        Keyspace keyspaceOperator = HFactory.createKeyspace("Keyspace1", cluster);

        log.info("  Create Mutator...");
        Mutator<String> mutator = HFactory.createMutator(keyspaceOperator, _ss);

        for (int i = 0; i < 10001; i++) {
            String key = UUID.randomUUID().toString() + "_" + i;

            mutator.addInsertion(key, "Standard1", HFactory.createStringColumn("column_name_1", "column_1_value_" + i));
            mutator.addInsertion(key, "Standard1", HFactory.createStringColumn("column_name_2", "column_2_value_" + i));

            if (i % 1000 == 0) {
                log.log(Level.INFO, "  Execute mutator... i = {0}", i);
                mutator.execute();
            }
        }

        log.info("  Execute mutator...");
        mutator.execute();

        log.info("  Do the Query...");

        int totalCount = 0;
        int pageSize = 2345;
        String keyStart = "";

        RangeSlicesQuery<String, String, String> rangeSlicesQuery = HFactory.createRangeSlicesQuery(keyspaceOperator, _ss, _ss, _ss);
        rangeSlicesQuery.setColumnFamily("Standard1");
        rangeSlicesQuery.setRange("", "", false, 3);
        rangeSlicesQuery.setRowCount(pageSize + 1);  // +1 used for paging, ie. next key start

        while (keyStart != null) {
            rangeSlicesQuery.setKeys(keyStart, "");

            QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
            OrderedRows<String, String, String> orderedRows = result.get();

            log.info("  got page: count = " + orderedRows.getCount());

//            log.info("Contents...: from count: " + totalCount);
//            for (Row<String, String, String> r : orderedRows) {
//                log.info("  -- " + r);
//            }

            Row<String, String, String> lastRow = orderedRows.peekLast();

            // At the end / last page?
            keyStart = (orderedRows.getCount() != (pageSize + 1)) ? null : lastRow.getKey();
            if (keyStart != null) {
                // nope, drop last result from counts
                totalCount += pageSize;
            } else {
                // yep, include all
                totalCount += orderedRows.getCount();
            }
        }

        log.info("TOTAL 'rows' = " + totalCount);

        log.info("  shutdown...");
        cluster.getConnectionManager().shutdown();

        log.info("main()... done.");
    }
}
