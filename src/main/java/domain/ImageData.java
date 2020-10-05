package domain;

import com.opencsv.bean.CsvBindByPosition;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ImageData {

    private static final Integer POSITION_1024_BYTE_JPEG = 1;
    @Getter
    @CsvBindByPosition(position = 0)
    private String url512WithUrl1024AsString;

    @CsvBindByPosition(position = 1)
    private String startObservation;

    @CsvBindByPosition(position = 2)
    private String endObservation;

    @CsvBindByPosition(position = 3)
    private String minSpectralRange;

    @CsvBindByPosition(position = 4)
    private String maxSpectralRange;

    @CsvBindByPosition(position = 5)
    private String waveType;

    @CsvBindByPosition(position = 6)
    private String observable;

    @CsvBindByPosition(position = 7)
    private String DataLayout;

    @CsvBindByPosition(position = 8)
    private String source;

    @CsvBindByPosition(position = 9)
    private String instrument;

    @CsvBindByPosition(position = 10)
    private String extent;

    public Boolean isContainImage() {
        return url512WithUrl1024AsString != null;
    }

    public Boolean isContainC3Image() {
        return isContainImage() && url512WithUrl1024AsString.contains("c3");
    }

    public String get1024Jpeg() {
        return url512WithUrl1024AsString.split(" ")[POSITION_1024_BYTE_JPEG];
    }

    public String getImageName() {
        String fullURLToImage = get1024Jpeg();
        return fullURLToImage.substring(fullURLToImage.lastIndexOf("/") + 1);
    }


}
