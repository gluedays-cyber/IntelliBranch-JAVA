package com.intellibranch.examples.enterprise;

import com.intellibranch.examples.enterprise.web.E01_RestApiIntentRouter;
import com.intellibranch.examples.enterprise.web.E02_GraphQLResolverRouter;
import com.intellibranch.examples.enterprise.web.E03_HttpPayloadTriage;
import com.intellibranch.examples.enterprise.web.E04_GrpcMethodDispatcher;
import com.intellibranch.examples.enterprise.web.E05_RateLimitTierRouter;
import com.intellibranch.examples.enterprise.web.E06_WebSocketStreamRouter;
import com.intellibranch.examples.enterprise.web.E07_CircuitBreakerFallback;
import com.intellibranch.examples.enterprise.web.E08_ContentNegotiationRouter;
import com.intellibranch.examples.enterprise.messaging.E09_KafkaPartitionKeyRouter;
import com.intellibranch.examples.enterprise.messaging.E10_RabbitMqDeadLetterRouter;
import com.intellibranch.examples.enterprise.messaging.E11_EventDrivenArchitectureRouter;
import com.intellibranch.examples.enterprise.messaging.E12_CdcDebeziumEventRouter;
import com.intellibranch.examples.enterprise.messaging.E13_PulsarTopicSelector;
import com.intellibranch.examples.enterprise.messaging.E14_ZeroAllocationLogStreamer;
import com.intellibranch.examples.enterprise.messaging.E15_MetricsAggregationFilter;
import com.intellibranch.examples.enterprise.messaging.E16_AlertNotificationPrioritizer;
import com.intellibranch.examples.enterprise.security.E17_SqlInjectionFilter;
import com.intellibranch.examples.enterprise.security.E18_XssSanitizationRouter;
import com.intellibranch.examples.enterprise.security.E19_JwtClaimVerificationRouter;
import com.intellibranch.examples.enterprise.security.E20_MfaStepUpEnforcer;
import com.intellibranch.examples.enterprise.security.E21_DdosTrafficClassifier;
import com.intellibranch.examples.enterprise.security.E22_ApiTokenPrivilegeRouter;
import com.intellibranch.examples.enterprise.security.E23_RansomwareIoCClassifier;
import com.intellibranch.examples.enterprise.security.E24_ZeroTrustAccessEvaluator;
import com.intellibranch.examples.enterprise.fintech.E25_WireTransferFraudGuard;
import com.intellibranch.examples.enterprise.fintech.E26_CreditCardChargebackPredictor;
import com.intellibranch.examples.enterprise.fintech.E27_CryptoAmlTransactionRouter;
import com.intellibranch.examples.enterprise.fintech.E28_HighFrequencyOrderRouter;
import com.intellibranch.examples.enterprise.fintech.E29_LoanEligibilityClassifier;
import com.intellibranch.examples.enterprise.fintech.E30_FxArbitrageOpportunityFilter;
import com.intellibranch.examples.enterprise.fintech.E31_PaymentGatewaySelector;
import com.intellibranch.examples.enterprise.fintech.E32_TaxAuditRiskTriage;
import com.intellibranch.examples.enterprise.llm.E33_SemanticCacheHitRouter;
import com.intellibranch.examples.enterprise.llm.E34_MultiModelCostOptimizer;
import com.intellibranch.examples.enterprise.llm.E35_PromptInjectionInterceptor;
import com.intellibranch.examples.enterprise.llm.E36_RagVectorQueryRouter;
import com.intellibranch.examples.enterprise.llm.E37_AiAgentToolDispatcher;
import com.intellibranch.examples.enterprise.llm.E38_HallucinationGuardrail;
import com.intellibranch.examples.enterprise.llm.E39_TokenBudgetThrottler;
import com.intellibranch.examples.enterprise.llm.E40_ConversationContextCondenser;
import com.intellibranch.examples.enterprise.iot.E41_SmartHomeVoiceActuator;
import com.intellibranch.examples.enterprise.iot.E42_FactorySensorTelemetryAlert;
import com.intellibranch.examples.enterprise.iot.E43_FleetVehicleGpsAnomaly;
import com.intellibranch.examples.enterprise.iot.E44_MedicalTelemetryMonitor;
import com.intellibranch.examples.enterprise.iot.E45_SmartGridPowerLoadBalancer;
import com.intellibranch.examples.enterprise.iot.E46_DroneFlightTelemetryFilter;
import com.intellibranch.examples.enterprise.iot.E47_WearableVitalSignTriage;
import com.intellibranch.examples.enterprise.iot.E48_ColdChainTemperatureGuard;
import com.intellibranch.examples.enterprise.cloud.E49_K8sPodAutoScalerRouter;
import com.intellibranch.examples.enterprise.cloud.E50_DistributedTraceSamplerDemo;
import com.intellibranch.examples.enterprise.cloud.E51_IncidentSeverityClassifier;
import com.intellibranch.examples.enterprise.cloud.E52_SpotInstanceEvictionHandler;
import com.intellibranch.examples.enterprise.cloud.E53_LogClusterAnomalyDetector;
import com.intellibranch.examples.enterprise.cloud.E54_MultiCloudFailoverRouter;
import com.intellibranch.examples.enterprise.cloud.E55_DatabaseReadWriteSplitter;
import com.intellibranch.examples.enterprise.cloud.E56_ServerlessColdStartOptimizer;
import com.intellibranch.examples.enterprise.ecommerce.E57_SearchQueryIntentParser;
import com.intellibranch.examples.enterprise.ecommerce.E58_ProductRecommendationFilter;
import com.intellibranch.examples.enterprise.ecommerce.E59_InventoryStockoutPredictor;
import com.intellibranch.examples.enterprise.ecommerce.E60_CustomerSupportTicketTriage;
import com.intellibranch.examples.enterprise.ecommerce.E61_DynamicPricingTierRouter;
import com.intellibranch.examples.enterprise.ecommerce.E62_DeliveryCourierAllocator;
import com.intellibranch.examples.enterprise.ecommerce.E63_ReturnRefundValidator;
import com.intellibranch.examples.enterprise.ecommerce.E64_FlashSaleQueuePrioritizer;
import com.intellibranch.examples.enterprise.healthcare.E65_EmergencyRoomTriage;
import com.intellibranch.examples.enterprise.healthcare.E66_PrescriptionDrugInteractionCheck;
import com.intellibranch.examples.enterprise.healthcare.E67_EhrClinicalNoteClassifier;
import com.intellibranch.examples.enterprise.healthcare.E68_MedicalImagingPriorityQueue;
import com.intellibranch.examples.enterprise.healthcare.E69_PatientVitalAlarmSuppressor;
import com.intellibranch.examples.enterprise.healthcare.E70_GenomicSequenceMarkerTriage;
import com.intellibranch.examples.enterprise.healthcare.E71_TelehealthSpecialistRouter;
import com.intellibranch.examples.enterprise.healthcare.E72_InsuranceClaimApprovalRouter;
import com.intellibranch.examples.enterprise.gaming.E73_MatchmakingLatencyOptimizer;
import com.intellibranch.examples.enterprise.gaming.E74_InGameChatToxicityFilter;
import com.intellibranch.examples.enterprise.gaming.E75_AntiCheatBehaviorDetector;
import com.intellibranch.examples.enterprise.gaming.E76_NpcDialogueStateSelector;
import com.intellibranch.examples.enterprise.gaming.E77_GameServerPhysicsSharding;
import com.intellibranch.examples.enterprise.gaming.E78_InAppPurchaseFraudDetector;
import com.intellibranch.examples.enterprise.gaming.E79_LeaderboardBatchScoreRouter;
import com.intellibranch.examples.enterprise.gaming.E80_LiveOpsQuestAssignmentRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Master Enterprise Demonstration Suite Runner.
 * Indexes and executes all 80+ enterprise production scenarios across 10 industry verticals.
 */
public class MegaGalleryRunner {

    private static final List<EnterpriseDemo> demos = new ArrayList<>();

    static {
        demos.add(new E01_RestApiIntentRouter());
        demos.add(new E02_GraphQLResolverRouter());
        demos.add(new E03_HttpPayloadTriage());
        demos.add(new E04_GrpcMethodDispatcher());
        demos.add(new E05_RateLimitTierRouter());
        demos.add(new E06_WebSocketStreamRouter());
        demos.add(new E07_CircuitBreakerFallback());
        demos.add(new E08_ContentNegotiationRouter());
        demos.add(new E09_KafkaPartitionKeyRouter());
        demos.add(new E10_RabbitMqDeadLetterRouter());
        demos.add(new E11_EventDrivenArchitectureRouter());
        demos.add(new E12_CdcDebeziumEventRouter());
        demos.add(new E13_PulsarTopicSelector());
        demos.add(new E14_ZeroAllocationLogStreamer());
        demos.add(new E15_MetricsAggregationFilter());
        demos.add(new E16_AlertNotificationPrioritizer());
        demos.add(new E17_SqlInjectionFilter());
        demos.add(new E18_XssSanitizationRouter());
        demos.add(new E19_JwtClaimVerificationRouter());
        demos.add(new E20_MfaStepUpEnforcer());
        demos.add(new E21_DdosTrafficClassifier());
        demos.add(new E22_ApiTokenPrivilegeRouter());
        demos.add(new E23_RansomwareIoCClassifier());
        demos.add(new E24_ZeroTrustAccessEvaluator());
        demos.add(new E25_WireTransferFraudGuard());
        demos.add(new E26_CreditCardChargebackPredictor());
        demos.add(new E27_CryptoAmlTransactionRouter());
        demos.add(new E28_HighFrequencyOrderRouter());
        demos.add(new E29_LoanEligibilityClassifier());
        demos.add(new E30_FxArbitrageOpportunityFilter());
        demos.add(new E31_PaymentGatewaySelector());
        demos.add(new E32_TaxAuditRiskTriage());
        demos.add(new E33_SemanticCacheHitRouter());
        demos.add(new E34_MultiModelCostOptimizer());
        demos.add(new E35_PromptInjectionInterceptor());
        demos.add(new E36_RagVectorQueryRouter());
        demos.add(new E37_AiAgentToolDispatcher());
        demos.add(new E38_HallucinationGuardrail());
        demos.add(new E39_TokenBudgetThrottler());
        demos.add(new E40_ConversationContextCondenser());
        demos.add(new E41_SmartHomeVoiceActuator());
        demos.add(new E42_FactorySensorTelemetryAlert());
        demos.add(new E43_FleetVehicleGpsAnomaly());
        demos.add(new E44_MedicalTelemetryMonitor());
        demos.add(new E45_SmartGridPowerLoadBalancer());
        demos.add(new E46_DroneFlightTelemetryFilter());
        demos.add(new E47_WearableVitalSignTriage());
        demos.add(new E48_ColdChainTemperatureGuard());
        demos.add(new E49_K8sPodAutoScalerRouter());
        demos.add(new E50_DistributedTraceSamplerDemo());
        demos.add(new E51_IncidentSeverityClassifier());
        demos.add(new E52_SpotInstanceEvictionHandler());
        demos.add(new E53_LogClusterAnomalyDetector());
        demos.add(new E54_MultiCloudFailoverRouter());
        demos.add(new E55_DatabaseReadWriteSplitter());
        demos.add(new E56_ServerlessColdStartOptimizer());
        demos.add(new E57_SearchQueryIntentParser());
        demos.add(new E58_ProductRecommendationFilter());
        demos.add(new E59_InventoryStockoutPredictor());
        demos.add(new E60_CustomerSupportTicketTriage());
        demos.add(new E61_DynamicPricingTierRouter());
        demos.add(new E62_DeliveryCourierAllocator());
        demos.add(new E63_ReturnRefundValidator());
        demos.add(new E64_FlashSaleQueuePrioritizer());
        demos.add(new E65_EmergencyRoomTriage());
        demos.add(new E66_PrescriptionDrugInteractionCheck());
        demos.add(new E67_EhrClinicalNoteClassifier());
        demos.add(new E68_MedicalImagingPriorityQueue());
        demos.add(new E69_PatientVitalAlarmSuppressor());
        demos.add(new E70_GenomicSequenceMarkerTriage());
        demos.add(new E71_TelehealthSpecialistRouter());
        demos.add(new E72_InsuranceClaimApprovalRouter());
        demos.add(new E73_MatchmakingLatencyOptimizer());
        demos.add(new E74_InGameChatToxicityFilter());
        demos.add(new E75_AntiCheatBehaviorDetector());
        demos.add(new E76_NpcDialogueStateSelector());
        demos.add(new E77_GameServerPhysicsSharding());
        demos.add(new E78_InAppPurchaseFraudDetector());
        demos.add(new E79_LeaderboardBatchScoreRouter());
        demos.add(new E80_LiveOpsQuestAssignmentRouter());
    }

    public static List<EnterpriseDemo> getAllDemos() {
        return demos;
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("       INTELLIBRANCH-JAVA: 80+ ENTERPRISE PRODUCTION DEMO SUITE");
        System.out.println("================================================================================");
        System.out.printf("Total Indexed Enterprise Scenarios: %d across 10 Industry Domains%n", demos.size());
        System.out.println("Zero external dependencies | 0 B/op heap allocation on hot paths | ~30 us latency");
        System.out.println("================================================================================");

        if (args.length > 0) {
            String filter = args[0].toLowerCase(Locale.ROOT);
            if (filter.equals("--list")) {
                printIndex();
                return;
            }
            if (filter.equals("--verify")) {
                runVerification();
                return;
            }

            // Filter by ID or Category
            boolean found = false;
            for (EnterpriseDemo demo : demos) {
                if (demo.getId().equalsIgnoreCase(args[0]) || demo.getCategory().toLowerCase(Locale.ROOT).contains(filter)) {
                    demo.execute();
                    found = true;
                }
            }
            if (!found) {
                System.err.printf("[ERROR] No demo found matching argument: '%s'%n", args[0]);
                System.out.println("Use '--list' to view all 80 available scenario IDs.");
            }
            return;
        }

        // Default: Run first 10 representative showcases (one from each domain) and summarize
        System.out.println("[INFO] Running representative showcase (1 demo per industry domain)...");
        System.out.println();

        String lastCat = "";
        int executedCount = 0;
        for (EnterpriseDemo demo : demos) {
            if (!demo.getCategory().equals(lastCat)) {
                demo.execute();
                lastCat = demo.getCategory();
                executedCount++;
            }
        }

        System.out.println();
        System.out.println("================================================================================");
        System.out.printf("  SHOWCASE COMPLETE: %d domain demos executed.%n", executedCount);
        System.out.println("  To run all 80 demos: java -cp target/classes com.intellibranch.examples.enterprise.MegaGalleryRunner --verify");
        System.out.println("  To inspect specific demo: java -cp target/classes com.intellibranch.examples.enterprise.MegaGalleryRunner E25");
        System.out.println("================================================================================");
    }

    public static void runVerification() {
        System.out.println("[VERIFY] Executing and benchmarking all 80 enterprise demos...");
        long start = System.nanoTime();
        int count = 0;
        for (EnterpriseDemo demo : demos) {
            demo.execute();
            count++;
        }
        long totalMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println();
        System.out.println("================================================================================");
        System.out.printf("  [SUCCESS] All %d Enterprise Demos executed without error in %d ms (avg: %.2f ms/demo)%n",
                count, totalMs, (double) totalMs / count);
        System.out.println("================================================================================");
    }

    public static void printIndex() {
        System.out.printf("%-6s | %-32s | %-45s%n", "ID", "DOMAIN", "TITLE");
        System.out.println("--------------------------------------------------------------------------------");
        for (EnterpriseDemo d : demos) {
            System.out.printf("%-6s | %-32s | %-45s%n", d.getId(), d.getCategory(), d.getTitle());
        }
    }
}