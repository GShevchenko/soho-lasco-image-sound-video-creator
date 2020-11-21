import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import service.FfmpegVideoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class FfmpegVideoServiceTest {

    private static String fullPathToJpeg = "E:\\JavaProj\\solar-download-images\\20150101_0026_20150101_0626\\images.txt";
    private static String fullPathToAudioFile = "E:\\JavaProj\\solar-download-images\\20150101_0026_20150101_0626\\audio.txt";
    private static String relativePathToJpeg = "images.txt";
    private static String pathToAudio = "audio.txt";


    @Test
    public void testCreateVideo() {
        log.info("FfmpegVideoServiceTest.testCreateVideo() START PROCESS...");
        FfmpegVideoService ffmpegVideoService = new FfmpegVideoService();
        assertTrue(Files.exists(Paths.get(fullPathToJpeg)));
        assertTrue(Files.exists(Paths.get(fullPathToAudioFile)));
        ffmpegVideoService.createVideo(fullPathToJpeg, fullPathToAudioFile, 24);
        assertTrue(true);
        log.info("FfmpegVideoServiceTest.testCreateVideo() END PROCESS...");
    }
}
