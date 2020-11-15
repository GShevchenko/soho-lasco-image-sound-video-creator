import domain.DateTimePicker;
import domain.IImageMetadata;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import service.ImagesDownloadingServiceImpl;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

//TODO добавить к имени jpeg его id
//TODO разобраться как сделать автоматическую компиляцию командой: mvn clean compile assembly:single
//TODO разобратья как удалить ненужные зависимости из конечного джарника и удалить их
@Slf4j
public class SecondMainClass extends Application {

    private static String query = "http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME=='LASCO'+AND+OBSERVING_MODE.NAME=='C3')+AND+OBSERVATION.BEGINDATE>'%s'+AND+OBSERVATION.BEGINDATE<'%s'&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE";
    public static String mockQuery = "http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME==%27LASCO%27+AND+OBSERVING_MODE.NAME==%27C3%27)+AND+OBSERVATION.BEGINDATE%3E%272009-01-01%2023:18:22.588%27+AND+OBSERVATION.BEGINDATE%3C%272009-01-02%2000:18:30.945%27&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE";
    private static String mockQueryWithoutSeconds = "http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME==%27LASCO%27+AND+OBSERVING_MODE.NAME==%27C3%27)+AND+OBSERVATION.BEGINDATE%3E%272009-01-01%2023:18%27+AND+OBSERVATION.BEGINDATE%3C%272009-01-02%2000:18%27&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE";
    private Desktop desktop = Desktop.getDesktop();

    public static void main(String[] args) throws IOException {
        log.info("STARTING PROGRAM...");
        Application.launch();
////        String resultedQuery = getTimePeriodFromUser();
//        String resultedQuery = mockQueryWithoutSeconds;
//        log.info("SecondMainClass. Queries {}", resultedQuery);
//        URL url = new URL(resultedQuery);
//        ObjectMapper objectMapper = new ObjectMapper();
//        MetaTotal metaTotal = objectMapper.readValue(url, MetaTotal.class);
//        log.info("SecondMainClass. ImageMetadata is {}", metaTotal);
//        downloadAllC3Images(metaTotal.getData());

    }

    public static String getTimePeriodFromUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter begin date observation in format yyyy-mm-dd 00:00:00.000: ");
        String startDate = scanner.nextLine();
        log.info("SecondMainClass. Start date is {}", startDate);
        System.out.println("Enter end date observation in format yyyy-mm-dd 00:00:00.000: ");
        String endDate = scanner.nextLine();
        log.info("SecondMainClass. End date is {}", endDate);
        return String.format(query, startDate, endDate);
    }

    public static <T extends IImageMetadata> int downloadAllC3Images(List<T> imageDataList) {
        log.info("SecondMainClass.downloadAllC3Images. imageDataList size={}", imageDataList.size());
        int count = 1;
        for (IImageMetadata imageData : imageDataList) {
            if (imageData.isContainC3Image()) {
                try {
                    URL url = new URL(imageData.get1024JpegUrl());
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(imageData.getBeginObservationDate().split(" ")[0] + "-" + imageData.getBeginObservationDate().split(" ")[1].replaceAll(":", "-") + ".jpg");
                    fileOutputStream.getChannel()
                            .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    count++;
                } catch (IOException ex) {
                    log.error("SecondMainClass.downloadAllC3Images. " + ex);
                }

            }
        }
        return count;
    }


    @Override
    public void start(Stage stage) throws Exception {
        log.info("SecondMainClass.start. Starting visual interface.");
// here we create a regular window
        VBox group = new VBox();
        group.setPadding(new Insets(10));

        DateTimePicker startDatePicker = new DateTimePicker();
        DateTimePicker endDatePicker = new DateTimePicker();
        Button button = new Button("Read Date");

        button.setOnAction(action -> {
            LocalDateTime startDateObservation = startDatePicker.getDateTimeValue();
            LocalDateTime endDateObservation = endDatePicker.getDateTimeValue();
            downloadImages(startDateObservation, endDateObservation);
        });

        final FileChooser fileChooser = new FileChooser();

        TextArea textArea = new TextArea();
        textArea.setMinHeight(70);

        Button button1 = new Button("Select One File and Open");
        Button buttonM = new Button("Select Multi Files");

        button1.setOnAction(event -> {
            textArea.clear();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                openFile(file);
                List<File> files = Collections.singletonList(file);
                log.info("SecondMainClass.button1 {}, {}", textArea, files);
            }
        });

        buttonM.setOnAction(event -> {
            textArea.clear();
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            printLog(textArea, files);

        });

        group.getChildren().addAll(startDatePicker, endDatePicker,button, textArea , button1, buttonM);

        stage.setTitle("SOHO-image-sound-video processor");
        Scene scene = new Scene(group, 550, 500);
        stage.setScene(scene);
        stage.show();
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
        ImagesDownloadingServiceImpl downloadingService = new ImagesDownloadingServiceImpl(observStartDate, observEndDate);
        log.info("SecondMainClass.downloadImages. downloadingService with query={}", downloadingService.getQuery());
        try {
            downloadingService.downloadImagesMetadata();
        } catch (IOException e) {
            log.error("SecondMainClass.downloadImages. {}", e.getStackTrace());
        }
        log.info("SecondMainClass.downloadImages. metadata contains {} image's info ", downloadingService.getMetaDataTotal());
        downloadingService.downloadImages();
    }

    private void printLog(TextArea textArea, List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        for (File file : files) {
            textArea.appendText(file.getAbsolutePath() + "\n");
        }
    }
}
//2009-01-02 00:42:03.538
//2009-01-02 01:18:03.416