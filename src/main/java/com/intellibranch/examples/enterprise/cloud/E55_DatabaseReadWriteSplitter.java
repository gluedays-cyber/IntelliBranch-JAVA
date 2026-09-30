package com.intellibranch.examples.enterprise.cloud;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E55] SQL Read/Write Replica Intelligent Splitter
 * Domain: Cloud Infrastructure & SRE
 * Analyzes incoming SQL AST semantics to route writes to master and reads to read replicas
 */
public class E55_DatabaseReadWriteSplitter extends BaseDemo {

    @Override
    public String getId() { return "E55"; }

    @Override
    public String getTitle() { return "SQL Read/Write Replica Intelligent Splitter"; }

    @Override
    public String getCategory() { return "Cloud Infrastructure & SRE"; }

    @Override
    public String getDescription() { return "Analyzes incoming SQL AST semantics to route writes to master and reads to read replicas"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "master_write_node", "replica_read_pool", "analytics_olap_cluster", "fallback", "group_by_cube", "window_function", "large_aggregation", "insert_into", "update_set", "delete_from", "select_where", "find_by_id", "fetch_records", "insert", "into", "users", "update", "set", "delete", "from", "accounts", "select", "where", "find", "by", "id", "fetch", "records", "query", "group", "cube", "window", "function", "large", "aggregation", "report", "empty", "transaction", "commit" };
        String[] classes = new String[]{ "master_write_node", "replica_read_pool", "analytics_olap_cluster", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("analytics_olap_cluster", new String[]{"group_by_cube", "window_function", "large_aggregation"});
            anchorMap.put("master_write_node", new String[]{"insert_into", "update_set", "delete_from"});
            anchorMap.put("replica_read_pool", new String[]{"select_where", "find_by_id", "fetch_records"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "insert into users update set delete from accounts");
        evaluateAndPrint(bundle, "select where find by id fetch records query");
        evaluateAndPrint(bundle, "group by cube window function large aggregation report");
        evaluateAndPrint(bundle, "empty transaction commit");
    }

    public static void main(String[] args) {
        new E55_DatabaseReadWriteSplitter().execute();
    }
}