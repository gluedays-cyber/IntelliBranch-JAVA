package com.intellibranch.examples.enterprise.gaming;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E74] Real-Time In-Game Voice & Chat Toxicity Filter
 * Domain: Gaming & Interactive Systems
 * Detects hate speech, death threats, and harassment in real-time game channels to auto-mute
 */
public class E74_InGameChatToxicityFilter extends BaseDemo {

    @Override
    public String getId() { return "E74"; }

    @Override
    public String getTitle() { return "Real-Time In-Game Voice & Chat Toxicity Filter"; }

    @Override
    public String getCategory() { return "Gaming & Interactive Systems"; }

    @Override
    public String getDescription() { return "Detects hate speech, death threats, and harassment in real-time game channels to auto-mute"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "auto_mute_and_report", "flag_moderator_review", "clean_game_chat", "fallback", "good_game_gg", "need_healing", "rush_b_plant", "griefing_taunt", "trash_talk", "spam_caps", "hate_speech", "death_threat", "racial_slur", "death", "threat", "racial", "slur", "hate", "speech", "detected", "griefing", "taunt", "trash", "talk", "spam", "caps", "in", "match", "good", "game", "gg", "need", "healing", "rush", "b", "plant", "tactics", "blank", "chat", "line" };
        String[] classes = new String[]{ "auto_mute_and_report", "flag_moderator_review", "clean_game_chat", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("clean_game_chat", new String[]{"good_game_gg", "need_healing", "rush_b_plant"});
            anchorMap.put("flag_moderator_review", new String[]{"griefing_taunt", "trash_talk", "spam_caps"});
            anchorMap.put("auto_mute_and_report", new String[]{"hate_speech", "death_threat", "racial_slur"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "death threat racial slur hate speech detected");
        evaluateAndPrint(bundle, "griefing taunt trash talk spam caps in match");
        evaluateAndPrint(bundle, "good game gg need healing rush b plant tactics");
        evaluateAndPrint(bundle, "blank chat line");
    }

    public static void main(String[] args) {
        new E74_InGameChatToxicityFilter().execute();
    }
}