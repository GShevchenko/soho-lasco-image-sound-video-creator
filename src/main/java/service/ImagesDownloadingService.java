package service;

import java.io.IOException;

public interface ImagesDownloadingService {

    void downloadImagesMetadata() throws IOException;
    int downloadImages();
    void createListImagesFileForFmpeg();
}
