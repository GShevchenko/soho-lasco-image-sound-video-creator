package service;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class AudioServiceImp {

    public static double summaryAudioDuration = 0;

    public void createListImagesFileForFmpeg(List<File> audioFiles) {
        log.info("AudioServiceImp.createListImagesFileForFmpeg. List size={}", audioFiles.size());
        Path path = Paths.get( "audio.txt");
        try(BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)){
            for (File file: audioFiles){
                writer.write("file " + "'" + file.getAbsolutePath() + "'\n");
                writer.newLine();
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public String getPathToAudioListFie() {
        return "audio.txt";
    }

    public void setSummaryDuration(List<File> audioFiles) {
        summaryAudioDuration = 0;
        log.info("AudioServiceImp.setSummaryDuration. List size={}", audioFiles.size());
        for (File audioFile : audioFiles) {
            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile)) {
                AudioFormat format = audioInputStream.getFormat();
                long audioFileLength = audioFile.length();
                log.info("AudioServiceImp.setSummaryDuration. File's {} length is {}", audioFile.getName(), audioFileLength);
                int frameSize = format.getFrameSize();
                float frameRate = format.getFrameRate();
                summaryAudioDuration += (audioFileLength / (frameSize * frameRate));
            } catch (UnsupportedAudioFileException | IOException exception) {
                    log.error("AudioServiceImp.setSummaryDuration. ", exception);
            }
        }

    }
}
