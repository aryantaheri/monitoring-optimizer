package no.uis.ux.cipsi.net.monitoringbalancing.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.stats.MonitoringStats;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.stats.MonitoringStatsManager;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import no.uis.ux.cipsi.net.monitoringbalancing.util.TxtFileUtil;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringBalanceFileIO implements SolutionFileIO {

    private static Logger log = LoggerFactory.getLogger(MonitoringBalanceFileIO.class);

    public static final String INPUT_EXTENSION = "txt";
    public static final String OUTPUT_EXTENSION = "obj";

    @Override
    public String getInputFileExtension() {
        return INPUT_EXTENSION;
    }

    @Override
    public String getOutputFileExtension() {
        return OUTPUT_EXTENSION;
    }

    @Override
    public Solution read(File inputSolutionFile) {
        Configs configs = TxtFileUtil.getConfigurations(inputSolutionFile);
        return new MonitoringBalancingGenerator().createMonitoringBalance(configs, false);
    }

    @Override
    public void write(Solution solution, File outputSolutionFile) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(outputSolutionFile));
            oos.writeObject(solution);
        } catch (IOException e) {
            log.error("write", e);
        } finally {
            if (oos != null){
                try {
                    oos.close();
                } catch (IOException e) {
                    log.error("write", e);
                }
            }
        }
        log.info("write: generated solutionFile: {}", outputSolutionFile);
        // FIXME: dummy stuff fix these:
        XStreamSolutionFileIO io = new XStreamSolutionFileIO(MonitoringBalance.class);
        io.write(solution, new File(outputSolutionFile.getAbsoluteFile()+".xml"));
        writeStats(solution, new File(outputSolutionFile.getAbsoluteFile()+"-stats.txt"));
    }

    private void writeStats(Solution solution, File file) {
        MonitoringStats stats = MonitoringStatsManager.getStats((MonitoringBalance) solution);
        log.info("write stats to file {}:\n {}", file, stats);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            writer.print("\n Solution Cost: ");
            writer.println(solution.getScore());

            writer.println("----------------------------");
            writer.println("Solution Problems Facts:");
            writer.println(solution.getProblemFacts());

            writer.println("----------------------------");
            writer.println("Monitoring Stats:");
            writer.println(stats.getSwitchStats().getDetailedString());
            writer.println(stats.getHostStats().getDetailedString());
            writer.println(stats.getSwitchHostStats().toString());
            writer.println(stats.getFlowStats().toString());

            writer.println("----------------------------");
            writer.println("Monitoring Facts:");
            writer.println(stats.getSwitchStats().getFactsString());
            writer.println(stats.getHostStats().getFactsString());
            writer.println(stats.getFlowStats().getFactsString());
            writer.println(stats.getSwitchHostStats().getLinkFactsString());
            writer.flush();
        } catch (IOException e) {
            log.error("writeStats", e);
        } finally {
            if (writer != null) writer.close();
        }
    }

}
