package org.example.service;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class FfmpegVideoService {

    private static final String COMPILE_VIDEO_CMD_FFMPEG_WITHOUT_FULL_PATH = "ffmpeg -r %d -f concat -safe 0 -i %s -f concat -safe 0 -i %s -c:a aac -pix_fmt yuv420p -crf 23 -r 24 -shortest -y %s";


    public void createVideo(String pathToJpegListFile, String pathToAudioListFile, int videoRate, LocalDateTime startObservDate, LocalDateTime endObservDate, int shiftInHours) {
//        log.info("FfmpegVideoService.createVideo. pathToJpegListFile={}, pathToAudioListFile={}, videoRate={}", pathToJpegListFile, pathToAudioListFile, videoRate);
        String command = String.format(COMPILE_VIDEO_CMD_FFMPEG_WITHOUT_FULL_PATH, videoRate, pathToJpegListFile, pathToAudioListFile, calculateVideoFileName(startObservDate, endObservDate, shiftInHours));
//        log.info("cmd is {}", command);
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
//            log.info(IOUtils.toString(process.getErrorStream(), Charset.defaultCharset()));
            while (process.isAlive()) {
//                log.info("FfmpegVideoService.createVideo. Is process alive: {}", process.isAlive());
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
//            log.info("FfmpegVideoService.createVideo. Error during creation video pathToJpegListFile={}, pathToAudioListFile={}, error={}",
//                    pathToJpegListFile, pathToAudioListFile, e.getStackTrace());
        }
    }

    public String calculateVideoFileName(LocalDateTime startObservDate, LocalDateTime endObservDate, int shiftInHours) {
//        log.info("FfmpegVideoService.calculateVideoFileName. startObservDate={}, endObservDate={}, shiftInHours", startObservDate, endObservDate, shiftInHours);
        return formatStartDateForViedoFileName(startObservDate) + (endObservDate.getDayOfYear() - startObservDate.getDayOfYear()) + shiftInHours + ".mp4";

    }

    //В имени файла фильма записываем t0 (6 чисел), длительность периода
    //озвучки t1-t0 в сутках (3 числа), сдвиг в часах dt (2числа)
    public String formatStartDateForViedoFileName(LocalDateTime startObservDate) {
//        log.info("FfmpegVideoService.formatStartDateForViedoFileName. startObservDate={}", startObservDate );
        return DateTimeFormatter.ofPattern("yyyyMMdd").format(startObservDate);
    }
}


