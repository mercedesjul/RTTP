package Sender;

import Protocol.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSender {

  private InputStream inputStream;
  private OutputStream outputStream;

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: filesender port");
    }

    int port = Integer.parseInt(args[0]);
    try {
      ServerSocket serverSocket = new ServerSocket(port);
      System.out.println("Awaiting connection on port " + port);
      Socket socket = serverSocket.accept();
      System.out.println("Accepted connection: " + socket.getInetAddress());
      FileSender fileSender = new FileSender(socket.getInputStream(), socket.getOutputStream());
      fileSender.awaitRequest();
    } catch (IOException e) {
      throw new RuntimeException(e);

    }
  }
  FileSender(InputStream inputStream, OutputStream outputStream){
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }
  public int sendFile(String fileName, String filePath) {
    PutPdu putPdu = null;
    try {
      byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
      if (fileBytes.length < 1) {
        byte errorCode = ErrorPdu.ERROR_CODES_MAPPING.get(ErrorPdu.ERROR_CODES.FILE_EMPTY);
        String errorString = ErrorPdu.ERROR_STRING_MAPPING.get(ErrorPdu.ERROR_CODES.FILE_EMPTY);
        ErrorPdu errorPdu = new ErrorPdu(errorCode,  errorString);
        errorPdu.send(outputStream);
        return 1;
      }
      putPdu = new PutPdu(fileName, fileBytes);
      System.out.printf("Sending %s with %d bytes", fileName, fileBytes.length);
      putPdu.send(outputStream);
    } catch (IOException e) {

      byte errorCode = ErrorPdu.ERROR_CODES_MAPPING.get(ErrorPdu.ERROR_CODES.FILE_NOT_FOUND);
      String errorString = ErrorPdu.ERROR_STRING_MAPPING.get(ErrorPdu.ERROR_CODES.FILE_NOT_FOUND);

      ErrorPdu errorPdu = new ErrorPdu(errorCode, errorString);
      System.out.printf("File %s not found, sending Error Code %d with Message %s", fileName, errorCode, errorString);
      errorPdu.send(outputStream);
      return 1;
    }
    return 0;
  }
  public void awaitRequest() {
    try {
      byte[] data;
      while(true) {
        if (inputStream.available() > 0) {
          data = inputStream.readNBytes(inputStream.available());
          break;
        }
      }
      GetPdu getPdu = (GetPdu) Pdu.createPduFromByteArray(data);
      System.out.printf("File %s got requested%n", getPdu.getFileName());
      sendFile(getPdu.getJustFileName(), getPdu.getFileName());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
