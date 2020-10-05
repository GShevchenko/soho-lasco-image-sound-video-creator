import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import domain.ImageData;
import lombok.extern.log4j.Log4j;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Log4j
public class MainClass {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("src/main/resources/vso_export_20201004_000000 (2).csv");
        List<ImageData> listImageData = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
            ColumnPositionMappingStrategy ms = new ColumnPositionMappingStrategy();
            ms.setType(ImageData.class);

            CsvToBean cb = new CsvToBeanBuilder(reader)
                    .withType(ImageData.class)
                    .withMappingStrategy(ms)
                    .build();
            listImageData.addAll(cb.parse());
        } catch (IOException ex) {
            ex.printStackTrace(); //handle an exception here
        }
        int downloadedImages = downloadAllC3Images(listImageData);
        System.out.printf("It was successfully downloaded %d images", downloadedImages);


    }

    //Сделать обработку ошибки внутри метода
    public static int downloadAllC3Images(List<ImageData> imageDataList) throws IOException {
        int count = 0;
        for (ImageData imageData : imageDataList) {
            if (imageData.isContainC3Image()) {
                URL url = new URL(imageData.get1024Jpeg());
                ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(imageData.getImageName());
                fileOutputStream.getChannel()
                        .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                count++;
            }
        }
        return count;
    }

}
