package service;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

//https://stackoverflow.com/questions/12837986/how-to-display-date-in-a-x-axis-of-line-graph-using-jfreechart
@Slf4j
public class LineCharService extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    private TimeSeriesCollection createImage() {
        log.info("LineCharService.createDataset.");
        TimeSeries series1 = new TimeSeries("Data");
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("E:\\JavaProj\\solar-download-images\\src\\main\\resources\\chart_data\\hdz120308_10000_lines.txt"));
            Function<String, Integer> strToIn = Integer::parseInt;
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(" ");

                series1.add(new Millisecond(strToIn.apply(line[2].split("\\.")[1]), strToIn.apply(line[2].split("\\.")[0]),
                        strToIn.apply(line[1]), strToIn.apply(line[0]), strToIn.apply(line[3]),strToIn.apply(line[4]),
                        2000 + strToIn.apply(line[5])), Double.parseDouble(line[6]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        log.info("Maximum age is {}", series1.getMaximumItemAge());
        dataset.addSeries(series1);
        return dataset;
    }

    public void createImages() {
        File data = new File("E:\\JavaProj\\solar-download-images\\src\\main\\resources\\chart_data\\hdz120308_10000_lines.txt");
        int noOfLines = getLinesInFile(data);
        int noOfImages = 110;
        int shift = noOfLines / noOfImages;

    }

    public int getLinesInFile(File file) {
        int noOfLines = 0;
        try (FileChannel channel = FileChannel.open(Paths.get(file.getAbsolutePath()), StandardOpenOption.READ)) {
            ByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            while (byteBuffer.hasRemaining()) {
                byte currentByte = byteBuffer.get();
                if (currentByte == '\n')
                    noOfLines++;
            }
        } catch (IOException exception) {
            log.error("LineCharService.getLinesInFile(). ",exception);
        }
        return noOfLines;
    }


    private ObservableList<XYChart.Series<Double, Double>> getChartData() {
        ObservableList<XYChart.Series<Double, Double>> answer = FXCollections.
                observableArrayList();
        XYChart.Series<Double, Double> componentH = new XYChart.Series<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("E:\\JavaProj\\solar-download-images\\src\\main\\resources\\chart_data\\hdz120308_1000_lines.txt"));
            int count = 0;
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(" ");
                Double time = 3600 * Double.parseDouble(line[0]) + 60 * Double.parseDouble(line[1]) + Double.parseDouble(line[2]);
                componentH.getData().add(new XYChart.Data(time, Double.valueOf(line[7])));
                log.info("line number is {}, time is {}", count++, time);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        double javaValue = 15.57;
        double cValue = 6.97;
        double cppValue = 4.55;

        XYChart.Series<Integer, Double> c = new XYChart.Series<>();
        XYChart.Series<Integer, Double> cpp = new XYChart.Series<>();
//        for (int i = 0; i < 2027; i+=50) {
//            componentH.getData().add(new XYChart.Data(i, javaValue));
//            javaValue = javaValue + 4 * Math.random() - 2;
////            c.getData().add(new XYChart.Data(i, cValue));
////            cValue = cValue + Math.random() - .5;
////            cpp.getData().add(new XYChart.Data(i, cppValue));
////            cppValue = cppValue + 4 * Math.random() - 2;
//        }
        answer.addAll(componentH);

        return answer;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TimeSeriesCollection timeSeriesCollection = createImage();
        List<TimeSeries> series = (List<TimeSeries>) timeSeriesCollection.getSeries();
        JFreeChart timeSeriesChart = ChartFactory.createTimeSeriesChart("BGZ", null, null,
                createImage());
        XYPlot plot = (XYPlot) timeSeriesChart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
//        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        plot.setRenderer(0, renderer);
//        plot.setRenderer(1, renderer1);
        plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(0, Color.BLACK);
//        plot.getRendererForDataset(plot.getDataset(1)).setSeriesPaint(0, Color.blue);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairPaint(Color.BLACK);
        plot.setDomainCrosshairStroke(new BasicStroke(1f));
        plot.setDomainCrosshairValue(timeSeriesCollection.getXValue(0, timeSeriesCollection.getItemCount(0) /2));

        ChartUtils.saveChartAsPNG(new File("time_series_chart.jpeg"), timeSeriesChart, 450, 400);
    }
}
