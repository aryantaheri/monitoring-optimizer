package no.uis.ux.cipsi.plot;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.stats.MonitoringStats;
import no.uis.ux.cipsi.net.monitoringbalancing.persistence.MonitoringBalanceFileIO;
import no.uis.ux.cipsi.net.monitoringbalancing.util.ConfigUtil;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panayotis.gnuplot.dataset.Point;


public class ProcessMonitoringSolutions {

    private static Logger log = LoggerFactory.getLogger(ProcessMonitoringSolutions.class);
    private static Map<String, Map<String, MonitoringStats>> statsMap = new TreeMap<String, Map<String,MonitoringStats>>();

    private static Map<String, List<Point<Number>>> swCountDataPointMap = new TreeMap<String, List<Point<Number>>>();

    private static final double BOX_WIDTH = 0.15;

    public static void main(String[] args) {
        String dir = "/run/user/1000/gvfs/sftp:host=badne8.ux.uis.no/import/br1raid6a1h2/stud/aryan/workspace/monitoring/monitoring-optimizer/local/data/monitoringbalancing/2015-04-07_150528/";
        loadSolutions(dir);
        prepareDataSets();
        plotDataSets("/tmp");
    }

    private static void loadSolutions(String dirPath) {
        FileFilter solutionDirFilter = new WildcardFileFilter(ConfigUtil.getOutPutSolutionWildCard());
        File dir = new File(dirPath);
        File[] outPutSolutionDirs = dir.listFiles(solutionDirFilter);
        log.info("Solutions Dir={}", dir);
        FileFilter localSearchDirFilter = DirectoryFileFilter.DIRECTORY;
        FileFilter solutionObjFile = new WildcardFileFilter(ConfigUtil.getOutPutSolutionWildCard()+".obj");

        for (File solutionDir : outPutSolutionDirs) {
            System.out.println(solutionDir.getName());
            File[] localSearchDirs = solutionDir.listFiles(localSearchDirFilter);
            for (File localSearchDir : localSearchDirs) {
                System.out.println("    "+localSearchDir.getName());
                File[] solutions = localSearchDir.listFiles(solutionObjFile);
                if (solutions.length > 1) System.err.println("More than one solution file found.");
                MonitoringStats stats = MonitoringBalanceFileIO.readStats(solutions[0]);
                addToMap(solutionDir.getName(), localSearchDir.getName(), stats);
            }
            System.out.println();
        }
    }

    private static void addToMap(String inputName, String localSearchName, MonitoringStats stats) {
        Map<String, MonitoringStats> lsMap = statsMap.get(inputName);
        if (lsMap == null) {
            lsMap = new TreeMap<String, MonitoringStats>();
        }
        MonitoringStats oldStats = lsMap.get(localSearchName);
        if (oldStats != null) {
            log.error("addToMap: overwriting existing solution {} with new one {}", oldStats, stats);
        }
        lsMap.put(localSearchName, stats);
        statsMap.put(inputName, lsMap);
    }

    private static void prepareDataSets() {

        for (Entry<String, Map<String, MonitoringStats>> inputSolutionEntry : statsMap.entrySet()) {
            int x = getXIndex(statsMap.keySet(), inputSolutionEntry.getKey());
            for (Entry<String, MonitoringStats> lsSolutionEntry : inputSolutionEntry.getValue().entrySet()) {
                double xOffset = getXOffset(lsSolutionEntry.getKey());

                int swNum = lsSolutionEntry.getValue().getSwitchStats().getMonitoringSwitchesNum();
                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, swNum, swCountDataPointMap);
            }
        }

    }

    private static void plotDataSets(String plotDir) {
        Plotter.plotBox(plotDir, "prefix", "MonSw.eps", "Number of Switches", "Inputs\\n" + getXTicLabels()+ "" , "#MonSw", swCountDataPointMap);
    }

    // TODO: Make it once and reuse it
    private static int getXIndex(Set<String> inputNames, String inputSolutionName) {
        ArrayList<String> inputs = new ArrayList<String>(inputNames);
        Collections.sort(inputs);
        return inputs.indexOf(inputSolutionName);
    }

    // TODO: Make it once and reuse it
    private static double getXOffset(String lsName) {
        Set<String> lsNames = new HashSet<String>();
        for (Map<String, MonitoringStats> values : statsMap.values()) {
            lsNames.addAll(values.keySet());
        }

        ArrayList<String> localSearches = new ArrayList<String>(lsNames);
        Collections.sort(localSearches);
        int index = localSearches.indexOf(lsName);
        double offset = (index - localSearches.size()/2) * BOX_WIDTH;
        return offset;
    }

    private static String getXTicLabels() {
        StringBuilder xtics = new StringBuilder();
        ArrayList<String> inputs = new ArrayList<String>(statsMap.keySet());
        Collections.sort(inputs);
        for (int i = 0; i < inputs.size(); i++) {
            xtics.append(i).append(" - ").append(inputs.get(i)).append("");
        }

        return xtics.toString();
    }
}
