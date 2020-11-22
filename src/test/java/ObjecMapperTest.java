import com.fasterxml.jackson.databind.ObjectMapper;
import domain.ImageMetadata;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjecMapperTest {

    @Test
    public void objectMapperTest() {
        try {
        URL url = new URL("http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME==%27LASCO%27+AND+OBSERVING_MODE.NAME==%27C3%27)+AND+OBSERVATION.BEGINDATE%3E%272012-11-29%2000:00%27+AND+OBSERVATION.BEGINDATE%3C%272012-12-29%2023:55%27&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE");
        URL url1 = new URL("http://ssa.esac.esa.int/ssa/aio/metadata-action?RESOURCE_CLASS=OBSERVATION&SELECTED_FIELDS=OBSERVATION&QUERY=(INSTRUMENT.NAME==%27LASCO%27+AND+OBSERVING_MODE.NAME==%27C3%27)+AND+OBSERVATION.BEGINDATE%3E%272015-11-08%2020:39%27+AND+OBSERVATION.BEGINDATE%3C%272015-11-08%2023:37%27&RETURN_TYPE=JSON&ORDER_BY=OBSERVATION.BEGINDATE");
        ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.readValue(url, ImageMetadata.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
