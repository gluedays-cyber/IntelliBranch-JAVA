package com.intellibranch.examples.enterprise.gaming;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E78] Mobile Game IAP Receipt Validation & Piracy Guard
 * Domain: Gaming & Interactive Systems
 * Validates Apple App Store / Google Play billing payloads against jailbreak spoofing frameworks
 */
public class E78_InAppPurchaseFraudDetector extends BaseDemo {

    @Override
    public String getId() { return "E78"; }

    @Override
    public String getTitle() { return "Mobile Game IAP Receipt Validation & Piracy Guard"; }

    @Override
    public String getCategory() { return "Gaming & Interactive Systems"; }

    @Override
    public String getDescription() { return "Validates Apple App Store / Google Play billing payloads against jailbreak spoofing frameworks"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "revoke_and_ban", "deliver_virtual_currency", "challenge_receipt_reauth", "fallback", "lucky_patcher", "fake_receipt", "zero_cent_spoof", "valid_apple_store_signature", "google_play_jws", "paid_tier", "cached_token_stale", "refund_requested", "network_timeout", "lucky", "patcher", "fake", "receipt", "zero", "cent", "spoof", "pirate", "valid", "apple", "store", "signature", "google", "play", "jws", "paid", "tier", "cached", "token", "stale", "refund", "requested", "network", "timeout", "game", "resume", "ping" };
        String[] classes = new String[]{ "revoke_and_ban", "deliver_virtual_currency", "challenge_receipt_reauth", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("revoke_and_ban", new String[]{"lucky_patcher", "fake_receipt", "zero_cent_spoof"});
            anchorMap.put("deliver_virtual_currency", new String[]{"valid_apple_store_signature", "google_play_jws", "paid_tier"});
            anchorMap.put("challenge_receipt_reauth", new String[]{"cached_token_stale", "refund_requested", "network_timeout"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "lucky patcher fake receipt zero cent spoof pirate");
        evaluateAndPrint(bundle, "valid apple store signature google play jws paid tier");
        evaluateAndPrint(bundle, "cached token stale refund requested network timeout");
        evaluateAndPrint(bundle, "game resume ping");
    }

    public static void main(String[] args) {
        new E78_InAppPurchaseFraudDetector().execute();
    }
}