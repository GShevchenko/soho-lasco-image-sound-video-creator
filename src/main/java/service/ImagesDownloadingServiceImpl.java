package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;
import domain.IImageMetadata;
import domain.MetaTotal;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME=='LASCO'+AND+OBSERVING_MODE.NAME=='C3')+AND+OBSERVATION.BEGINDATE>'2009-11-01 00:02'+AND+OBSERVATION.BEGINDATE<'2009-11-01 14:02'&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE
//http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME=='LASCO'+AND+OBSERVING_MODE.NAME=='C3')+AND+OBSERVATION.BEGINDATE>'2020-11-15 14:02'+AND+OBSERVATION.BEGINDATE<'2020-11-15 14:02'&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE
//http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME==%27LASCO%27+AND+OBSERVING_MODE.NAME==%27C3%27)+AND+OBSERVATION.BEGINDATE%3E%272009-01-01%2023:18:22.588%27+AND+OBSERVATION.BEGINDATE%3C%272009-01-02%2000:18:30.945%27&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE
@Slf4j
@Data
@Getter
@Setter
@RequiredArgsConstructor
public class ImagesDownloadingServiceImpl implements ImagesDownloadingService {
    public static String templateQuery = "http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME=='LASCO'+AND+OBSERVING_MODE.NAME=='C3')+AND+OBSERVATION.BEGINDATE>'%s'+AND+OBSERVATION.BEGINDATE<'%s'&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE";
    public static String mockQuery = "http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME==%27LASCO%27+AND+OBSERVING_MODE.NAME==%27C3%27)+AND+OBSERVATION.BEGINDATE%3E%272009-01-01%2023:18:22.588%27+AND+OBSERVATION.BEGINDATE%3C%272009-01-02%2000:18:30.945%27&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE";
    public static String mock2 = "http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME=='LASCO'+AND+OBSERVING_MODE.NAME=='C3')+AND+OBSERVATION.BEGINDATE>'2009-11-01%2000:02'+AND+OBSERVATION.BEGINDATE<'2009-11-01%2014:02'&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE";
    public static String mock3 = "http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME=='LASCO'+AND+OBSERVING_MODE.NAME=='C3')+AND+OBSERVATION.BEGINDATE>'2009-11-01'+AND+OBSERVATION.BEGINDATE<'2009-11-02'&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE";
    @NonNull
    private LocalDateTime observStartDate;
    @NotNull
    private LocalDateTime observEndDate;
    private MetaTotal metaDataTotal;

    private String query;

    public ImagesDownloadingServiceImpl(LocalDateTime observStartDate, LocalDateTime observEndDate) {
        this.observStartDate = observStartDate;
        this.observEndDate = observEndDate;
        this.query = String.format(templateQuery, removeTFromDateTime(observStartDate), removeTFromDateTime(observEndDate));
    }


    public void downloadImagesMetadata() throws IOException {
        log.info("ImagesDownloadingServiceImpl.downloadImagesMetadata. Query is {}", getQuery());
        URL url = new URL(getQuery());
        log.info("ImagesDownloadingServiceImpl.downloadImagesMetadata. {}", url.getQuery());
        ObjectMapper objectMapper = new ObjectMapper();
        String text = IOUtils.toString(url.openStream(), StandardCharsets.UTF_8.name());
        log.info("ImagesDownloadingServiceImpl.downloadImagesMetadata. text is {}", text);
        metaDataTotal = objectMapper.readValue(url.openStream(), MetaTotal.class);
    }

    @Override
    public int downloadImages() {
        log.info("SecondMainClass.downloadAllC3Images. imageDataList size={}", metaDataTotal.getTotal());
        int count = 0;
        try {
            createFolderForJpegs();
        } catch (IOException e) {
            log.info("ImagesDownloadingServiceImpl.downloadImages" + e.getStackTrace());
            return 0;
        }
        for (IImageMetadata imageData : metaDataTotal.getData()) {
            if (imageData.isContainC3Image()) {
                try {
                    URL url = new URL(imageData.get1024JpegUrl());
                    log.info("ImagesDownloadingService.downloadImages. Try to download image {}", imageData.get1024JpegUrl());
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream( getFolderNameForJpegs() + imageData.getBeginObservationDate().split(" ")[0] + "-" + imageData.getBeginObservationDate().split(" ")[1].replaceAll(":", "-") + ".jpg");
                    fileOutputStream.getChannel()
                            .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    count++;
                } catch (IOException ex) {
                    log.error("ImagesDownloadingServiceImpl.downloadImages. " + ex);
                }

            }
        }
        log.info("ImagesDownloadingServiceImpl.downloadImages. Total count is {}", count);
        return count;
    }

    @Override
    public void createListImagesFileForFmpeg() {

    }

    public void createFolderForJpegs() throws IOException {
        Files.createDirectories(Paths.get(getFolderNameForJpegs()));
    }

    public String getFolderNameForJpegs() {
        return formatDateTimeForFolderName(observStartDate) + "_" + formatDateTimeForFolderName(observEndDate) + File.separator;
    }

    private String formatDateTimeForFolderName(LocalDateTime dateTime) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm").format(dateTime);
    }

    public static String removeTFromDateTime(LocalDateTime dateTimeWithT) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd%20HH:mm").format(dateTimeWithT);
    }


}
