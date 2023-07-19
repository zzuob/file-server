package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Data {
    private final String PATH = "/File Server/task/src/server/data/";
    private Map<Integer, String> fileIDs = new ConcurrentHashMap<>();

    private int getNextID() { // get the next lowest unused file ID
        int lowestUnused = 1;
        while (true) {
            boolean found = false;
            for (Integer id: fileIDs.keySet()) {
                if (lowestUnused == id) {
                    found = true;
                    break;
                }
                if (found) {
                    lowestUnused++;
                } else {
                    return lowestUnused;
                }
            }
        }
    }

    public String put(String fileName, String content) {
        File newFile = new File(PATH+fileName);
        try {
            boolean hasCreated = newFile.createNewFile();
            if (hasCreated) {
                try (FileWriter writer = new FileWriter(newFile)) {
                    writer.write(content);
                }
                int ID = getNextID();
                fileIDs.put(ID, fileName);
                return "200";
            } else {
                return "403";
            }
        } catch (IOException e) {
            return "404";
        }
    }

    public String get(String fileName) {
        File file = new File(PATH+fileName);
        try (Scanner scan = new Scanner(file)) {
            StringBuilder response = new StringBuilder("200 "); // file found
            while (scan.hasNext()) {
                response.append(scan.nextLine());
                response.append("\n");
            }
            return response.toString(); // return status code and content of file
        } catch (FileNotFoundException e) {
            return "404"; // file not found
        }
    }

    public String delete(String fileName) {
        File file = new File(PATH+fileName);
        if (file.delete()) {
            for (Integer id: fileIDs.keySet()) {
                if (Objects.equals(fileIDs.get(id), fileName)) {
                    fileIDs.remove(id);
                }
            }
            return "200";
        } else {
            return "404";
        }
    }

}
