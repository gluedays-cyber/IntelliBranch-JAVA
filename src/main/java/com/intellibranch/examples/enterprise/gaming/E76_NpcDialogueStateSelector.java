package com.intellibranch.examples.enterprise.gaming;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E76] Dynamic RPG NPC Dialogue & Quest Branch Router
 * Domain: Gaming & Interactive Systems
 * Routes open-ended player dialogue to NPC emotional reactions and dynamic branching states
 */
public class E76_NpcDialogueStateSelector extends BaseDemo {

    @Override
    public String getId() { return "E76"; }

    @Override
    public String getTitle() { return "Dynamic RPG NPC Dialogue & Quest Branch Router"; }

    @Override
    public String getCategory() { return "Gaming & Interactive Systems"; }

    @Override
    public String getDescription() { return "Routes open-ended player dialogue to NPC emotional reactions and dynamic branching states"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "npc_become_hostile", "npc_offer_quest", "npc_friendly_trade", "fallback", "threaten_npc", "unsheathe_sword", "steal_pocket", "show_wares", "buy_potions", "gold_coins", "ask_for_work", "rumors_at_inn", "missing_artifact", "threaten", "npc", "unsheathe", "sword", "steal", "pocket", "hostile", "ask", "for", "work", "rumors", "at", "inn", "missing", "artifact", "quest", "show", "wares", "buy", "potions", "gold", "coins", "trading", "silence", "standing", "still" };
        String[] classes = new String[]{ "npc_become_hostile", "npc_offer_quest", "npc_friendly_trade", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("npc_become_hostile", new String[]{"threaten_npc", "unsheathe_sword", "steal_pocket"});
            anchorMap.put("npc_friendly_trade", new String[]{"show_wares", "buy_potions", "gold_coins"});
            anchorMap.put("npc_offer_quest", new String[]{"ask_for_work", "rumors_at_inn", "missing_artifact"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "threaten npc unsheathe sword steal pocket hostile");
        evaluateAndPrint(bundle, "ask for work rumors at inn missing artifact quest");
        evaluateAndPrint(bundle, "show wares buy potions gold coins trading");
        evaluateAndPrint(bundle, "silence standing still");
    }

    public static void main(String[] args) {
        new E76_NpcDialogueStateSelector().execute();
    }
}