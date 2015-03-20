package no.uis.ux.cipsi.net.monitoringbalancing.app;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

public class MonitoringBalancingBenchmark {

    public static void main(String[] args) {
        // Build the PlannerBenchmark
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "monitoringbalancing/benchmark/monitoringBalancingBenchmarkConfig.xml");
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();

        // Benchmark the problem
        plannerBenchmark.benchmark();

        // Show the benchmark report
        System.out.println("\nPlease open the benchmark report in:  \n"
                + plannerBenchmarkFactory.getPlannerBenchmarkConfig().getBenchmarkDirectory().getAbsolutePath());
    }
}
