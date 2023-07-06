import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testcase.TestCase;
import org.hyperskill.hstest.testing.TestedProgram;
import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FileServerTest extends StageTest<String> {

    private static final String onConnectExceptionMessage = "A client can't connect to the server!\n" +
        "Make sure the server handles connections and doesn't stop after one client connected.";
    private static final String filesPath = System.getProperty("user.dir") +
        File.separator + "src" + File.separator + "server" + File.separator + "server/data" + File.separator;

    private static final Map<String, String> savedFiles = new HashMap<>();

    @Override
    public List<TestCase<String>> generate() {
        return Collections.singletonList(
            new TestCase<String>()
                .feedbackOnException(ConnectException.class, onConnectExceptionMessage)
                .setDynamicTesting(this::test)
        );
    }

    CheckResult test() {

        testStopServer();

        TestedProgram client;
        TestedProgram server = new TestedProgram("server");
        String fileName;
        String fileContent;

        if (!Files.exists(Paths.get(filesPath)) || !Files.isDirectory(Paths.get(filesPath))) {
            return CheckResult.wrong("Can't find '/server/data' folder. You should store all saved files in it!\n" +
                "The folder should be created even if the server wasn't started!");
        }

        // Delete files in case the previous test was failed with exception
        deleteTestFiles();
        server.startInBackground();

        // Test #1 Saving a file on the server
        client = getClient();
        client.start();
        fileName = FileNameGenerator.name();
        fileContent = FileNameGenerator.content();
        savedFiles.put(fileName, fileContent);
        client.execute(String.format("2\n%s\n%s", fileName, fileContent));

        if (!isFileExists(fileName)) {
            return CheckResult.wrong("Can't find just saved file in the /server/data folder!");
        }

        String savedFileContent = getFileContent(fileName);
        if (!savedFileContent.equals(savedFiles.get(fileName))) {
            return CheckResult.wrong("A file after saving has wrong content!");
        }

        // Test #2 Saving a fail that already exists
        client = getClient();
        client.start();
        String output = client.execute(String.format("2\n%s\n%s", fileName, fileContent));

        if (!output.contains("The response says that creating the file was forbidden!")) {
            return CheckResult.wrong("You should print 'The response says that creating the file was forbidden!' " +
                "if a client tries to add file that already exist!");
        }

        // Test #3 Getting a file
        client = getClient();
        client.start();
        output = client.execute(String.format("1\n%s", fileName));

        if (!output.contains("The content of the file is")) {
            return CheckResult.wrong("When a client tries to get a file that is stored on the server" +
                " you should print:\n\"The content of the file is: FILE_CONTENT\"\nwhere FILE_CONTENT is a " +
                "content of the requested file!");
        }

        if (!output.contains(fileContent)) {
            return CheckResult.wrong("The server returned wrong content of the file!");
        }

        // Test #4 Getting a not existing file
        client = getClient();
        client.start();
        fileName = FileNameGenerator.name();
        output = client.execute(String.format("1\n%s", fileName));

        if (!output.contains("The response says that the file was not found!")) {
            return CheckResult.wrong("You should print \"The response says that the file was not found!\" if a" +
                " client tries to request a file that doesn't exist");
        }

        // Test #5 Deleting a file that doesn't exist
        client = getClient();
        client.start();
        fileName = FileNameGenerator.name();
        output = client.execute(String.format("3\n%s", fileName));

        if (!output.contains("The response says that the file was not found!")) {
            return CheckResult.wrong("You should print \"The response says that the file was not found!\" if a" +
                " client tries to delete a file that doesn't exist");
        }

        // Test #6 Deleting a file
        client = getClient();
        client.start();

        fileName = savedFiles.keySet().stream().findFirst().get();
        client.execute(String.format("3\n%s", fileName));

        if (isFileExists(fileName)) {
            return CheckResult.wrong("You should delete a file from /server/data folder if the user requests it!");
        }

        // Stop server
        client = getClient();
        client.start();
        client.execute("exit");

        return CheckResult.correct();
    }

    private static void testStopServer() {
        TestedProgram server = new TestedProgram("server");
        TestedProgram client = new TestedProgram("client");

        server.startInBackground();
        client.start();
        client.execute("exit");

        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {}

        if (!server.isFinished()) {
            throw new WrongAnswer("The server should stop after a client sends 'exit'!");
        }
    }

    private static void deleteTestFiles() {
        File dir = new File(filesPath);
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().startsWith("test_purpose_")) {
                boolean isDeleted = file.delete();
                if (!isDeleted) {
                    throw new WrongAnswer("Can't delete test files. Maybe they are not closed!");
                }
            }
        }
    }

    private static boolean isFileExists(String fileName) {
        String path = filesPath + fileName;
        return Files.exists(Paths.get(path)) && !Files.isDirectory(Paths.get(path));
    }

    private static String getFileContent(String fileName) {
        String path = filesPath + fileName;
        try {
            return new String(Files.readAllBytes(Paths.get(path))).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Calls fatal error
        throw new RuntimeException("Can't read file!");
    }

    @AfterClass
    public static void afterTestDeleteFiles() {
        deleteTestFiles();
    }

    public static TestedProgram getClient() {
        return new TestedProgram("client");
    }
}

class FileNameGenerator {

    private final static String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";
    private final static Random rand = new Random();
    private final static Set<String> identifiers = new HashSet<>();

    public static String name() {
        return generate(5, true);
    }

    public static String content() {
        return generate(15, false);
    }

    public static String generate(int len, boolean name) {
        StringBuilder builder = new StringBuilder();

        while (builder.toString().length() == 0) {
            if (name) builder.append("test_purpose_");
            int length = rand.nextInt(len) + 5;
            for (int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if (identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            } else {
                identifiers.add(builder.toString());
            }
        }
        if (name) builder.append(".txt");
        return builder.toString();
    }
}
