package service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class ConcatenateVideosService {

    private static final String FFMPEG_COMMAND_CONCATENATE_VIDEO = "ffmpeg -f concat -safe 0 -i videos.txt -c copy output.mp4";
    private List<File> videFiles;


    public void concatenateVideoFiles() {
        createTxtListFile();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(FFMPEG_COMMAND_CONCATENATE_VIDEO);
            log.info(IOUtils.toString(process.getErrorStream(), Charset.defaultCharset()));
            while (process.isAlive()) {
                log.info("FfmpegVideoService.createVideo. Is process alive: {}", process.isAlive());
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            log.error("ConcatenateVideosService.concatenateVideoFiles. Error during concatenating video.", e);
        }

    }

    private void createTxtListFile() {
        log.info("ConcatenateVideosService.createTxtListFile. List size={}", videFiles.size());
        Path path = Paths.get("videos.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (File file : videFiles) {
                writer.write("file " + "'" + file.getAbsolutePath() + "'\n");
                writer.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setVideoFiles(List<File> files) {
        this.videFiles = files;
    }
}

