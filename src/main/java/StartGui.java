import domain.DateTimePicker;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import service.AudioServiceImp;
import service.ConcatenateVideosService;
import service.FfmpegVideoService;
import service.ImagesDownloadingServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

/**
 * -1. Увеличить размер графических элеметнов.
 * 0. Проверить на больших данных
 * 0.1 Исправить название конечного файла
 * 0.5 Показывать информацию по загрузке в гуи
 * 3. Сделать графический интерфейс получше
 * 4. Разобраться с многопоточностью в javafx.
 * 5. Разобраться, можно ли ffmpeg добавить в джарник, чтобы его не устанавливать на компе.
 * 6. Продумать показ сообщениий от ffmpeg и/или показ логов и ошибок от него.
 *
 */
//TODO добавить к имени jpeg его id
//TODO разобратья как удалить ненужные зависимости из конечного джарника и удалить их
//TODO проверить везде закрытие стримов
//TODO разобраться с делиметерами для разных ОП
@Slf4j
public class StartGui extends Application {

    private static FfmpegVideoService ffmpegVideoService = new FfmpegVideoService();
    private static ImagesDownloadingServiceImpl imagesDownloadingService = new ImagesDownloadingServiceImpl();
    private static AudioServiceImp audioService = new AudioServiceImp();
    private static ConcatenateVideosService concatenateVideosService = new ConcatenateVideosService();

    public static void main(String[] args) {
        log.info("STARTING PROGRAM...");
        Application.launch();
    }


    @Override
    public void start(Stage stage) {
        log.info("StartGui.start. Starting visual interface.");
        Tab secondTabForDownloadingImagesAndCreatingVideo = new Tab("JPEG video processor");
        Tab thirdTabForCombiningVideo = new Tab("Combine video files");

        VBox group = new VBox();
        HBox selectPreviousDownloadedImages = new HBox();
        GridPane gridPane = new GridPane();
        HBox boxForDateTimePickers = new HBox();
        boxForDateTimePickers.setSpacing(10);
        boxForDateTimePickers.scaleYProperty();
        group.setPadding(new Insets(10));
        gridPane.add(boxForDateTimePickers, 0, 0);
        gridPane.add(selectPreviousDownloadedImages, 0, 1);
        gridPane.add(group, 0, 2);



        DateTimePicker startDatePicker = new DateTimePicker();
        startDatePicker.setDateTimeValue(LocalDateTime.of(2012, Month.MARCH, 6, 14, 0));
        DateTimePicker endDatePicker = new DateTimePicker();
        endDatePicker.setDateTimeValue(LocalDateTime.of(2012, Month.MARCH, 7, 14, 0));

        Button butStartDownloadImages = new Button("Get jpeg");
        boxForDateTimePickers.getChildren().addAll(startDatePicker, endDatePicker, butStartDownloadImages);

        startDatePicker.setOnAction(action -> {
            endDatePicker.setDateTimeValue(startDatePicker.getDateTimeValue().plusDays(1));
        });
        startDatePicker.setPrefHeight(30);
        endDatePicker.setPrefHeight(30);
        butStartDownloadImages.setPrefHeight(30);
        butStartDownloadImages.setFont(Font.font(14));
        Button butStartCreatingVideo = new Button("Create video");
        butStartCreatingVideo.setPrefHeight(30);
        butStartCreatingVideo.setFont(Font.font(14));
        butStartDownloadImages.setOnAction(action -> {
            LocalDateTime startDateObservation = startDatePicker.getDateTimeValue();
            startDatePicker.setUserData(startDateObservation);
            LocalDateTime endDateObservation = endDatePicker.getDateTimeValue();
            downloadImages(startDateObservation, endDateObservation);
        });

        butStartCreatingVideo.setOnAction(action -> startCreatingVideo());

        final FileChooser audioFileChooser = new FileChooser();
        final FileChooser videoFileChooser = new FileChooser();

        TextArea audioFilesTextArea = new TextArea();
        audioFilesTextArea.setMinHeight(70);


        TextArea videoFilesTextArea = new TextArea();
        videoFilesTextArea.setMinHeight(70);

        Button butSelectMultiAudioFiles = new Button("Select audio files");
        butSelectMultiAudioFiles.setPrefHeight(30);
        butSelectMultiAudioFiles.setFont(Font.font(14));

        butSelectMultiAudioFiles.setOnAction(event -> {
            audioFilesTextArea.clear();
            List<File> files = audioFileChooser.showOpenMultipleDialog(stage);
            if (files != null && files.size() > 0) {
                audioFileChooser.setInitialDirectory(new File(files.get(0).getParent()));
            }
            createListAudioFile(audioFilesTextArea, files);
        });


        Button butSelectMultiVideoFiles = new Button("Select video files");
        butSelectMultiVideoFiles.setPrefHeight(30);
        butSelectMultiVideoFiles.setFont(Font.font(14));
        Button concatenateVideosButt = new Button("Concatenate videos");
        concatenateVideosButt.setPrefHeight(30);
        concatenateVideosButt.setFont(Font.font(14));
        butSelectMultiVideoFiles.setOnAction(event -> {
            videoFilesTextArea.clear();
            List<File> files = videoFileChooser.showOpenMultipleDialog(stage);
            if (files != null && files.size() > 0) {
                videoFileChooser.setInitialDirectory(new File(files.get(0).getParent()));
            }
            for (File file : files) {
                videoFilesTextArea.appendText(file.getAbsolutePath() + "\n");
            }
            concatenateVideosService.setVideoFiles(files);
        });
        concatenateVideosButt.setOnAction( event -> {
            concatenateVideosService.concatenateVideoFiles();
        });
        group.getChildren().addAll(butSelectMultiAudioFiles, audioFilesTextArea, butStartCreatingVideo);

        secondTabForDownloadingImagesAndCreatingVideo.setContent(gridPane);
        thirdTabForCombiningVideo.setContent(new VBox(videoFilesTextArea, butSelectMultiVideoFiles, concatenateVideosButt));

        TextArea jpegDirTA = new TextArea();
        Button jpegDirBtn = new Button("Select dir with jpeg");
        jpegDirBtn.setFont(Font.font(14));
        jpegDirTA.setPrefSize(500, 20);
        DirectoryChooser jpegDirChr = new DirectoryChooser();
        selectPreviousDownloadedImages.getChildren().addAll(jpegDirTA, jpegDirBtn);
        jpegDirBtn.setOnAction( e ->{
            jpegDirTA.clear();
            File possibleJpegDir = jpegDirChr.showDialog(stage);
            Optional<Path> optionalPath = setJpegDirToVideProcessor(possibleJpegDir);
            optionalPath.ifPresent(p -> jpegDirTA.setText(p.toString()));
        });

        TabPane tabPane = new TabPane(secondTabForDownloadingImagesAndCreatingVideo, thirdTabForCombiningVideo);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        stage.setTitle("SOHO-image-sound-video processor");
        Scene scene = new Scene(tabPane, 700, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void startCreatingVideo() {
        int videoRate = imagesDownloadingService.calculateVideoRate(AudioServiceImp.summaryAudioDuration);
        ffmpegVideoService.createVideo(imagesDownloadingService.getCurrentFolderWithJpegFile(), audioService.getPathToAudioListFie(),
                videoRate, imagesDownloadingService.getObservStartDate(),
                imagesDownloadingService.getObservEndDate(), imagesDownloadingService.getImagesCount(), 0);
    }

    private Optional<Path> setJpegDirToVideProcessor(File jpegDir) {
        log.info("StartGui.setJpegDirToVideProcessor. jpegDir={}", jpegDir);
        return imagesDownloadingService.setJpegDir(jpegDir);
    }

    public static void downloadImages(LocalDateTime observStartDate, LocalDateTime observEndDate) {
        log.info("StartGui.downloadImages. Start Date={}, endDate={}", observStartDate, observEndDate);
        imagesDownloadingService.setObservStartDate(observStartDate);
        imagesDownloadingService.setObservEndDate(observEndDate);
        try {
            imagesDownloadingService.downloadImagesMetadata();
        } catch (IOException e) {
            log.error("StartGui.downloadImages. ", e);
        }
        log.info("StartGui.downloadImages. metadata contains {} image's info ", imagesDownloadingService.getMetaDataTotal().getTotal());
        imagesDownloadingService.downloadImagesParallel();
        imagesDownloadingService.createListImagesFileForFmpeg();
    }

    private void createListAudioFile(TextArea textArea, List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        for (File file : files) {
            textArea.appendText(file.getAbsolutePath() + "\n");
        }
        audioService.createListImagesFileForFmpeg(files);
        audioService.setSummaryDuration(files);
    }
}
//2012-04-08 04:00