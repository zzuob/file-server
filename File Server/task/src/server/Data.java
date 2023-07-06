package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Data {
    private final String PATH = "File Server/task/src/server/data/";

    public String put(String fileName, String content) {
        File newFile = new File(PATH+fileName);
        try {
            boolean hasCreated = newFile.createNewFile();
            if (hasCreated) {
                try (FileWriter writer = new FileWriter(newFile)) {
                    writer.write(content);
                    return "200";
                }
            } else {
                return "403";
            }
        } catch (IOException e) {
            return "404";
        }

    }

}
