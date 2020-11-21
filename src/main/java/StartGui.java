import domain.DateTimePicker;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import service.AudioServiceImp;
import service.FfmpegVideoService;
import service.ImagesDownloadingServiceImpl;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 0. Сделать расчет продолжительности видео
 * 0.5 Показывать информацию по загрузке в гуи
 * 1. Сначала сделать запуск ffmpeg в идее
 * 2. Проверить загрузку картинок на скомпилировнном варианте.
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

    private Desktop desktop = Desktop.getDesktop();
    private static FfmpegVideoService ffmpegVideoService = new FfmpegVideoService();
    private static ImagesDownloadingServiceImpl imagesDownloadingService = new ImagesDownloadingServiceImpl();
    private static AudioServiceImp audioService = new AudioServiceImp();

    public static void main(String[] args) {
        log.info("STARTING PROGRAM...");
        Application.launch();
    }


    @Override
    public void start(Stage stage) {
        log.info("SecondMainClass.start. Starting visual interface.");
        VBox group = new VBox();
        group.setPadding(new Insets(10));

        DateTimePicker startDatePicker = new DateTimePicker();
        DateTimePicker endDatePicker = new DateTimePicker();
        Button butStartDownloadImages = new Button("Download images");
        Button butStartCreatingVideo = new Button("Create video");

        butStartDownloadImages.setOnAction(action -> {
            LocalDateTime startDateObservation = startDatePicker.getDateTimeValue();
            LocalDateTime endDateObservation = endDatePicker.getDateTimeValue();
            downloadImages(startDateObservation, endDateObservation);
        });

        butStartCreatingVideo.setOnAction(action -> startCreatingVideo());

        final FileChooser fileChooser = new FileChooser();

        TextArea textArea = new TextArea();
        textArea.setMinHeight(70);

        Button butSelectMultiAudioFiles = new Button("Select audio files");

        butSelectMultiAudioFiles.setOnAction(event -> {
            textArea.clear();
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            createListAudioFile(textArea, files);
        });

        group.getChildren().addAll(startDatePicker, endDatePicker, butStartDownloadImages, butSelectMultiAudioFiles, textArea, butStartCreatingVideo);

        stage.setTitle("SOHO-image-sound-video processor");
        Scene scene = new Scene(group, 550, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void startCreatingVideo() {
        int videoRate = imagesDownloadingService.calculateVideoRate(AudioServiceImp.summaryAudioDuration);
        ffmpegVideoService.createVideo(imagesDownloadingService.getPathToJpegListFile(), audioService.getPathToAudioListFie(), videoRate);
    }

    private void openFile(File file) {
        try {
            this.desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downloadImages(LocalDateTime observStartDate, LocalDateTime observEndDate) {
        log.info("SecondMainClass.downloadImages. Start Date={}, endDate={}", observStartDate, observEndDate);
        imagesDownloadingService.setObservStartDate(observStartDate);
        imagesDownloadingService.setObservEndDate(observEndDate);
        log.info("SecondMainClass.downloadImages. downloadingService with query={}", imagesDownloadingService.getQuery());
        try {
            imagesDownloadingService.downloadImagesMetadata();
        } catch (IOException e) {
            log.error("SecondMainClass.downloadImages. {}", e.getStackTrace());
        }
        log.info("SecondMainClass.downloadImages. metadata contains {} image's info ", imagesDownloadingService.getMetaDataTotal());
        imagesDownloadingService.downloadImages();
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
//2009-01-02 00:42:03.538
//2009-01-02 01:18:03.416
//2014-01-01 00:44