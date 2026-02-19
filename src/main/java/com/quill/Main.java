package com.quill;

import com.quill.api.Logger;
import com.quill.api.LoggerFactory;
import com.quill.appender.Appenders;
import com.quill.config.Logging;
import com.quill.context.LogContext;
import com.quill.model.Level;

import java.io.File;

/**
 * Demo application for the Quill logging library.
 */
public class Main {
    static void main(String[] args) {
        Logger log = LoggerFactory.get(Main.class);

        // ========================================
        // Phase 4: Sampling
        // ========================================
        System.out.println("=== Phase 4: Sampling ===");

        // Configure with 50% sampling rate for DEBUG/TRACE logs
        Logging.configure(config -> config
                .level(Level.DEBUG)           // Enable DEBUG globally
                .sampling(0.5)                // Sample 50% of DEBUG/TRACE logs
                .appender(Appenders.jsonConsole())
        );

        System.out.println("Logging 100 debug messages with 50% sampling...");
        for (int i = 0; i < 100; i++) {
            log.debug(STR."Debug message \{i}", "index", i);
            // Count actual outputs (we can't directly count, so we'll estimate)
        }
        // Approximately 50 messages should appear (due to 50% sampling)

        // ========================================
        // Phase 4: Package-Level Filtering
        // ========================================
        System.out.println("\n=== Phase 4: Package-Level Filtering ===");

        // Configure different levels for different packages
        Logging.configure(config -> config
                .level(Level.INFO)                    // Global INFO
                .packageLevel("com.quill", Level.DEBUG)  // DEBUG for com.quill package
                .packageLevel("com.external", Level.WARN) // WARN for external packages
                .appender(Appenders.console(true))
        );

        Logger quillLogger = LoggerFactory.get("com.quill.MyService");
        Logger externalLogger = LoggerFactory.get("com.external.ApiClient");

        // com.quill.MyService logs at DEBUG level (overridden)
        quillLogger.debug("This DEBUG log appears because com.quill is set to DEBUG");
        quillLogger.trace("This TRACE does NOT appear (below DEBUG)");

        // com.external.ApiClient logs at WARN level (overridden)
        externalLogger.debug("This DEBUG does NOT appear because com.external is WARN");
        externalLogger.info("This INFO does NOT appear because com.external is WARN");
        externalLogger.warn("This WARN appears because com.external is set to WARN");

        // ========================================
        // Phase 4: Everything Combined
        // ========================================
        System.out.println("\n=== Phase 4: Everything Combined ===");

        Logging.configure(config -> config
                .level(Level.INFO)
                .packageLevel("com.quill", Level.TRACE)  // Very verbose for our package
                .sampling(0.2)                             // Sample 80% of TRACE/DEBUG
                .appender(Appenders.jsonConsole())
        );

        LogContext.bind("demoPhase", "phase4")
                .and("sampling", "20%")
                .run(() -> {
                    log.traceEmit("TRACE message (subject to sampling)");
                    log.debugEmit("DEBUG message (subject to sampling)");
                    log.infoEmit("INFO message (NEVER sampled)");
                    log.warnEmit("WARN message (NEVER sampled)");
                    log.errorEmit("ERROR message (NEVER sampled)");
                });

        // ========================================
        // Full Feature Demo
        // ========================================
        System.out.println("\n=== Full Feature Demo: All Phases ===");

        // Configure with all features
        Logging.configure(config -> config
                .level(Level.INFO)
                .packageLevel("com.quill", Level.DEBUG)
                .sampling(0.5)
                .appender(Appenders.chain(
                        Appenders.console(true),
                        Appenders.file("logs/full-demo.log")
                ))
        );

        Logger serviceLogger = LoggerFactory.get("com.quill.PaymentService");

        LogContext.bind("requestId", "final-req-123")
                .and("userId", 9999)
                .run(() -> {
                    serviceLogger.debug("Processing payment");  // 50% sampling applies
                    serviceLogger.info("Payment initiated");    // Always logged
                    serviceLogger.warn("High latency detected", "ms", 1500);
                    serviceLogger.error("Payment gateway timeout", "gateway", "stripe");
                });

        System.out.println(STR."""

Check \{new File("logs/full-demo.log").getAbsolutePath()} for JSON output""");

        System.out.println("\n=== Quill Demo Complete ===");
        System.out.println("All 4 phases implemented:");
        System.out.println("  Phase 1: Core (Fluent API, JSON output, Levels)");
        System.out.println("  Phase 2: Context (Scoped context, Virtual thread support)");
        System.out.println("  Phase 3: Appenders (File, Rolling, Async, Chaining)");
        System.out.println("  Phase 4: Polish (Sampling, Package filtering)");
    }
}
