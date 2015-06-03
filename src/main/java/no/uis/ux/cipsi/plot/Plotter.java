package no.uis.ux.cipsi.plot;

import java.util.List;
import java.util.Map;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.JavaPlot.Key;
import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.layout.AutoGraphLayout;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.plot.Plot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;

public class Plotter {


    static boolean addTitle = true;

    public static JavaPlot plotCandleStick(String plotDir, String plotPrefix, String plotSuffix, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap) {
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
        //        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.CANDLESTICKS);
        //        FillStyle fillStyle = new FillStyle();
        //        fillStyle.setBorder(3);
        //        fillStyle.setDensity(3);
        //        fillStyle.setPattern(3);
        //        fillStyle.setStyle(Fill.PATTERN);
        //        plotStyle.setFill(fillStyle);
        for (String dataSetName : dataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForCandleStick(dataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLable);
        plot.set("xzeroaxis", "");
        //        plot.set("boxwidth",  "0.1 relative ");
        //        plot.set("style", "data boxes");
        plot.set("style", "fill pattern 1 border");
        plot.set("xtics", "1 nomirror");
        plot.set("xrange", "[0.5:4.5]");
        plot.set("grid", "y");
        //        plot.set("datafile missing" ,"'-'");
        //        plot.set("xtics", "border in scale 0 nomirror rotate by -45  autojustify");
        if (addTitle) plot.setTitle(plotTitle);
        //        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        //        plot.setTerminal(term);
        plot.set("term", "postscript eps noenhanced color font ',20'");
        plot.set("output", "'" + plotName + "'");
        plot.plot();
        return plot;
    }

    public static JavaPlot plotBoxWithMinMax(String plotDir, String plotPrefix, String plotSuffix, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap) {
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
        //        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.BOXERRORBARS);

        for (String dataSetName : dataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetWithMinMaxForBox(dataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.set("xlabel \"" + xLable + "\"", "");
        //        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLable);
        plot.set("xzeroaxis", "");
        //        plot.set("boxwidth",  "0.1 relative ");
        plot.set("style", "data boxes");
        plot.set("style", "fill pattern 1 border");
        plot.set("xtics", "1 nomirror");
        plot.set("yrange", "[0:]");
        plot.set("grid", "y");
        plot.set("offsets", "graph 0, 0, 0.05, 0.05");

        if (addTitle) plot.setTitle(plotTitle);
        plot.set("term", "postscript eps noenhanced color font ',20'");
        plot.set("output", "'" + plotName + "'");
        plot.plot();
        return plot;
    }

    public static JavaPlot plotInverseBoxWithMinMax(String plotDir, String plotPrefix, String plotSuffix, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMapY1, String y2Lable, Map<String, List<Point<Number>>> dataPointMapY2) {
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        PlotStyle plotStyle = new PlotStyle();
        plotStyle.setStyle(Style.BOXERRORBARS);

        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        for (String dataSetName : dataPointMapY1.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY1.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(yLable+"-"+dataSetName);
            plot.addPlot(dataSetPlot);
        }

        for (String dataSetName : dataPointMapY2.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY2.get(dataSetName), true));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle("-1 * " + y2Lable + "-" + dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        //        plot.getAxis("x").setLabel(xLable);
        plot.set("xlabel \"" + xLable + "\"", "");
        plot.getAxis("y").setLabel(y2Lable + " - " + yLable);
        plot.set("xzeroaxis", "");
        plot.set("style", "data boxes");
        plot.set("style", "fill pattern 1 border");
        plot.set("xtics", "1 nomirror");
        plot.set("xrange", "[0.5:4.5]");
        plot.set("grid", "y");
        if (addTitle) plot.setTitle(plotTitle);
        //        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        //        plot.setTerminal(term);
        plot.set("term", "postscript eps noenhanced color");
        plot.set("output", "'" + plotName + "'");
        plot.plot();
        return plot;
    }

    public static JavaPlot plotBox(String plotDir, String plotPrefix, String plotSuffix, String plotTitle, String xLable, String xTicsLabel, String yLable, String arbitaryText, Map<String, List<Point<Number>>> dataPointMap, Key key) {
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
        //        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.BOXES);

        for (String dataSetName : dataPointMap.keySet()) {

            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForBox(dataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(key);
        // NOTE: escaping \' to properly read \n in the input
        //        plot.getAxis("x").setLabel(xLable);
        plot.set("xlabel \"" + xLable + "\"", "");
        plot.getAxis("y").setLabel(yLable);

        //        plot.set("label 1\"", arbitaryText+"\n");
        //        plot.set("label", "1 at graph  0.02, 0.85");
        plot.set("xzeroaxis", "");
        //        plot.set("boxwidth",  "0.1 relative ");
        plot.set("style", "data boxes");
        plot.set("style", "fill pattern 1 border");
        plot.set("xtics", "1 nomirror (" + xTicsLabel + ")");
        plot.set("offsets", "graph 0, 0, 0.005, 0.05");
        //        plot.set("xrange", "[0.5:4.5]");
        plot.set("grid", "y");
        //        plot.set("xtics", "border in scale 0 nomirror rotate by -45  autojustify");
        if (addTitle) plot.setTitle(plotTitle);
        //        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        //        plot.setTerminal(term);
        plot.set("term", "postscript eps noenhanced color");
        plot.set("output", "'" + plotName + "'");
        plot.plot();
        return plot;
    }

    public static JavaPlot plotBoxWithOverLappingPoints(String plotDir,
            String plotPrefix,
            String plotSuffix,
            String plotTitle,
            String xLable,
            String xTicsLabel,
            String yLabel,
            String y2Label,
            String y3Label,
            String y4Label,
            String arbitaryText,
            Map<String, List<Point<Number>>> boxDataPointMap,
            Map<String, List<Point<Number>>> point2DataPointMap,
            Map<String, List<Point<Number>>> point3DataPointMap,
            Map<String, List<Point<Number>>> point4DataPointMap,Key key) {

        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyleY1 = new PlotStyle();
        plotStyleY1.setStyle(Style.BOXES);
        //        FillStyle fs = new FillStyle(Fill.EMPTY);
        //        fs.setPattern(1);
        //        plotStyleY1.setFill(fs);

        PlotStyle plotStyleY2 = new PlotStyle();
        plotStyleY2.setStyle(Style.POINTS);
        plotStyleY2.setPointType(1);
        //        plotStyleY2.setPointSize(1);
        plotStyleY2.setLineType(0);
        plotStyleY2.setLineWidth(4);

        PlotStyle plotStyleY3 = new PlotStyle();
        plotStyleY3.setStyle(Style.POINTS);
        plotStyleY3.setPointType(2);
        plotStyleY3.setLineType(0);
        plotStyleY3.setLineWidth(4);

        PlotStyle plotStyleY4 = new PlotStyle();
        plotStyleY4.setStyle(Style.POINTS);
        plotStyleY4.setPointType(3);
        plotStyleY4.setLineType(0);
        plotStyleY4.setLineWidth(4);

        for (String dataSetName : boxDataPointMap.keySet()) {

            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForBox(boxDataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY1);
            dataSetPlot.setTitle(dataSetName);
            plot.addPlot(dataSetPlot);
        }

        // Yet another horrible hack. Remove it for a proper DS.
        boolean firstDs = true;
        for (String dataSetName : point2DataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForBox(point2DataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY2);
            if (firstDs) {
                dataSetPlot.setTitle(y2Label);
                firstDs = false;
            } else {
                dataSetPlot.setTitle("");
            }
            plot.addPlot(dataSetPlot);
        }
        firstDs = true;
        for (String dataSetName : point3DataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForBox(point3DataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY3);
            if (firstDs) {
                dataSetPlot.setTitle(y3Label);
                firstDs = false;
            } else {
                dataSetPlot.setTitle("");
            }
            plot.addPlot(dataSetPlot);
        }
        firstDs = true;
        for (String dataSetName : point4DataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForBox(point4DataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY4);
            if (firstDs) {
                dataSetPlot.setTitle(y4Label);
                firstDs = false;
            } else {
                dataSetPlot.setTitle("");
            }
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(key);
        // NOTE: escaping \' to properly read \n in the input
        plot.set("xlabel \"" + xLable + "\"", "");
        //        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLabel);

        //        plot.set("label 1\"", arbitaryText+"\n");
        //        plot.set("label", "1 at graph  0.02, 0.85");
        plot.set("xzeroaxis", "");
        //        plot.set("boxwidth",  "0.1 relative ");
        plot.set("style", "data boxes");
        plot.set("style", "fill pattern 1 border");
        plot.set("xtics", "1 nomirror (" + xTicsLabel + ")");
        plot.set("ytics", "nomirror");
        plot.set("grid", "y");

        if (addTitle) plot.setTitle(plotTitle);
        plot.set("term", "postscript eps noenhanced color");
        plot.set("output", "'" + plotName + "'");
        plot.plot();
        return plot;
    }

    public static JavaPlot plotBoxWithOverLappingPointsOnY2(String plotDir,
            String plotPrefix,
            String plotSuffix,
            String plotTitle,
            String xLable,
            String xTicsLabel,
            String yLabel,
            String y2Label,
            String point2Label,
            String point3Label,
            String point4Label,
            String arbitaryText,
            Map<String, List<Point<Number>>> boxDataPointMap,
            Map<String, List<Point<Number>>> point2DataPointMap,
            Map<String, List<Point<Number>>> point3DataPointMap,
            Map<String, List<Point<Number>>> point4DataPointMap,Key key) {

        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyleY1 = new PlotStyle();
        plotStyleY1.setStyle(Style.BOXES);
        //        FillStyle fs = new FillStyle();
        //        fs.setStyle(Fill.PATTERN);
        //        fs.setPattern(0);
        //        plotStyleY1.setFill(fs);

        PlotStyle plotStyleY2 = new PlotStyle();
        plotStyleY2.setStyle(Style.POINTS);
        plotStyleY2.setPointType(1);
        plotStyleY2.setLineType(0);
        plotStyleY2.setLineWidth(4);

        PlotStyle plotStyleY3 = new PlotStyle();
        plotStyleY3.setStyle(Style.POINTS);
        plotStyleY3.setPointType(2);
        plotStyleY3.setLineType(0);
        plotStyleY3.setLineWidth(4);

        PlotStyle plotStyleY4 = new PlotStyle();
        plotStyleY4.setStyle(Style.POINTS);
        plotStyleY4.setPointType(3);
        plotStyleY4.setLineType(0);
        plotStyleY4.setLineWidth(4);

        for (String dataSetName : boxDataPointMap.keySet()) {

            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForBox(boxDataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY1);
            dataSetPlot.setTitle(dataSetName);
            dataSetPlot.set("axes", "x1y1");
            plot.addPlot(dataSetPlot);
        }

        // Yet another horrible hack. Remove it for a proper DS.
        boolean firstDs = true;
        for (String dataSetName : point2DataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForBox(point2DataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY2);
            if (firstDs) {
                dataSetPlot.setTitle(point2Label);
                firstDs = false;
            } else {
                dataSetPlot.setTitle("");
            }
            //            dataSetPlot.setTitle(dataSetName + "-" + point2Label);
            dataSetPlot.set("axes", "x1y2");
            plot.addPlot(dataSetPlot);
        }
        firstDs = true;
        for (String dataSetName : point3DataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForBox(point3DataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY3);
            if (firstDs) {
                dataSetPlot.setTitle(point3Label);
                firstDs = false;
            } else {
                dataSetPlot.setTitle("");
            }
            //            dataSetPlot.setTitle(dataSetName + "-" + point3Label);
            dataSetPlot.set("axes", "x1y2");
            plot.addPlot(dataSetPlot);
        }
        firstDs = true;
        for (String dataSetName : point4DataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetForBox(point4DataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY4);
            if (firstDs) {
                dataSetPlot.setTitle(point4Label);
                firstDs = false;
            } else {
                dataSetPlot.setTitle("");
            }
            //            dataSetPlot.setTitle(dataSetName + "-" + point4Label);
            dataSetPlot.set("axes", "x1y2");
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(Key.BELOW);
        plot.set("xlabel \"" + xLable + "\"", "");
        plot.getAxis("y").setLabel(yLabel);

        //        plot.set("label 1\"", arbitaryText+"\n");
        //        plot.set("label", "1 at graph  0.02, 0.85");
        plot.set("xzeroaxis", "");
        plot.set("style", "data boxes");
        plot.set("style", "fill pattern 1 border");
        plot.set("xtics", "1 nomirror (" + xTicsLabel + ")");
        plot.set("yrange", "[0:*]");
        plot.set("ytics", "nomirror");
        //        plot.set("grid", "y");
        plot.set("y2label", "'"+y2Label+"'");
        plot.set("y2tics", "5");
        //        plot.set("offsets", "graph -0.05, 0.05, 0.05, 0.05");
        if (addTitle) plot.setTitle(plotTitle);
        plot.set("term", "postscript eps noenhanced color");
        plot.set("output", "'" + plotName + "'");
        plot.plot();
        return plot;
    }

    public static JavaPlot plotErrorLines(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap) {
        return plot(plotName, plotTitle, xLable, yLable, dataPointMap, Style.ERRORLINES);
    }

    public static JavaPlot plot(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap, Style style) {
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
        //        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(style);

        for (String dataSetName : dataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetWithMinMaxForBox(dataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLable);
        plot.set("xzeroaxis", "");
        if (addTitle) plot.setTitle(plotTitle);
        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        plot.setTerminal(term);
        plot.plot();
        return plot;
    }

    public static JavaPlot plotInverse(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMapY1, String y2Lable, Map<String, List<Point<Number>>> dataPointMapY2) {
        JavaPlot plot = new JavaPlot();
        PlotStyle plotStyle = new PlotStyle();
        //        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.ERRORLINES);

        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        for (String dataSetName : dataPointMapY1.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY1.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(yLable+"-"+dataSetName);
            plot.addPlot(dataSetPlot);
        }

        for (String dataSetName : dataPointMapY2.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY2.get(dataSetName), true));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle("-1 * " + y2Lable + "-" + dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(y2Lable + " - " + yLable);
        plot.set("xzeroaxis", "");
        if (addTitle) plot.setTitle(plotTitle);
        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        plot.setTerminal(term);
        plot.plot();
        return plot;
    }

    private static JavaPlot plotY1Y2(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMapY1, String y2Lable, Map<String, List<Point<Number>>> dataPointMapY2) {
        JavaPlot plot = new JavaPlot();
        PlotStyle plotStyle = new PlotStyle();
        //        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.ERRORLINES);

        PlotStyle plotStyleY2 = new PlotStyle();
        plotStyleY2.setStyle(Style.LINESPOINTS);
        plotStyleY2.setLineWidth(5);
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        for (String dataSetName : dataPointMapY1.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY1.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(yLable+"-"+dataSetName);
            dataSetPlot.set("axes", "x1y1");
            plot.addPlot(dataSetPlot);
        }

        for (String dataSetName : dataPointMapY2.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY2.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY2);
            dataSetPlot.setTitle(y2Lable + "-" + dataSetName);
            dataSetPlot.set("axes", "x1y2");
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLable);
        plot.set("y2label", "'"+y2Lable+"'");

        plot.set("y2tics", "");
        plot.set("ytics", "nomirror");
        //        plot.set("autoscale", "y2");
        //        plot.set("autoscale", "y");
        //        plot.set("xrange", "[0:32]");
        //        plot.set("y2range", "[0:100]");
        //        plot.set("yrange", "[0:100]");
        //        plot.set("grid", "x y y2");
        if (addTitle) plot.setTitle(plotTitle);
        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        plot.setTerminal(term);
        plot.plot();
        return plot;
    }

    public static void plotMultipleBoxPlots(String plotDir, String plotPrefix, String plotSuffix, String arbitraryText, List<JavaPlot> plots) {
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;

        PlotStyle plotStyle = new PlotStyle();
        plotStyle.setStyle(Style.ERRORLINES);

        JavaPlot allPlot = new JavaPlot();
        allPlot.getDebugger().setLevel(allPlot.getDebugger().VERBOSE);

        allPlot.set("term", "postscript eps size 11.7in,16.5in noenhanced color");
        allPlot.set("output", "'" + plotName + "'");
        AutoGraphLayout layout = new AutoGraphLayout();
        layout.setColumns(2);
        layout.setRows(7);
        allPlot.getPage().setLayout(layout);
        allPlot.set("xzeroaxis", "");
        allPlot.set("style", "fill pattern 1 border");
        allPlot.set("xtics", "1 nomirror");
        //        allPlot.set("xrange", "[0:5]");
        allPlot.set("grid", "y");
        allPlot.set("offsets", "graph 0, 0, 0.005, 0.05");
        //        allPlot.set("label 1\"", arbitraryText+"\n");
        //        allPlot.set("label", "1 at screen  -1 -1");
        allPlot.setKey(Key.BELOW);
        for (JavaPlot javaPlot : plots) {
            for (Plot dsp : javaPlot.getPlots()) {
                allPlot.addPlot(dsp);
            }
            //            allPlot.getAxis("x").setLabel(javaPlot.getAxis("x").get("xlabel").split("'")[1]);
            //            System.out.println(javaPlot.getParameters().get("y2label"));
            String y2label;
            if (javaPlot.getParameters().get("y2label") != null){
                y2label = "' \n set y2label " + javaPlot.getParameters().get("y2label");
            } else {
                y2label = "' \n set y2label ";
            }
            // Dirty hack to reset title
            if (addTitle) allPlot.getAxis("y").setLabel(javaPlot.getAxis("y").get("ylabel").split("'")[1]  + "' \n set title '" + javaPlot.getParameters().get("title").split("'")[1] +  y2label);
            allPlot.newGraph();
        }
        allPlot.setMultiTitle("All Plots\\n"+ arbitraryText);
        allPlot.plot();
    }

    public static JavaPlot plotSimple(String plotDir, String plotPrefix, String plotSuffix, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap){
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        //        plotStyle.setStyle(Style.BOXERRORBARS);
        int lt = 6;
        for (String dataSetName : dataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(PlotUtils.getSortedPointDataSet(dataPointMap.get(dataSetName), false));
            lt = lt+1;
            PlotStyle plotStyle = new PlotStyle();
            plotStyle.setStyle(Style.LINES);
            plotStyle.setLineWidth(3);
            plotStyle.setLineType(lt);
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLable);
        plot.set("xzeroaxis", "");
        //        plot.set("boxwidth",  "0.1 relative ");
        //        plot.set("style", "data boxes");
        //        plot.set("style", "line 1 linewidth 3");
        //        plot.set("xtics", "1 nomirror");
        //        plot.set("xrange", "[0.5:4.5]");
        plot.set("grid", "y");
        //        plot.set("datafile missing" ,"'-'");
        //        plot.set("xtics", "border in scale 0 nomirror rotate by -45  autojustify");
        if (addTitle) plot.setTitle(plotTitle);
        //        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        //        GNUPlotTerminal term = new DefaultTerminal();
        //        plot.setTerminal(term);
        //        plot.set("termoption", "lw 3");
        plot.set("term", "postscript eps noenhanced color font ',20'");
        plot.set("output", "'" + plotName + "'");
        plot.plot();
        return plot;
    }
}
