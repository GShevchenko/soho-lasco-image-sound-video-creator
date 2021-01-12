import domain.ChartVideoProcessor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.data.time.Millisecond;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ChartVideoTest {

    public static File dataForTest_10000Lines = new File("E:\\JavaProj\\solar-download-images\\src\\test\\resources\\hdz120308_10000_lines.txt");
    public static File fullDataset = new File("E:\\JavaProj\\solar-download-images\\src\\test\\resources\\hdz120308withoutHeader.txt");
    public static File fullDatasetB = new File("E:\\JavaProj\\solar-download-images\\src\\test\\resources\\hdz120308b.txt");

    @Test
    public void testLinesCountInFile() {
        ChartVideoProcessor chartVideoProcessor = null;
        try {
            chartVideoProcessor = new ChartVideoProcessor(fullDataset, 110);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assertEquals(10000, chartVideoProcessor.getNoOfLinesInInitDataFile());
    }

    @Test
    public void testMilliseconds() {
        Millisecond m = new Millisecond(500, 50, 59, 15, 1, 4, 1950);
        assertEquals(m.getFirstMillisecond(), m.getLastMillisecond());
    }

    @Test
    public void createImages() throws IOException {
        ChartVideoProcessor chartVideoProcessor = new ChartVideoProcessor(fullDatasetB, 110);
        chartVideoProcessor.createAndSaveImage();
        assertTrue(Boolean.TRUE);
    }

    @Test
    public void checkVideoDuration() throws FileNotFoundException {
        File data = new File("E:\\JavaProj\\solar-download-images\\src\\test\\resources\\hdz120308b.txt");
        Scanner scanner = new Scanner(data);
        int count = 0;

        assertTrue(data.exists());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.split(" ").length < 8) {

                System.out.println("Line:" + line);
                System.out.println("count:" + count);
            }
            count++;
        }

    }

    @Test
    public void testRetrieveVideoDuration() {
        String commandLine = "ffmpeg -i E:\\JavaProj\\solar-download-images\\src\\test\\resources\\input1.mp4";
        String commandLine1 = "ffprobe -show_streams -show_format E:\\JavaProj\\solar-download-images\\2012030610.mp4";
        File videoFile = new File("E:\\JavaProj\\solar-download-images\\2012030610.mp4");
        if (videoFile.exists()) {
            log.info("File exist");
        }
        try {
            Process process = Runtime.getRuntime().exec(commandLine1);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String description = "";
            while((description = br.readLine()) != null){
                if (description.contains("description")) {
                    System.out.println(description.split(":")[1].trim());
                }
            }
            log.info("Metadata is: {}", description);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }


}
