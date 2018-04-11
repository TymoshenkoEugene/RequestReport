package net.company.requesttest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class FilesHelper {

    public static void copyFile(String pathFrom, String pathTo) throws IOException {
        File fileFrom = new File(pathFrom);
        if(!fileFrom.exists()){
            throw new FileNotFoundException("File "+pathFrom+" not found");
        }

        File fileTo = new File(pathTo);

        Files.copy(fileFrom.toPath(),fileTo.toPath());
    }
}
