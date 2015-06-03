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

import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.stats.MonitoringStats;
import no.uis.ux.cipsi.net.monitoringbalancing.persistence.MonitoringBalanceFileIO;
import no.uis.ux.cipsi.net.monitoringbalancing.util.ConfigUtil;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.Point;


public class ProcessMonitoringSolutions {

    private static Logger log = LoggerFactory.getLogger(ProcessMonitoringSolutions.class);
    private static Map<String, Map<String, MonitoringStats>> statsMap = new TreeMap<String, Map<String,MonitoringStats>>();

    private static Map<String, List<Point<Number>>> swCountDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> swEdgeCountDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> swAggrCountDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> swCoreCountDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> swEdgeReuseDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> swAggrReuseDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> swCoreReuseDataPointMap = new TreeMap<String, List<Point<Number>>>();

    private static Map<String, List<Point<Number>>> swReuseDataPointMap = new TreeMap<String, List<Point<Number>>>();

    private static Map<String, List<Point<Number>>> hostCountDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> hostReuseDataPointMap = new TreeMap<String, List<Point<Number>>>();

    private static Map<String, List<Point<Number>>> swHostDistanceDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> hostSwDistanceDataPointMap = new TreeMap<String, List<Point<Number>>>();

    private static Map<String, List<Point<Number>>> solutionHardCostDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> solutionSoftCostDataPointMap = new TreeMap<String, List<Point<Number>>>();

    private static Map<String, List<Point<Number>>> nullSwDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> nullHostDataPointMap = new TreeMap<String, List<Point<Number>>>();
    private static Map<String, List<Point<Number>>> nullPathDataPointMap = new TreeMap<String, List<Point<Number>>>();

    private static Map<String, List<Point<Number>>> flowCountDataPointMap = new TreeMap<String, List<Point<Number>>>();

    private static final double BOX_WIDTH = 0.15;

    public static void main(String[] args) {
        //        String dir = "/run/user/1000/gvfs/sftp:host=badne8.ux.uis.no/import/br1raid6a1h2/stud/aryan/workspace/monitoring/monitoring-optimizer/local/data/monitoringbalancing/2015-04-07_150528/";
        //        String dir = "/run/user/1000/gvfs/sftp:host=badne8.ux.uis.no/import/br1raid6a1h2/stud/aryan/workspace/monitoring/monitoring-optimizer/local/data/monitoringbalancing/2015-04-09_172929-120min-good-k8/";
        String dir = "/home/aryan/University/DC/Monitoring/data/2015-06-03_121514";
        loadSolutions(dir);
        prepareDataSets();
        plotDataSets(dir);
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

                // FIXME: remove this
                //                Solution solution = MonitoringBalanceFileIO.readSolution(solutions[0]);
                //                MonitoringBalanceFileIO.writeStats(solution, new File(solutions[0].getAbsolutePath()+"-recovered-stats.txt"));
                if (stats == null){
                    log.error("MonitoringStats is null.");
                    continue;
                }
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
                MonitoringStats stats = lsSolutionEntry.getValue();

                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchStats().getMonitoringSwitchesNum(), swCountDataPointMap);

                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchStats().getSwitchCountInLayer(TYPE.EDGE), swEdgeCountDataPointMap);
                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchStats().getSwitchCountInLayer(TYPE.AGGREGATION), swAggrCountDataPointMap);
                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchStats().getSwitchCountInLayer(TYPE.CORE), swCoreCountDataPointMap);

                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchStats().getSwitchReuseInLayer(TYPE.EDGE), swEdgeReuseDataPointMap);
                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchStats().getSwitchReuseInLayer(TYPE.AGGREGATION), swAggrReuseDataPointMap);
                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchStats().getSwitchReuseInLayer(TYPE.CORE), swCoreReuseDataPointMap);

                PlotUtils.addDataPointsWithMinMax(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchStats().getSwitchReuseStats(), swReuseDataPointMap);

                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getHostStats().getMonitoringHostNum(), hostCountDataPointMap);
                PlotUtils.addDataPointsWithMinMax(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getHostStats().getReuseStats(), hostReuseDataPointMap);

                PlotUtils.addDataPointsWithMinMax(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchHostStats().getSwitchHostDistanceStats(), swHostDistanceDataPointMap);
                PlotUtils.addDataPointsWithMinMax(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchHostStats().getHostSwitchDistanceStats(), hostSwDistanceDataPointMap);

                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getScore().getHardScore().doubleValue(), solutionHardCostDataPointMap);
                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getScore().getSoftScore().doubleValue(), solutionSoftCostDataPointMap);

                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchStats().getNullSwitches(), nullSwDataPointMap);
                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getHostStats().getNullHosts(), nullHostDataPointMap);
                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getSwitchHostStats().getNullMonitoringPaths(), nullPathDataPointMap);

                PlotUtils.addBoxDataPoint(lsSolutionEntry.getKey(), x, xOffset, BOX_WIDTH, stats.getFlowStats().getFlowCount(), flowCountDataPointMap);

            }
        }
    }

    private static void plotDataSets(String plotDir) {
        List<JavaPlot> plots = new ArrayList<JavaPlot>();
        JavaPlot plot = null;
        plot = Plotter.plotBox(plotDir, "", "SoftCost.eps", "Soft Cost", "Inputs\\n" + getXTicLabels(), "",  "Soft Cost", "", solutionSoftCostDataPointMap, JavaPlot.Key.BOTTOM_LEFT);
        plots.add(plot);
        plot = Plotter.plotBox(plotDir, "", "HardCost.eps", "Hard Cost", "Inputs\\n" + getXTicLabels(), "",  "Hard Cost", "", solutionHardCostDataPointMap, JavaPlot.Key.BOTTOM_LEFT);
        plots.add(plot);
        plot = Plotter.plotBox(plotDir, "", "MonSw.eps", "Number of Switches", "Inputs\\n" + getXTicLabels(), "",  "#MonSw", "", swCountDataPointMap, JavaPlot.Key.TOP_RIGHT);
        plots.add(plot);
        plot = Plotter.plotBox(plotDir, "", "MonHost.eps", "Number of Hosts", "Inputs\\n" + getXTicLabels(), "",  "#MonHost", "", hostCountDataPointMap, JavaPlot.Key.TOP_RIGHT);
        plots.add(plot);

        plot = Plotter.plotBoxWithOverLappingPoints(plotDir, "",
                "MonSwLayerCount.eps", "Monitoring Switches And Network Layer Distribution", "Inputs\\n"
                        + getXTicLabels(), "", "#MonSw", "Edge", "Aggregation",
                        "Core", "", swCountDataPointMap, swEdgeCountDataPointMap,
                        swAggrCountDataPointMap, swCoreCountDataPointMap,
                        JavaPlot.Key.TOP_RIGHT);

        plots.add(plot);

        plot = Plotter.plotBoxWithOverLappingPointsOnY2(plotDir, "",
                "MonSwLayerReuse.eps", "Monitoring Switches and Total Switch Resuse in Layers", "Inputs\\n"
                        + getXTicLabels(), "", "#MonSw", "Total Switch Reuse in Layer", "Edge", "Aggregation",
                        "Core", "", swCountDataPointMap, swEdgeReuseDataPointMap,
                        swAggrReuseDataPointMap, swCoreReuseDataPointMap,
                        JavaPlot.Key.TOP_RIGHT);

        plots.add(plot);

        plot = Plotter.plotBoxWithMinMax(plotDir, "", "MonSwReuse.eps", "Monitoring Switch Reuse Stats", "Inputs\\n"
                + getXTicLabels(), "#Reuse", swReuseDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBoxWithMinMax(plotDir, "", "MonHostReuse.eps", "Monitoring Host Reuse Stats", "Inputs\\n"
                + getXTicLabels(), "#Reuse", hostReuseDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBoxWithMinMax(plotDir, "", "MonSwHostDistance.eps", "Distance Between Monitoring Switch and Host", "Inputs\\n"
                + getXTicLabels(), "Distance", swHostDistanceDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBoxWithMinMax(plotDir, "", "MonHostSwDistance.eps", "Distance Between Monitoring Host and Switch", "Inputs\\n"
                + getXTicLabels(), "Distance", hostSwDistanceDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBox(plotDir, "", "nullSw.eps", "Null Switches", "Inputs\\n" + getXTicLabels(), "",  "Null Switches", "", nullSwDataPointMap, JavaPlot.Key.TOP_RIGHT);
        plots.add(plot);
        plot = Plotter.plotBox(plotDir, "", "nullHost.eps", "Null Hosts", "Inputs\\n" + getXTicLabels(), "",  "Null Hosts", "", nullHostDataPointMap, JavaPlot.Key.TOP_RIGHT);
        plots.add(plot);
        plot = Plotter.plotBox(plotDir, "", "nullPaths.eps", "Null Paths (MonSw != null && MonHost != null)", "Inputs\\n" + getXTicLabels(), "",  "Null Paths (MonSw != null && MonHost != null)", "", nullPathDataPointMap, JavaPlot.Key.TOP_RIGHT);
        plots.add(plot);

        plot = Plotter.plotBox(plotDir, "", "flowCount.eps", "Flow Size", "Inputs\\n" + getXTicLabels(), "",  "Flow Size", "", flowCountDataPointMap, JavaPlot.Key.TOP_RIGHT);
        plots.add(plot);

        Plotter.plotMultipleBoxPlots(plotDir, "", "all.eps", getXTicLabels(), plots);
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
            xtics.append(i).append(" - ").append(inputs.get(i)).append("\\n");
        }

        return xtics.toString();
        //        return "";
    }

    private static String getXTicLabelsInGnuPlot() {
        StringBuilder xtics = new StringBuilder();
        ArrayList<String> inputs = new ArrayList<String>(statsMap.keySet());
        Collections.sort(inputs);
        for (int i = 0; i < inputs.size(); i++) {
            xtics.append("\"").append(inputs.get(i)).append("\" ").append(i).append(", ");
        }
        return xtics.substring(0, xtics.length() -2 ).toString();
    }
}
