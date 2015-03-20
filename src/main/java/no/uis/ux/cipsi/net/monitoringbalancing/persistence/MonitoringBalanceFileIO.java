package no.uis.ux.cipsi.net.monitoringbalancing.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import no.uis.ux.cipsi.net.monitoringbalancing.app.MonitoringBalancingHelloWorld;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;

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

        return new MonitoringBalancingGenerator().createMonitoringBalance(false);
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
        String analysis = MonitoringBalancingHelloWorld.toDisplayString((MonitoringBalance) solution);
        log.info("write analysis:\n {}", analysis);
    }

}
