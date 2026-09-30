package com.intellibranch.examples.enterprise.gaming;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E73] Competitive Esports Matchmaking & Region Router
 * Domain: Gaming & Interactive Systems
 * Allocates ranked multiplayer lobbies across edge game servers based on ping and MMR
 */
public class E73_MatchmakingLatencyOptimizer extends BaseDemo {

    @Override
    public String getId() { return "E73"; }

    @Override
    public String getTitle() { return "Competitive Esports Matchmaking & Region Router"; }

    @Override
    public String getCategory() { return "Gaming & Interactive Systems"; }

    @Override
    public String getDescription() { return "Allocates ranked multiplayer lobbies across edge game servers based on ping and MMR"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "match_ranked_na_east", "match_ranked_eu_central", "casual_pool_global", "fallback", "diamond_tier", "ping_15ms_nyc", "north_america", "master_tier", "ping_12ms_fra", "europe_west", "unranked_newbie", "party_mixed_skill", "arcade_mode", "diamond", "tier", "ping", "15ms", "nyc", "north", "america", "matchmaking", "master", "12ms", "fra", "europe", "west", "match", "unranked", "newbie", "party", "mixed", "skill", "arcade", "mode", "lobby", "idle", "heartbeat" };
        String[] classes = new String[]{ "match_ranked_na_east", "match_ranked_eu_central", "casual_pool_global", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("match_ranked_na_east", new String[]{"diamond_tier", "ping_15ms_nyc", "north_america"});
            anchorMap.put("match_ranked_eu_central", new String[]{"master_tier", "ping_12ms_fra", "europe_west"});
            anchorMap.put("casual_pool_global", new String[]{"unranked_newbie", "party_mixed_skill", "arcade_mode"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "diamond tier ping 15ms nyc north america matchmaking");
        evaluateAndPrint(bundle, "master tier ping 12ms fra europe west match");
        evaluateAndPrint(bundle, "unranked newbie party mixed skill arcade mode lobby");
        evaluateAndPrint(bundle, "idle matchmaking heartbeat");
    }

    public static void main(String[] args) {
        new E73_MatchmakingLatencyOptimizer().execute();
    }
}