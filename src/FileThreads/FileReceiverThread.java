package FileThreads;

import Protocol.ErrorPdu;
import Protocol.GetPdu;
import Protocol.Pdu;
import Protocol.PutPdu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class FileReceiverThread implements Runnable {

  Socket socket;
  boolean isDirectory;
  String filePath;
  String savePath;

  public FileReceiverThread(Socket socket, String filePath, String savePath) {
    this.socket = Objects.requireNonNull(socket);
    this.filePath = filePath;
    this.savePath = savePath;
  }

  public int receiveFile() {
    try {
      Pdu responsePdu = sendGetPdu();
      if (responsePdu instanceof ErrorPdu) {
        System.out.printf(
                "Error code %d occurred with Error Message \"%s\"",
                ((ErrorPdu) responsePdu).getErrorCode(),
                ((ErrorPdu) responsePdu).getErrorString()
        );
        return -1;
      }
      FileOutputStream fileOutputStream = new FileOutputStream(savePath);
      System.out.printf("Writing to %s%n", savePath);
      fileOutputStream.write(((PutPdu) responsePdu).getFileContent());
      fileOutputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return 0;
  }

  public Pdu sendGetPdu() {
    byte[] data;
    try {
      (new GetPdu(filePath, isDirectory)).send(socket.getOutputStream());
      while (true) {
        if (socket.getInputStream().available() > 0) {
          data = socket.getInputStream().readNBytes(socket.getInputStream().available());
          break;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Pdu.createPduFromByteArray(data);
  }

  @Override
  public void run() {
    receiveFile();
  }
}
