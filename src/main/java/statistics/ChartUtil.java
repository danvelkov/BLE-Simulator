package statistics;

import java.util.List;
import java.util.Map;

import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

public class ChartUtil {
	
    /**
     * Function to create a bar chart with given data.
     * @param title      Title of the chart
     * @param xAxisLabel Label for the X-axis
     * @param yAxisLabel Label for the Y-axis
     * @param dataMap    A map where keys are category names, and values are maps of series names to values
     * @return BarChart<String, Number>
     */
	
    public BarChart<String, Number> createBarChart(String title, String xAxisLabel, String yAxisLabel, Map<String, Map<String, Double>> dataMap) {
        // Create the axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        // Create the bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(title);

        // Populate the chart with data series
        for (String seriesName : dataMap.values().iterator().next().keySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(seriesName);

            for (Map.Entry<String, Map<String, Double>> categoryEntry : dataMap.entrySet()) {
                String categoryName = categoryEntry.getKey();
                Double value = categoryEntry.getValue().get(seriesName);

                series.getData().add(new XYChart.Data<>(categoryName, value));
            }

            barChart.getData().add(series);
        }

        return barChart;
    }
	
	 /**
     * Function to create a line chart with given data.
     * @param title     Title of the chart
     * @param xAxisLabel Label for the X-axis
     * @param yAxisLabel Label for the Y-axis
     * @param dataMap   A map where keys are series names, and values are lists of Y-values
     * @return LineChart<Number, Number>
     */
	
    public LineChart<Number, Number> createLineChart(String title, String xAxisLabel, String yAxisLabel, Map<String, List<Double>> dataMap) {
        // Create the axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        // Create the line chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);

        // Populate the chart with data series
        for (Map.Entry<String, List<Double>> entry : dataMap.entrySet()) {
            String seriesName = entry.getKey();
            List<Double> data = entry.getValue();

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(seriesName);

            for (int i = 0; i < data.size(); i++) {
                series.getData().add(new XYChart.Data<>(i + 1, data.get(i))); // X-values are indices + 1
            }

            lineChart.getData().add(series);
        }

        return lineChart;
    }
    
    /**
     * Function to create a scatter chart with given data.
     * @param title      Title of the chart
     * @param xAxisLabel Label for the X-axis
     * @param yAxisLabel Label for the Y-axis
     * @param dataMap    A map where keys are series names, and values are lists of double arrays [x, y] for data points
     * @return ScatterChart<Number, Number>
     */
    
    public ScatterChart<Number, Number> createScatterChart(String title, String xAxisLabel, String yAxisLabel, Map<String, List<double[]>> dataMap) {
        // Create the axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        // Create the scatter chart
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(title);

        // Populate the chart with data series
        for (Map.Entry<String, List<double[]>> entry : dataMap.entrySet()) {
            String seriesName = entry.getKey();
            List<double[]> dataPoints = entry.getValue();

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(seriesName);

            for (double[] point : dataPoints) {
                series.getData().add(new XYChart.Data<>(point[0], point[1]));
            }

            scatterChart.getData().add(series);
        }

        return scatterChart;
    }
    
    /**
     * Function to create a stacked bar chart with given data.
     * @param title      Title of the chart
     * @param xAxisLabel Label for the X-axis
     * @param yAxisLabel Label for the Y-axis
     * @param dataMap    A map where keys are category names, and values are maps of series names to values
     * @return StackedBarChart<String, Number>
     */
    
    public StackedBarChart<String, Number> createStackedBarChart(String title, String xAxisLabel, String yAxisLabel, Map<String, Map<String, Double>> dataMap) {
        // Create the axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        // Create the stacked bar chart
        StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setTitle(title);

        // Populate the chart with data series
        for (String seriesName : dataMap.values().iterator().next().keySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(seriesName);

            for (Map.Entry<String, Map<String, Double>> categoryEntry : dataMap.entrySet()) {
                String categoryName = categoryEntry.getKey();
                Double value = categoryEntry.getValue().get(seriesName);

                series.getData().add(new XYChart.Data<>(categoryName, value));
            }

            stackedBarChart.getData().add(series);
        }

        return stackedBarChart;
    }
    
    /**
     * Function to create a density (area) chart with given data.
     * @param title     Title of the chart
     * @param xAxisLabel Label for the X-axis
     * @param yAxisLabel Label for the Y-axis
     * @param dataMap   A map where keys are series names, and values are lists of Y-values
     * @return AreaChart<Number, Number>
     */
    public AreaChart<Number, Number> createDensityChart(String title, String xAxisLabel, String yAxisLabel, Map<String, List<Double>> dataMap) {
        // Create the axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        // Create the area chart
        AreaChart<Number, Number> densityChart = new AreaChart<>(xAxis, yAxis);
        densityChart.setTitle(title);

        // Populate the chart with data series
        for (Map.Entry<String, List<Double>> entry : dataMap.entrySet()) {
            String seriesName = entry.getKey();
            List<Double> data = entry.getValue();

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(seriesName);

            for (int i = 0; i < data.size(); i++) {
                series.getData().add(new XYChart.Data<>(i + 1, data.get(i))); // X-values are indices + 1
            }

            densityChart.getData().add(series);
        }

        return densityChart;
    }
    
    /**
     * Function to create a funnel chart.
     *
     * @param dataList   List of FunnelData containing stage names and values
     * @param chartWidth The width of the chart
     * @param chartHeight The height of the chart
     * @return StackPane containing the funnel chart
     */
    public StackPane createFunnelChart(List<FunnelData> dataList, double chartWidth, double chartHeight) {
        StackPane funnelPane = new StackPane();
        double totalHeight = chartHeight;
        double currentY = 0;

        // Calculate width ratios
        double maxWidth = chartWidth;
        double maxValue = dataList.stream().mapToDouble(FunnelData::getValue).max().orElse(1);

        for (int i = 0; i < dataList.size(); i++) {
            FunnelData data = dataList.get(i);

            double normalizedWidth = maxWidth * (data.getValue() / maxValue);
            double nextNormalizedWidth = i < dataList.size() - 1
                ? maxWidth * (dataList.get(i + 1).getValue() / maxValue)
                : 0;

            double segmentHeight = totalHeight / dataList.size();

            // Create polygon for funnel segment
            Polygon segment = new Polygon();
            segment.getPoints().addAll(
                -normalizedWidth / 2, currentY,             // Top left
                normalizedWidth / 2, currentY,              // Top right
                nextNormalizedWidth / 2, currentY + segmentHeight, // Bottom right
                -nextNormalizedWidth / 2, currentY + segmentHeight // Bottom left
            );
            segment.setFill(Color.hsb(360.0 / dataList.size() * i, 0.8, 0.8));

            // Create label
            Text label = new Text(data.getStageName() + " (" + data.getValue() + ")");
            label.setFill(Color.WHITE);

            // Add segment and label to pane
            funnelPane.getChildren().addAll(segment, label);
            StackPane.setAlignment(label, javafx.geometry.Pos.TOP_CENTER);
            currentY += segmentHeight;
        }

        return funnelPane;
    }

    /**
     * Data model for funnel chart stages.
     */
    static class FunnelData {
        private final String stageName;
        private final double value;

        public FunnelData(String stageName, double value) {
            this.stageName = stageName;
            this.value = value;
        }

        public String getStageName() {
            return stageName;
        }

        public double getValue() {
            return value;
        }
    }

}
