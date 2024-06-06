package server;

import client.Action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class Reply {

    private Action action;
    private Status status;
    private int fileID;
    private byte[] fileContent;

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    @Override
    public String toString() {
        return "Reply("+action+", "+status+", ID="+fileID+", content="+fileContent+")";
    }


    private void readBytesFromInput(DataInputStream input) {
        try {
            int length = input.readInt();
            fileContent = new byte[length];
            input.readFully(fileContent, 0, length);
        } catch (IOException e) {
            status = Status.FORBIDDEN;
        }
    }

    public void execute(Data data, List<String> parameters, DataInputStream input) {
        switch (action) {
            case POST -> status = Status.OK;
            case GET -> {
                String getType = parameters.get(0);
                String parameter = parameters.get(1);
                status = Status.FORBIDDEN;
                if ("BY_NAME".equals(getType)) {
                    status = data.get(parameter, this);
                } else if ("BY_ID".equals(getType)) {
                    try {
                        status = data.get(Integer.parseInt(parameter), this);
                    } catch (NumberFormatException ignored) {}
                }
            }
            case PUT -> {
                readBytesFromInput(input);
                if (fileContent != null) {
                    String fileName = parameters.size() == 1 ? parameters.get(0) : null;
                    status = data.put(fileName, this);
                } else {
                    status = Status.FORBIDDEN;
                }
            }
            case DELETE -> {
                String deleteType = parameters.get(0);
                String parameter = parameters.get(1);
                status = Status.NOT_FOUND;
                if ("BY_NAME".equals(deleteType)) {
                    status = data.delete(parameter);
                } else if ("BY_ID".equals(deleteType)) {
                    try {
                        status = data.delete(Integer.parseInt(parameter));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    public String updateClient(DataOutputStream output) {
        String response = String.valueOf(status.code);
        if (action == Action.PUT) {
            response = response+" "+fileID;
        }
        try {
            output.writeUTF(response);
            if (action == Action.GET && status == Status.OK) {
                output.writeInt(fileContent.length);
                output.write(fileContent);
            }
            return response;
        } catch (IOException e) {
            System.out.println("Could not reply to client");
            return "";
        }
    }
}
