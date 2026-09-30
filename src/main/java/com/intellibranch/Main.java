package com.intellibranch;

import com.intellibranch.core.BinaryModel;
import com.intellibranch.core.InferenceModel;
import com.intellibranch.routing.Router;
import com.intellibranch.training.DataSample;
import com.intellibranch.training.TrainConfig;
import com.intellibranch.training.Trainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Main production entry point for IntelliBranch-JAVA microsecond branch dispatching.
 */
public class Main {

    // 1. Business Logic Handlers
    private static void handleRefund(Object ctx, Object payload) {
        System.out.printf("[ACTION: Refund]   Processing refund for: '%s'%n", payload);
    }

    private static void handleDelivery(Object ctx, Object payload) {
        System.out.printf("[ACTION: Delivery] Querying shipment tracking for: '%s'%n", payload);
    }

    private static void handleAccount(Object ctx, Object payload) {
        System.out.printf("[ACTION: Account]  Initiating account security for: '%s'%n", payload);
    }

    private static void handleFallback(Object ctx, Object payload) {
        System.out.printf("[FALLBACK: Safety] Isolated low-confidence request: '%s'%n", payload);
    }

    public static void main(String[] args) throws Exception {
        Path modelPath = Path.of("weights/intent.bin");

        // Auto-compile model if missing (ensures instant zero-config clone & run)
        if (!Files.exists(modelPath)) {
            System.out.println("Model weights not found. Compiling from data/sample_dataset.csv...");
            List<DataSample> samples = Trainer.loadCSVDataset(Path.of("data/sample_dataset.csv"));
            TrainConfig cfg = TrainConfig.defaultConfig()
                    .setEpochs(50)
                    .setLearningRate(0.005f)
                    .setTargetVocabSize(250);

            InferenceModel model = Trainer.trainModel(samples, cfg);
            Files.createDirectories(modelPath.getParent());
            BinaryModel.save(modelPath, model);
            System.out.println("Model compilation completed.");
        }

        // 2. Load compiled binary weights into memory (0.60 calibrated threshold)
        Router router = Router.load(modelPath, 0.60);

        // 3. Bind routes directly
        router
                .bind("Refund", Main::handleRefund)
                .bind("Delivery", Main::handleDelivery)
                .bind("Account", Main::handleAccount)
                .fallback(Main::handleFallback);

        // 4. Execute microsecond branch dispatch
        List<String> testQueries = List.of(
                "I want to cancel my payment and request a refund",
                "When will my delivery package arrive",
                "Forgot my account password",
                "Please refund my purchase",
                "Track my shipment status",
                "Completely random gibberish noise 12345!@#$",
                "hey where is my stuff it was supposed to get here yesterday",
                "can u cancel order #49281? i bought it by mistake",
                "bruh the reset link is not sending to my email, fix this",
                "got charged twice on my card, refund the extra charge asap",
                "item arrived totally smashed, want my money back",
                "cant log into my acct keeps saying wrong password",
                "tracking says delivered but nothing is in my mailbox",
                "yo i typed the wrong apt number, can someone update the address before it ships",
                "sent the return box a week ago, when do i get my refund?",
                "locked out of my account after 3 tries... help pls",
                "ordered a large but you guys sent me a small",
                "any update on order #88412? hasnt moved in 4 days",
                "how do i just delete my account permanently? done with this site",
                "driver dumped the package in the rain, everything inside is ruined",
                "promo code didnt apply at checkout, can u refund the difference",
                "need a real person, this bot is completely useless",
                "can i change the delivery date? nobody will be home this friday",
                "my card was charged but never received any confirmation email or receipt",
                "lost access to my 2FA phone number, how do i get back in",
                "package has been stuck in transit for 10 days straight, is it lost or what"
        );

        System.out.println("=== IntelliBranch Server Routing Started ===");
        for (String query : testQueries) {
            router.dispatch(null, query, query);
        }
        System.out.println("=== All queries dispatched in microseconds ===");
    }
}
