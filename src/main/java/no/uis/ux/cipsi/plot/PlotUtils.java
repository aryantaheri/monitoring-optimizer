package no.uis.ux.cipsi.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.dataset.PointDataSet;

public class PlotUtils {

    public static Map<String, List<Point<Number>>> addDataPoint(String dataSetName, int x, double y, Map<String, List<Point<Number>>> dataPointMap) {
        List<Point<Number>> dataPointList = dataPointMap.get(dataSetName);
        if (dataPointList == null){
            dataPointList = new ArrayList<>();
            dataPointMap.put(dataSetName, dataPointList);
        }

        if (Double.compare(y, Double.NaN) == 0) {
            return dataPointMap;
        }
        Point<Number> mean = new Point<Number>(x, y);
        //        System.out.println("mean:" + stats.getMean());
        dataPointList.add(mean);
        return dataPointMap;
    }

    public static Map<String, List<Point<Number>>> addBoxDataPoint(String dataSetName, int x, double xOffset, double boxWidth, double y, Map<String, List<Point<Number>>> dataPointMap) {
        List<Point<Number>> dataPointList = dataPointMap.get(dataSetName);
        if (dataPointList == null){
            dataPointList = new ArrayList<>();
            dataPointMap.put(dataSetName, dataPointList);
        }

        if (Double.compare(y, Double.NaN) == 0) {
            return dataPointMap;
        }
        Point<Number> mean = new Point<Number>(x + xOffset, y, boxWidth);
        //        System.out.println("mean:" + stats.getMean());
        dataPointList.add(mean);
        return dataPointMap;
    }

    public static Map<String, List<Point<Number>>> addCandleStickDataPoints(String dataSetName, int x, double xOffset, double boxWidth, DescriptiveStatistics stats, Map<String, List<Point<Number>>> dataPointMap) {
        List<Point<Number>> dataPointList = dataPointMap.get(dataSetName);
        if (dataPointList == null){
            dataPointList = new ArrayList<>();
            dataPointMap.put(dataSetName, dataPointList);
        }

        if (Double.compare(stats.getMean(), Double.NaN) == 0) {
            return dataPointMap;
        }
        //x  box_min  whisker_min  whisker_high  box_high  box_width
        Point<Number> mean = new Point<Number>(x + xOffset, stats.getPercentile(25), stats.getMin(),  stats.getMax(), stats.getPercentile(75), boxWidth);
        //        Point<Number> mean = new Point<Number>(x + xOffset, stats.getPercentile(95), stats.getMin(), stats.getMax(), boxWidth);
        //        System.out.println("mean:" + stats.getMean());
        dataPointList.add(mean);
        return dataPointMap;
    }

    public static Map<String, List<Point<Number>>> addDataPointsWithMinMax(String dataSetName, int x, double xOffset, double boxWidth, DescriptiveStatistics stats, Map<String, List<Point<Number>>> dataPointMap) {
        List<Point<Number>> dataPointList = dataPointMap.get(dataSetName);
        if (dataPointList == null){
            dataPointList = new ArrayList<>();
            dataPointMap.put(dataSetName, dataPointList);
        }

        if (Double.compare(stats.getMean(), Double.NaN) == 0) {
            return dataPointMap;
        }
        Point<Number> mean = new Point<Number>(x + xOffset, stats.getMean(), stats.getMin(), stats.getMax(), boxWidth);
        //        Point<Number> mean = new Point<Number>(x + xOffset, stats.getPercentile(95), stats.getMin(), stats.getMax(), boxWidth);
        //        System.out.println("mean:" + stats.getMean());
        dataPointList.add(mean);
        return dataPointMap;
    }



    public static PointDataSet<Number> getSortedPointDataSetForCandleStick(List<Point<Number>> pointListOrig, boolean inverse) {
        PointDataSet<Number> points = new PointDataSet<>();
        List<Point<Number>> pointList = new ArrayList<>();
        int scale = ((inverse == true) ? -1 : 1);

        // Fail-safe behavior
        if (pointListOrig == null || pointListOrig.size() == 0) {
            points.add(new Point(0));
        }

        for (Point<Number> point : pointListOrig) {
            Number[] dataPoints = new Number[point.getDimensions()];
            for (int i = 1; i < point.getDimensions(); i++) {
                dataPoints[i] = scale * point.get(i).doubleValue();
            }
            dataPoints[0] = point.get(0);
            Point<Number> newPoint = new Point<Number>(dataPoints);
            System.out.println("adding " + newPoint);
            pointList.add(newPoint);
        }

        Collections.sort(pointList, new Comparator<Point<Number>>() {

            @Override
            public int compare(Point<Number> o1, Point<Number> o2) {
                if (o1.get(0).doubleValue() < o2.get(0).doubleValue()){
                    return -1;
                } else if (o1.get(0).doubleValue() == o2.get(0).doubleValue()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        points.addAll(pointList);
        return points;
    }

    public static PointDataSet<Number> getSortedPointDataSetWithMinMaxForBox(List<Point<Number>> pointListOrig, boolean inverse) {
        PointDataSet<Number> points = new PointDataSet<>();
        List<Point<Number>> pointList = new ArrayList<>();
        int scale = ((inverse == true) ? -1 : 1);

        // Fail-safe behavior
        if (pointListOrig == null || pointListOrig.size() == 0) {
            points.add(new Point(0));
        }

        for (Point<Number> point : pointListOrig) {
            pointList.add(new Point<Number>(point.get(0),
                    (scale * point.get(1).doubleValue())
                    ,
                    (scale * point.get(2).doubleValue()),
                    (scale * point.get(3).doubleValue()),
                    (point.get(4).doubleValue())
                    ));
        }
        Collections.sort(pointList, new Comparator<Point<Number>>() {

            @Override
            public int compare(Point<Number> o1, Point<Number> o2) {
                if (o1.get(0).doubleValue() < o2.get(0).doubleValue()){
                    return -1;
                } else if (o1.get(0).doubleValue() == o2.get(0).doubleValue()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        points.addAll(pointList);
        return points;
    }

    public static PointDataSet<Number> getSortedPointDataSetForBox(List<Point<Number>> pointListOrig, boolean inverse) {
        PointDataSet<Number> points = new PointDataSet<>();
        List<Point<Number>> pointList = new ArrayList<>();
        int scale = ((inverse == true) ? -1 : 1);

        // Fail-safe behavior
        if (pointListOrig == null || pointListOrig.size() == 0) {
            points.add(new Point(0));
        }

        for (Point<Number> point : pointListOrig) {
            pointList.add(new Point<Number>(point.get(0),
                    (scale * point.get(1).doubleValue())
                    ,
                    (scale * point.get(2).doubleValue())
                    ));
        }
        Collections.sort(pointList, new Comparator<Point<Number>>() {

            @Override
            public int compare(Point<Number> o1, Point<Number> o2) {
                if (o1.get(0).doubleValue() < o2.get(0).doubleValue()){
                    return -1;
                } else if (o1.get(0).doubleValue() == o2.get(0).doubleValue()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        points.addAll(pointList);
        return points;
    }

    public static PointDataSet<Number> getSortedPointDataSet(List<Point<Number>> pointListOrig, boolean inverse) {
        PointDataSet<Number> points = new PointDataSet<>();
        List<Point<Number>> pointList = new ArrayList<>();
        int scale = ((inverse == true) ? -1 : 1);

        // Fail-safe behavior
        if (pointListOrig == null || pointListOrig.size() == 0) {
            points.add(new Point(0));
        }

        for (Point<Number> point : pointListOrig) {
            pointList.add(new Point<Number>(point.get(0),
                    (scale * point.get(1).doubleValue())
                    ));
        }
        Collections.sort(pointList, new Comparator<Point<Number>>() {

            @Override
            public int compare(Point<Number> o1, Point<Number> o2) {
                if (o1.get(0).doubleValue() < o2.get(0).doubleValue()){
                    return -1;
                } else if (o1.get(0).doubleValue() == o2.get(0).doubleValue()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        points.addAll(pointList);
        return points;
    }
}
