package server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Data {
    private final String PATH = "/home/zinzan/IdeaProjects/File Server/File Server/task/src/server/data/";
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

    public String put(String fileName, byte[] content) {
        File newFile = new File(PATH+fileName);
        try {
            boolean hasCreated = newFile.createNewFile();
            if (hasCreated) {
                FileOutputStream outputStream = new FileOutputStream(newFile);
                outputStream.write(content);
                outputStream.close();
                int ID = getNextID();
                fileIDs.put(ID, fileName);
                return "200";
            } else {
                return "403";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "404";
        }
    }

    public String get(String fileName) {
        // check if file can be read
        try {
            Files.readAllBytes(Path.of(PATH + fileName));
            return "200";

        } catch (IOException e) {
            return "404"; // file not found
        }
    }

    public byte[] getFileBytes(String fileName) {
        try {
            return Files.readAllBytes(Path.of(PATH + fileName));

        } catch (IOException e) {
            return new byte[0];
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
