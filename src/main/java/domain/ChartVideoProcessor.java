package domain;

import javafx.scene.media.Media;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

/**
 * Алгоритм создания подграфиков
 * 1. Подсчитать количество строк r в файле
 * 2. Рассчитать смещение dr = r/n, где n кол-во картинок в видео из спутниковых снимков.
 * 3. Взять первые 7 часов, сохранить их как график.
 * 4. Сделать смещение и сохранить график. Повторять до достижения конца файла.
 */
@Slf4j
public class ChartVideoProcessor {

    public static final int YEAR_POSITION = 5;
    public static final int MONTH_POSITION = 4;
    public static final int DAY_POSITION = 3;
    public static final int MINUTE_POSITION = 1;
    public static final int HOUR_POSITION = 0;
    public static final int SECONDS_POSITION = 2;
    public static final int EIGHT_HOURS_IN_MILLISECONDS = 8 * 60 * 60 * 1000;
    public static final int SEVEN_HOURS_IN_MILLISECONDS = 7 * 60 * 60 * 1000;
    public static final int H_COMPONENT_POSITION = 6;
    public static final int D_COMPONENT_POSITION = 7;
    public static final int Z_COMPONENT_POSITION = 8;
    private static final String COMPILE_VIDEO_CMD_FFMPEG_WITHOUT_FULL_PATH = "ffmpeg -r %d -f concat -safe 0 -i %s -f concat -safe 0 -i %s -c:a aac -pix_fmt yuv420p -crf 23 -r 24 -shortest -y %s";


    private File fileWithTxtData;
    //r
    private Integer noOfLinesInInitDataFile;
    private Integer startingPosition;
    private Millisecond startingPositionMillisec;
    private Integer numberSohoImages;
    private Integer displacement;
    private Boolean reachEndOfFile;
    public static final Function<String, Integer> STR_TO_INT_FUNCT = Integer::parseInt;
    private double averageH;
    private double averageZ;
    private double averageD;
    private List<File> savedCharts;
    private YRange yRange;
    private double duration;

    public ChartVideoProcessor(File fileWithTxtData, Integer numberSohoImages) throws FileNotFoundException {
        log.info("start ChartVideoObject(), initCrudeDataTxt={}, numberSohoImages={}", fileWithTxtData, numberSohoImages);
        this.fileWithTxtData = fileWithTxtData;
        this.numberSohoImages = numberSohoImages;
        this.startingPosition = 0;
        setNoOfLinesInInitDataFile();
        this.displacement = noOfLinesInInitDataFile / numberSohoImages;
        this.reachEndOfFile = Boolean.FALSE;
        this.savedCharts = new ArrayList<>();
        setAverages();
        log.info("end ChartVideoObject(), averageZ={}, averageD={}, averageH={}", averageZ, averageD, averageH);

    }


    private void setAverages() {
        try (Stream<String> zLines = Files.lines(fileWithTxtData.toPath());
             Stream<String> dLines = Files.lines(fileWithTxtData.toPath());
             Stream<String> hLines = Files.lines(fileWithTxtData.toPath())) {
            //TODO сделать через бифункцию
            ToDoubleFunction<String> zToDoubleFunction = s -> Double.parseDouble(s.split(" ")[Z_COMPONENT_POSITION]);
            ToDoubleFunction<String> dToDoubleFunction = s -> Double.parseDouble(s.split(" ")[D_COMPONENT_POSITION]);
            ToDoubleFunction<String> hToDoubleFunction = s -> Double.parseDouble(s.split(" ")[H_COMPONENT_POSITION]);
            averageZ = zLines.mapToDouble(zToDoubleFunction).average().orElse(1);
            averageD = dLines.mapToDouble(dToDoubleFunction).average().orElse(1);
            averageH = hLines.mapToDouble(hToDoubleFunction).average().orElse(1);

        } catch (IOException exception) {
            log.error("ChartVideoObject.setAverages. path to file={}", fileWithTxtData);
        }
    }


    public void setFileWithTxtData(File fileWithTxtData) {
        log.info("ChartVideoObject.setInitCrudeDataTxt(). initCrudeDataTxt={}", fileWithTxtData);
        this.fileWithTxtData = fileWithTxtData;

    }


    private void setNoOfLinesInInitDataFile() {
        log.info("ChartVideoObject.setNoOfLinesInInitDataFile()");
        try (Stream<String> fileStream = Files.lines(fileWithTxtData.toPath())) {
            noOfLinesInInitDataFile = (int) fileStream.count();
        } catch (IOException exception) {
            log.error("ChartVideoObject.setNoOfLinesInInitDataFile. ", exception);
        }
    }

    public int getNoOfLinesInInitDataFile() {
        return noOfLinesInInitDataFile;
    }


    public TimeSeriesCollection getDataForPeriod() throws FileNotFoundException {
        log.info("ChartVideoObject.getDataForPeriod start");
        TimeSeries hComponent = new TimeSeries("H-component");
        TimeSeries dComponent = new TimeSeries("D-component");
        TimeSeries zComponent = new TimeSeries("Z-component");
        Scanner scanner = new Scanner(fileWithTxtData);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        if (startingPositionMillisec == null) {
            startingPositionMillisec = convertStrToMillisecond(scanner.nextLine().split(" "));
        }
        int currentPosition = 0;
        while (currentPosition < startingPosition) {
            scanner.nextLine();
            currentPosition++;
        }
        log.info("ChartVideoObject.getDataForPeriod(), processedLines={}, currentPosition={}, displacement={}", startingPosition, currentPosition, displacement);
        Millisecond nextMillSec = startingPositionMillisec;
        Millisecond nexStartingPosition = startingPositionMillisec;
        while (scanner.hasNextLine() &&
                nextMillSec.getFirstMillisecond() - startingPositionMillisec.getFirstMillisecond() < SEVEN_HOURS_IN_MILLISECONDS) {
            String[] nextLine = scanner.nextLine().split(" ");
            nextMillSec = convertStrToMillisecond(nextLine);
            //TODO сделать через функцию
            hComponent.add(nextMillSec, 120 *(Double.parseDouble(nextLine[H_COMPONENT_POSITION]) - averageH));
            dComponent.add(nextMillSec, -120 * (Double.parseDouble(nextLine[D_COMPONENT_POSITION])- averageD));
            zComponent.add(nextMillSec, -120 * (Double.parseDouble(nextLine[Z_COMPONENT_POSITION]) - averageZ));
            ++currentPosition;
            if (currentPosition == startingPosition + displacement) {
                nexStartingPosition = nextMillSec;
            }
        }
        if (!scanner.hasNextLine()) {
            reachEndOfFile = true;
        }
        scanner.close();
        startingPosition += displacement;
        dataset.addSeries(hComponent);
        dataset.addSeries(dComponent);
        startingPositionMillisec = nexStartingPosition;
        dataset.addSeries(zComponent);
        log.info("ChartVideoObject.getDataForPeriod end");
        return dataset;
    }

    public void createAndSaveImage() throws IOException {
        savedCharts.clear();
        while (!reachEndOfFile && startingPosition != noOfLinesInInitDataFile) {
            TimeSeriesCollection timeSeriesCollection = getDataForPeriod();
            log.info("Start ChartVideoObject.createAndSaveImage(), processedLines={}, noOfLinesInInitDataFile={}", startingPosition, noOfLinesInInitDataFile);
            JFreeChart timeSeriesChart = ChartFactory.createTimeSeriesChart("BGZ", null, null, timeSeriesCollection);
            XYPlot plot = (XYPlot) timeSeriesChart.getPlot();
            plot.setBackgroundPaint(Color.white);
            plot.setRangeGridlinePaint(Color.BLACK);
            plot.setDomainGridlinePaint(Color.BLACK);
            NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
            yAxis.setRange(-60, 100);
            yAxis.setTickUnit(new NumberTickUnit(20));
            yAxis.setTickLabelFont(new Font("Dialog", Font.BOLD, 26));
            DateAxis xAxis = (DateAxis) plot.getDomainAxis();
//            xAxis.setTickUnit(new DateTickUnit(DateTickUnitType.MILLISECOND, 2*3600*1000, new SimpleDateFormat("HH:mm")));
            xAxis.setTickLabelFont(new Font("Dialog", Font.BOLD, 26));

            XYLineAndShapeRenderer hRenderer = new XYLineAndShapeRenderer();
            hRenderer.setSeriesLinesVisible(0, true);
            hRenderer.setSeriesShapesVisible(0, false);
            hRenderer.setSeriesShapesVisible(1, false);
            hRenderer.setSeriesShapesVisible(2, false);

            plot.setRenderer(0, hRenderer);
            plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(0, Color.GREEN);
            plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(1, Color.BLUE);
            plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(2, Color.RED);
            plot.setDomainCrosshairVisible(true);
            plot.setDomainCrosshairPaint(Color.BLACK);
            plot.setDomainCrosshairStroke(new BasicStroke(1f));
            plot.setDomainCrosshairValue(timeSeriesCollection.getXValue(0, timeSeriesCollection.getItemCount(0) / 2));
            File chartJpegFile = new File(startingPosition + "_time_series_chart.jpg");
            ChartUtils.saveChartAsJPEG(chartJpegFile, timeSeriesChart, 1024, 1024);
            savedCharts.add(chartJpegFile);
//            displaceMillisecond();
            log.info("End ChartVideoObject.createAndSaveImage()");
        }
        createFileForFfmpeg();
    }

//    public void createVideo(Path pathToJpegListFile, String pathToAudioListFile, int videoRate, LocalDateTime startObservDate, LocalDateTime endObservDate, int shiftInHours) {
//        log.info("FfmpegVideoService.createVideo. pathToJpegListFile={}, pathToAudioListFile={}, videoRate={}", pathToJpegListFile, pathToAudioListFile, videoRate);
//        String command = String.format(COMPILE_VIDEO_CMD_FFMPEG_WITHOUT_FULL_PATH, videoRate, pathToJpegListFile.toAbsolutePath(), pathToAudioListFile, calculateVideoFileName(startObservDate, endObservDate, shiftInHours));
//                                                                            "ffmpeg -r %d -f concat -safe 0 -i %s -f concat -safe 0 -pix_fmt yuv420p -crf 23 -r 24 -shortest -y %s"
//        log.info("cmd is {}", command);
//        Process process = null;
//        try {
//            process = Runtime.getRuntime().exec(command);
//            log.info(IOUtils.toString(process.getErrorStream(), Charset.defaultCharset()));
//            while (process.isAlive()) {
//                log.info("FfmpegVideoService.createVideo. Is process alive: {}", process.isAlive());
//                Thread.sleep(1000);
//            }
//        } catch (IOException | InterruptedException e) {
//            log.info("FfmpegVideoService.createVideo. Error during creation video pathToJpegListFile={}, pathToAudioListFile={}, error={}",
//                    pathToJpegListFile, pathToAudioListFile, e.getStackTrace());
//        }
//    }

//    public int calculateVideoRate(double audioDuration) {
//        log.info("ChartVideoProcessor.calculateVideoRate. audioDuration={}, imagesCount={}", audioDuration, imagesCount);
//        return (int) Math.round(savedCharts.size() / );
//    }

    public void createFileForFfmpeg() {
        log.info("ChartVideoProcessor.createFileForFfmpeg(). savedCharts size is {}", savedCharts.size());
        for (File chartImage : savedCharts) {
            try (FileWriter writer = new FileWriter("charts.txt", true)) {
                writer.write("file '" + chartImage.getAbsolutePath() + "'\n");
            } catch (IOException exception) {
                log.error("ChartVideoProcessor.createFileForFfmpeg(). ", exception);
            }
        }

    }

    private void displaceMillisecond() {
        long shiftedTime = startingPositionMillisec.getFirstMillisecond() - displacement;
        startingPositionMillisec = new Millisecond(new Date(shiftedTime));

    }

    private Millisecond convertStrToMillisecond(String[] strArray) {
//        log.info("ChartVideoObject.convertStrToMillisecond(), strArray={}", strArray.to);
        return new Millisecond(
                STR_TO_INT_FUNCT.apply(strArray[SECONDS_POSITION].split("\\.")[1]),
                STR_TO_INT_FUNCT.apply(strArray[SECONDS_POSITION].split("\\.")[0]),
                STR_TO_INT_FUNCT.apply(strArray[MINUTE_POSITION]),
                STR_TO_INT_FUNCT.apply(strArray[HOUR_POSITION]),
                STR_TO_INT_FUNCT.apply(strArray[DAY_POSITION]),
                STR_TO_INT_FUNCT.apply(strArray[MONTH_POSITION]),
                2000 + STR_TO_INT_FUNCT.apply(strArray[YEAR_POSITION]));
    }

    public void setDuration(File sohoVideoFile) {
        Media videoFile = new Media(sohoVideoFile.getAbsolutePath());
        Duration duration = videoFile.getDuration();
        this.duration = duration.toMillis();
    }
}
