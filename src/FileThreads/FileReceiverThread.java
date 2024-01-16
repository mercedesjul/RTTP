package FileThreads;

import Protocol.ErrorPdu;
import Protocol.GetPdu;
import Protocol.Pdu;
import Protocol.PutPdu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FileReceiverThread extends FileThread implements Runnable {
  boolean isDirectory;
  String filePath;
  String savePath;

  public FileReceiverThread(Socket socket, String filePath, String savePath) {
    super(socket);
    this.filePath = filePath;
    this.savePath = savePath;
  }

  public int receiveFile() {
    try {
      Pdu responsePdu = sendGetPdu();
      if (responsePdu instanceof ErrorPdu) {
        System.out.printf(
                "%sError code %d occurred with Error Message \"%s\"",
                getThreadIdStringPrefix(),
                ((ErrorPdu) responsePdu).getErrorCode(),
                ((ErrorPdu) responsePdu).getErrorString()
        );
        return -1;
      }
      PutPdu putPdu = ((PutPdu) responsePdu);
      FileOutputStream fileOutputStream = new FileOutputStream(savePath);
      System.out.printf("%sWriting to %s%n", getThreadIdStringPrefix(), savePath);
      long start = System.currentTimeMillis();
      Pdu.stream(fileOutputStream, putPdu.getContentInputStream(), putPdu.getFileLength());
      long end = System.currentTimeMillis();
      System.out.printf("%sFinished writing to %s in %s milliseconds%n", getThreadIdStringPrefix(), savePath, end - start);
      fileOutputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return 0;
  }

  public Pdu sendGetPdu() {
    try {
      (new GetPdu(filePath, isDirectory)).send(socket.getOutputStream());
      return Pdu.createPduFromInputStream(socket.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
  @Override
  public void run() {
    receiveFile();
  }
}
