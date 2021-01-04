import domain.ChartVideoObject;
import lombok.extern.slf4j.Slf4j;
import org.jfree.data.time.Millisecond;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ChartVideoTest {

    public static File dataForTest_10000Lines = new File("E:\\JavaProj\\solar-download-images\\src\\test\\resources\\hdz120308_10000_lines.txt");
    public static File fullDataset = new File("E:\\JavaProj\\solar-download-images\\src\\test\\resources\\hdz120308withoutHeader.txt");

    @Test
    public void testLinesCountInFile() {
        ChartVideoObject chartVideoObject = null;
        try {
            chartVideoObject = new ChartVideoObject(fullDataset, 110);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assertEquals(10000, chartVideoObject.getNoOfLinesInInitDataFile());
    }

    @Test
    public void testMilliseconds() {
        Millisecond m = new Millisecond(500, 50, 59, 15, 1, 4, 1950);
        assertEquals(m.getFirstMillisecond(), m.getLastMillisecond());
    }

    @Test
    public void createImages() throws IOException {
        ChartVideoObject chartVideoObject = new ChartVideoObject(fullDataset, 110);
        chartVideoObject.createAndSaveImage();
        assertTrue(Boolean.TRUE);
    }



}
