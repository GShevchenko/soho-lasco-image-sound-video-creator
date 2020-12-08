package domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageMetadata implements IImageMetadata, Serializable {


    @JsonProperty("OBSERVATION.BEGINDATE")
    public String beginDate;
    @JsonProperty("OBSERVATION.CALIBRATED")
    public String calibrated;
    @JsonProperty("OBSERVATION.ENDDATE")
    public String endDate;
    @JsonProperty("OBSERVATION.FILESIZE")
    public int fileSize;


    public String download;
    @JsonProperty("postcard")
    public String postcardUrl;
    @JsonIgnoreProperties
    public String details;

    @Override
    public boolean isContainC3Image() {
        return postcardUrl != null;
    }

    @Override
    public String get1024JpegUrl() {
        return postcardUrl;
    }

    @Override
    public String getBeginObservationDate() {
        return beginDate;
    }

    public String getJpegFileName() {
        return beginDate.substring(0, beginDate.lastIndexOf(":")).replaceAll("-", "").replace(" ", "").replace(":", "")+ ".jpg";
    }


}


