package Receiver;

import FileThreads.FileReceiverThread;
import Protocol.DirectoryPdu;
import Protocol.ErrorPdu;
import Protocol.GetPdu;
import Protocol.Pdu;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileReceiver {

  public static void main(String[] args) {
    if (args.length < 4) {
      System.out.println("Usage: filereceiver [file/directory]type request_path save_path host port");
    }
    String type = args[0];
    String request_path = args[1];
    String save_path = args[2];
    String host = args[3];
    int port = Integer.parseInt(args[4]);
    try {
      Socket socket = new Socket(host, port);
      FileReceiverThread fileReceiverThread;
      Thread fileReceiverThreadWrapper;
      switch (type) {
        case "file":
          fileReceiverThread = new FileReceiverThread(socket, request_path, save_path);
          fileReceiverThreadWrapper = new Thread(fileReceiverThread);
          fileReceiverThreadWrapper.start();
          break;
        case "directory":
          receiveDirectory(host, port, request_path, save_path);
          break;
        case null, default:
          System.out.println("argument 1 must be one of [directory/file]");
      }

    } catch (IOException e) {
      System.out.println("Host not found");
      System.exit(1);
    }
  }

  public static void spawnReceiverThreads(String host, int port, Path directoryPath, String ...filePaths) throws IOException {
    for (String path: filePaths) {
      Socket socket = new Socket(host, port);
      System.out.println("Spawning Thread for " + path);
      String[] fileNameParts = path.split("\\\\");
      FileReceiverThread fileReceiverThread = new FileReceiverThread(socket, path, directoryPath + "\\" +  fileNameParts[fileNameParts.length-1]);
      Thread fileReceiverThreadWrapper = new Thread(fileReceiverThread);
      fileReceiverThreadWrapper.start();
    }
  }

  public static void receiveDirectory(String host, int port, String request_path, String save_path) throws IOException {
    Socket socket = new Socket(host, port);
    Path directoryPath = Path.of(save_path);
    if (Files.isRegularFile(directoryPath)) {
      System.out.println("Save path must be a directory");
      System.exit(1);
    }
    if (!Files.exists(directoryPath)) {
      System.out.println("Directory not found, creating now...");
      Files.createDirectory(directoryPath);
    }
    (new GetPdu(request_path, true)).send(socket.getOutputStream());
    Pdu responsePdu = Pdu.createPduFromInputStream(socket.getInputStream());
    if (responsePdu instanceof ErrorPdu) {
      System.out.printf(
              "Error code %d occurred with Error Message \"%s\"%n",
              ((ErrorPdu) responsePdu).getErrorCode(),
              ((ErrorPdu) responsePdu).getErrorString()
      );
      System.exit(1);
    }
    if (((DirectoryPdu) responsePdu).getSubDirectoryPaths().length > 0) {
      String requestDirectory = ((DirectoryPdu)responsePdu).getDirectoryName();
      String new_save_path = save_path + "\\" + Pdu.getExplodedLastElement(requestDirectory, "\\\\");
      for (String subDirectoryPath : ((DirectoryPdu) responsePdu).getSubDirectoryPaths()) {
        receiveDirectory(host, port, subDirectoryPath, new_save_path);
      }
    }
    spawnReceiverThreads(host, port, directoryPath, ((DirectoryPdu) responsePdu).getFilePaths());
  }

}
