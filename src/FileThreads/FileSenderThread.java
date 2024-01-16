package FileThreads;

import Protocol.*;
import Protocol.ErrorPdu.ERROR_CODES;

import java.io.File;
import java.io.FileInputStream;
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
              "%sDirectory %s not found, sending Error Code %d with Message %s%n",
              getThreadIdStringPrefix(), path, errorPdu.getErrorCode(), errorPdu.getErrorString()
      );
      return 1;
    }
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
      ArrayList<String> directoryPaths = new ArrayList<>();
      ArrayList<String> filePaths = new ArrayList<>();
      directoryStream.forEach(
              (Path filePath) -> {
                if (filePath.toFile().isDirectory()) {
                   directoryPaths.add(filePath.toString());
                } else {
                  filePaths.add(filePath.toString());
                }
              }
      );
      DirectoryPdu directoryPdu = new DirectoryPdu(
              directoryName,
              filePaths.toArray(String[]::new),
              directoryPaths.toArray(String[]::new)
      );
      System.out.printf(
              "%sSending Directory with files:%n%s%nand subdirectories:%s%n",
              getThreadIdStringPrefix(),
              Arrays.toString(directoryPdu.getFilePaths()),
              Arrays.toString(directoryPdu.getSubDirectoryPaths())
      );
      directoryPdu.send(socket.getOutputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return 0;
  }

  public int sendFile(String fileName, String filePath) {
    try {
      File file = new File(filePath);
      if (file.length() < 1) {
        sendErrorPdu(ERROR_CODES.FILE_EMPTY);
        return 1;
      }
      FileInputStream fileInputStream = new FileInputStream(file);
      PutPdu putPdu = new PutPdu(fileName, (int) file.length(), fileInputStream);
      System.out.printf("%sSending %s with %d bytes%n", getThreadIdStringPrefix(), fileName, file.length());
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
      System.out.println(getThreadIdStringPrefix() + "Awaiting Request");
      Pdu requestPdu = Pdu.createPduFromInputStream(socket.getInputStream());
      if (requestPdu instanceof ErrorPdu) {
        ErrorPdu errorPdu = sendErrorPdu(ERROR_CODES.PARSE_ERROR);
        System.out.printf(
                "%sCould not parse Pdu. Sending Error Code %d with Message %s%n",
                getThreadIdStringPrefix(), errorPdu.getErrorCode(), errorPdu.getErrorString()
        );
        return;
      }
      System.out.printf(
              "%s%s \"%s\" got requested%n",
              getThreadIdStringPrefix(),
              ((GetPdu) requestPdu).isDirectory() ? "Directory" : "File",
              ((GetPdu) requestPdu).getPath()
      );
      if (((GetPdu) requestPdu).isDirectory()) {
        sendDirectoryPdu(((GetPdu) requestPdu).getPath());
      } else {
        sendFile(
                Pdu.getExplodedLastElement(((GetPdu) requestPdu).getPath(), "\\\\"),
                ((GetPdu) requestPdu).getPath());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
