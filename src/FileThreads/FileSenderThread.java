package FileThreads;

import Protocol.*;
import Protocol.ErrorPdu.ERROR_CODES;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;

public class FileSenderThread extends FileThread implements Runnable {
  public FileSenderThread(Socket socket) {
    super(socket);
  }

  @Override
  public void run() {
    awaitRequest();
  }

  public int sendDirectoryPdu(String directoryName) {
    Path path = Paths.get(directoryName);
    if (!Files.isDirectory(path)) {
      ErrorPdu errorPdu = sendErrorPdu(ERROR_CODES.NOT_A_DIRECTORY);
      System.out.printf(
              "Directory %s not found, sending Error Code %d with Message %s%n",
              path, errorPdu.getErrorCode(), errorPdu.getErrorString()
      );
      return 1;
    }
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
      ArrayList<String> filePaths = new ArrayList<>();
      directoryStream.forEach(
              (Path filePath) -> filePaths.add(filePath.toString())
      );
      DirectoryPdu directoryPdu = new DirectoryPdu(directoryName, filePaths.toArray(String[]::new));
      System.out.printf("Sending Directory with files:%n%s%n", Arrays.toString(directoryPdu.getFilePaths()));
      directoryPdu.send(socket.getOutputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return 0;
  }

  public int sendFile(String fileName, String filePath) {
    try {
      byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
      if (fileBytes.length < 1) {
        sendErrorPdu(ERROR_CODES.FILE_EMPTY);
        return 1;
      }
      PutPdu putPdu = new PutPdu(fileName, fileBytes);
      System.out.printf("Sending %s with %d bytes%n", fileName, fileBytes.length);
      putPdu.send(socket.getOutputStream());
    } catch (IOException e) {
      ErrorPdu errorPdu = sendErrorPdu(ERROR_CODES.FILE_NOT_FOUND);
      System.out.printf(
              "File %s not found, sending Error Code %d with Message %s%n",
              fileName, errorPdu.getErrorCode(), errorPdu.getErrorString()
      );
      return 1;
    }
    return 0;
  }
  public void awaitRequest() {
    try {
      byte[] data;
      while(true) {
        if (socket.getInputStream().available() > 0) {
          data = socket.getInputStream().readNBytes(socket.getInputStream().available());
          break;
        }
      }
      GetPdu getPdu = (GetPdu) Pdu.createPduFromByteArray(data);
      System.out.printf("%s \"%s\" got requested%n", getPdu.isDirectory() ? "Directory" : "File", getPdu.getPath());
      if (getPdu.isDirectory()) {
        sendDirectoryPdu(getPdu.getPath());
      } else {
        sendFile(getPdu.getJustFileName(), getPdu.getPath());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
