package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Binary {

    public static byte[] getBytesFromFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            System.out.println("Error - file not found");
        }
        return new byte[0];
    }

    public static void createFileFromBytes(String path, byte[] content) {
        File newFile = new File(path);
        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            outputStream.write(content); // write content to new file
        } catch (IOException e) {
            System.out.println("Error - File could not be created");
        }
    }

    public static byte[] getBytesFromStream(DataInputStream input) {
        try {
            int length = input.readInt();
            byte[] content = new byte[length];
            input.readFully(content, 0, content.length);
            return content;
        } catch (IOException e) {
            System.out.println("Error - DataInputStream could not be read: "+e.getMessage());
        }
        return new byte[0];
    }

    public static void sendBytesToStream(DataOutputStream output, byte[] content) {
        try {
            output.writeInt(content.length);
            output.write(content);
        } catch (IOException e) {
            System.out.println("Error - DataOutputStream could not be written to: "+e.getMessage());
        }
    }
}
