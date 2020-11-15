package domain;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class MetaTotal implements Serializable {

    private Integer total;
    private List<ImageMetadata> data;

}
