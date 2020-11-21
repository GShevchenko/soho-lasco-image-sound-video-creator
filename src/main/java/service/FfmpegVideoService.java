package service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class FfmpegVideoService {

    private static final String COMPILE_VIDEO_COMMAND_TEMPLATE = "ffmpeg -r 24 -f concat -i %s concat -i %s -c:a aac -pix_fmt yuv420p -crf 23 -r 24 -y video-from-frames.mp4";
    private static final String SECOND_COMMAND = "E:\\Programs\\ffmpeg\\ffmpeg-2020-10-03-git-069d2b4a50-full_build\\bin\\ffmpeg -r %d -f concat -safe 0 -i %s -f concat -safe 0 -i %s -c:a aac -pix_fmt yuv420p -crf 23 -r 24 -shortest -y video-from-frames_111.mp4";


    public void createVideo(String pathToJpegListFile, String pathToAudioListFile, int videoRate) {
        log.info("FfmpegVideoService.createVideo. pathToJpegListFile={}, pathToAudioListFile={}, videoRate={}", pathToJpegListFile, pathToAudioListFile, videoRate);
        String command = String.format(SECOND_COMMAND, videoRate, pathToJpegListFile, pathToAudioListFile);
        log.info("cmd is {}", command);
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            log.info(IOUtils.toString(process.getErrorStream(), Charset.defaultCharset()));
            while (process.isAlive()) {
                log.info("FfmpegVideoService.createVideo. Is process alive: {}", process.isAlive());
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            log.info("FfmpegVideoService.createVideo. Error during creation video pathToJpegListFile={}, pathToAudioListFile={}, error={}",
                    pathToJpegListFile, pathToAudioListFile, e.getStackTrace());
        }


    }
}
