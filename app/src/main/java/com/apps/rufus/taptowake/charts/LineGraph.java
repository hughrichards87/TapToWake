package com.apps.rufus.taptowake.charts;

import android.content.Context;
import android.graphics.Color;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class LineGraph {

    private int[] ColorPallet = new int[]{
            Color.GREEN,
            Color.BLUE,
            Color.RED,
            Color.DKGRAY
    };

    private XYSeries[] datasets;
    private XYMultipleSeriesDataset multipleDataset;

    private XYSeriesRenderer[] renderers;
    private XYMultipleSeriesRenderer multipleRenderer;

    private boolean isStacked;
    private int numberOfDatasets;
    private double maxRange;

    public LineGraph(String[] datasetsNames, boolean stacked){

        this.isStacked = stacked;
        this.numberOfDatasets = datasetsNames.length;
        setupDatasets(datasetsNames);
        setupRenderer(numberOfDatasets);
    }

    private void setupDatasets(String[] datasetsNames){
        int numberOfDatasets = datasetsNames.length;
        datasets = new XYSeries[numberOfDatasets];
        multipleDataset = new XYMultipleSeriesDataset();
        for (int i = 0; i < numberOfDatasets; i++){
            datasets[i] = new XYSeries(datasetsNames[i]);
            multipleDataset.addSeries(datasets[i]);
        }
    }

    private void setupRenderer(int numberOfDatasets){
        renderers = new XYSeriesRenderer[numberOfDatasets];
        multipleRenderer = new XYMultipleSeriesRenderer();
        multipleRenderer.setZoomEnabled(false);
        multipleRenderer.setPanEnabled(false);
        multipleRenderer.setMarginsColor(Color.WHITE);
        multipleRenderer.setApplyBackgroundColor(false);
        multipleRenderer.setShowLegend(false);
        multipleRenderer.setShowLabels(false);

        XYSeriesRenderer renderer;
        for (int i = 0; i < numberOfDatasets; i++){
            renderer = new XYSeriesRenderer();
            renderer.setColor(ColorPallet[i]);
            renderer.setPointStyle(PointStyle.POINT);
            renderer.setFillPoints(false);
            renderer.setLineWidth(2f);
            renderers[i] = renderer;
            multipleRenderer.addSeriesRenderer(renderers[i]);
        }
    }

    public GraphicalView getView(Context context){
         return ChartFactory.getLineChartView(context, multipleDataset, multipleRenderer);
    }

    public void addPoint(int indexOfDataset, double x1, double x2){
        if (isStacked)
        {
            //y += (numberOfDatasets - 1 - indexOfDataset) * maxRange;
            double maxRange = calculateMaxRange();
            double differenceInRange = maxRange - this.maxRange;
            if (Math.abs(differenceInRange) > 0.5)
            {
                 this.maxRange = maxRange;
            }
        }

        XYSeries dataset = datasets[indexOfDataset];
        if (dataset.getItemCount() > 600)
        {
            dataset.remove(0);
        }
        dataset.add(x1, x2);
    }

    public double calculateMaxRange()
    {
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double min, max;
        for(int i=0; i < numberOfDatasets; i++)
        {
            min = datasets[i].getMinY();
            max = datasets[i].getMaxY();
            if (min < minY){
                minY = min;
            }
            if (max > maxY){
                maxY = max;
            }
        }
        return maxY - minY;
    }


}
