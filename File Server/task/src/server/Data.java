package server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs read/write operations on the server's files and returns status codes.
 */
public class Data {

    public static final String ROOT;
    static {
        String path = System.getProperty("user.dir");
        Pattern p = Pattern.compile("File Server");
        Matcher m = p.matcher(path);
        if (m.find()) {
            String start = path.substring(0, m.end());
            ROOT = start + "/File Server/task/src";
        } else {
            throw new RuntimeException("Illegal directory configuration");
        }
    }
    private static final String PATH = ROOT+"/server/data/";
    private static final String FILE_IDS_PATH = ROOT+"/server/files.ser";
    private final ConcurrentHashMap<Integer,String> fileIDs;

    /**
     * Deserialize {@link #fileIDs} if a saved state exists, otherwise make a new map.
     */
    @SuppressWarnings("unchecked")
    public Data() {
        if ((new File(FILE_IDS_PATH)).exists()) {
            try (FileInputStream fileStream = new FileInputStream(FILE_IDS_PATH);
                 ObjectInputStream fromFile = new ObjectInputStream(fileStream)) {
                this.fileIDs = (ConcurrentHashMap<Integer, String>) fromFile.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.fileIDs = new ConcurrentHashMap<>();
        }

    }

    /**
     * Attempt to save {@link #fileIDs} to disk.
     * @return {@link Status} of operation
     */
    public Status onTerminate() {
        try (FileOutputStream fileStream = new FileOutputStream(FILE_IDS_PATH);
             ObjectOutputStream toFile = new ObjectOutputStream(fileStream)) {
            toFile.writeObject(fileIDs);
        } catch (IOException e) {
            return Status.FORBIDDEN;
        }
        return Status.OK;
    }

    /**
     * Find the next unused ID in {@link #fileIDs}.
     * @return smallest positive integer not present in keys
     */
    private int getNextID() {
        int next = 1;
        while (fileIDs.containsKey(next)) {
            next++;
        }
        return next;
    }

    /**
     * Create a list of ID(s) associated with a given file.
     * @param fileName file to search for
     * @return all keys with this file as their value
     */
    private List<Integer> findIDsFromName(String fileName) {
        return fileIDs.keySet()
                .stream()
                .filter(key -> fileIDs.get(key).equals(fileName))
                .toList();
    }

    /**
     * Generate an unused random alphanumeric filename.
     * @return name of the new file
     */
    private String randomNewFileName() {
        String fileName;
        Random random = new Random();
        int nameLength = 8;
        do {
            fileName = random.ints('0', '{')
                    .filter(i -> (i <= '9') || ('A' <= i && i <= 'Z') || ('a' <= i))
                    .limit(nameLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        } while (new File(PATH+fileName).exists());
        return fileName;
    }

    /**
     * Write a string to a new file.
     * @param fileName name of new file
     * @return {@link Status} result of operation
     */
    public Status put(String fileName, Reply reply) {
        if (fileName == null) {
            fileName = randomNewFileName(); // assign a random name if not specified by the client
        }
        File newFile = new File(PATH+fileName);
        try {
            boolean created = newFile.createNewFile();
            if (created) {
                System.out.println("file was created");
                byte[] content = reply.getFileContent();
                if (content != null) { // write any content to the new file
                    try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
                        outputStream.write(content);
                    }
                    String value = "";
                    int id = 0;
                    while (value != null) {
                        id = getNextID();
                        value = fileIDs.putIfAbsent(id, fileName);
                    }
                    reply.setFileID(id);
                    System.out.println("file id = "+id);
                }
                return Status.OK;
            }
            return Status.FORBIDDEN; // file already exists
        } catch (IOException e) {
            return Status.FORBIDDEN;
        }
    }

    public Optional<byte[]> getBytesFromDisk(String fileName) {
        try {
            return Optional.of(Files.readAllBytes(Paths.get(PATH + fileName)));
        } catch (IOException ignored) {}
        return Optional.empty();
    }

    /**
     * Check that a given file can be accessed.
     * @param fileName name of file to access
     */
    public Status get(String fileName, Reply reply) {
        Optional<byte[]> content = getBytesFromDisk(fileName);
        if (content.isPresent()) {
            reply.setFileContent(content.get());
            return Status.OK;
        }
        return Status.NOT_FOUND;
    }

    public Status get(int identifier, Reply reply) {
        if (fileIDs.containsKey(identifier)) {
            return get(fileIDs.get(identifier), reply);
        }
        return Status.NOT_FOUND;
    }

    /**
     * Delete a file on disk.
     * @param fileName name of file to delete
     * @return {@link Status} result of operation
     */
    public Status delete(String fileName) {
        File file = new File(PATH+fileName);
        if (file.delete()) {
            findIDsFromName(fileName).forEach(fileIDs::remove);
            return Status.OK;
        } else {
            return Status.NOT_FOUND;
        }
    }

    public Status delete(int identifier) {
        if (fileIDs.containsKey(identifier)) {
            File file = new File(PATH+fileIDs.get(identifier));
            if (file.delete()) {
                fileIDs.remove(identifier);
                return Status.OK;
            }
        }
        return Status.NOT_FOUND;
    }

}
