package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface Handler {

    String runOperation(String request, DataInputStream input, DataOutputStream output);
}
