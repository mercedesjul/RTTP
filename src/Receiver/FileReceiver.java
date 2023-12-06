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
    System.out.println(args[0]);
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
          Path directoryPath = Path.of(save_path);
          if (Files.isRegularFile(directoryPath)) {
            System.out.println("save path must be a directory");
          }
          if (!Files.exists(directoryPath)) {
            System.out.println("Directory not found, creating now...");
            Files.createDirectory(directoryPath);
          }
          (new GetPdu(request_path, true)).send(socket.getOutputStream());
          byte[] data;
          while (true) {
            if (socket.getInputStream().available() > 0) {
              data = socket.getInputStream().readNBytes(socket.getInputStream().available());
              break;
            }
          }
          Pdu responsePdu = Pdu.createPduFromByteArray(data);
          if (responsePdu instanceof ErrorPdu) {
            System.out.printf(
                    "Error code %d occurred with Error Message \"%s\"%n",
                    ((ErrorPdu) responsePdu).getErrorCode(),
                    ((ErrorPdu) responsePdu).getErrorString()
            );
            System.exit(1);
          }

          for (String path: ((DirectoryPdu) responsePdu).getFilePaths()) {
            socket = new Socket(host, port);
            System.out.println("Spawning Thread for " + path);
            String[] fileNameParts = path.split("\\\\");
            fileReceiverThread = new FileReceiverThread(socket, path, directoryPath + "\\" +  fileNameParts[fileNameParts.length-1]);
            fileReceiverThreadWrapper = new Thread(fileReceiverThread);
            fileReceiverThreadWrapper.start();
          }
          break;
        case null, default:
          System.out.println("argument 1 must be one of [directory/file]");
      }

    } catch (IOException e) {
      System.out.println("Host not found");
      System.exit(1);
    }
  }

}
