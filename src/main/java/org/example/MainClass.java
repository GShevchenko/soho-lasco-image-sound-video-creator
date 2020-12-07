package org.example;

public class MainClass {

//    public static void main(String[] args) throws IOException {
//        Path path = Paths.get("src\\main\\resources\\one_week\\vso_export_20201101_000000 (2).csv");
////        Path path = Paths.get(args[0]);
//        List<ImageData> listImageData = new ArrayList<>();
//        try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
//            ColumnPositionMappingStrategy ms = new ColumnPositionMappingStrategy();
//            ms.setType(ImageData.class);
//
//            CsvToBean cb = new CsvToBeanBuilder(reader)
//                    .withType(ImageData.class)
//                    .withMappingStrategy(ms)
//                    .build();
//            listImageData.addAll(cb.parse());
//        } catch (IOException ex) {
//            ex.printStackTrace(); //handle an exception here
//        }
//        System.out.printf("Csv file contains %d c3 files\n", listImageData.stream().filter(ImageData::isContainC3Image).count());
//        System.out.println("Enter Y for downloading images: ");
//        Scanner userInput = new Scanner(System.in);
//        if (!userInput.next().toLowerCase().equals("y")) {
//            System.out.println("Exit from program");
//            userInput.close();
//            System.exit(0);
//        }
//        userInput.close();
//        int downloadedImages = downloadAllC3Images(listImageData);
//        System.out.printf("It was successfully downloaded %d images", downloadedImages - 1);
//
//
//    }
//
//    //TODO Сделать обработку ошибки внутри метода
//    public static int downloadAllC3Images(List<ImageData> imageDataList) throws IOException {
//        int count = 1;
//        for (ImageData imageData : imageDataList) {
//            if (imageData.isContainC3Image()) {
//                URL url = new URL(imageData.get1024Jpeg());
//                System.out.printf("Image %d %s is downloaded\n", count, url.getPath());
//                ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
//                FileOutputStream fileOutputStream = new FileOutputStream(count < 10? "0" + count + ".jpg": count + ".jpg");
//                fileOutputStream.getChannel()
//                        .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
//                count++;
//            }
//        }
//        return count;
//    }

}
