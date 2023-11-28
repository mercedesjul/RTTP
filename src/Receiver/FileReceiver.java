package Receiver;

import Protocol.ErrorPdu;
import Protocol.GetPdu;
import Protocol.Pdu;
import Protocol.PutPdu;

import java.io.*;
import java.net.Socket;

public class FileReceiver {
  InputStream inputStream;
  OutputStream outputStream;

  public static void main(String[] args) {
    if (args.length < 4) {
      System.out.println("Usage: filereceiver request_path save_path host port");
    }
    String request_path = args[0];
    String save_path = args[1];
    String host = args[2];
    int port = Integer.parseInt(args[3]);
    Socket socket = null;
    try {
      socket = new Socket(host, port);
      FileReceiver fileReceiver = new FileReceiver(socket.getInputStream(), socket.getOutputStream());
      fileReceiver.receiveFile(request_path, save_path);
    } catch (IOException e) {
      System.out.println("Host not found");
      System.exit(1);
    }
  }

  FileReceiver(InputStream inputStream, OutputStream outputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  public int receiveFile(String filePath, String savePath) {
    try {
      (new GetPdu(filePath)).send(outputStream);
      byte[] data;
      while (true) {
        if (inputStream.available() > 0) {
          data = inputStream.readNBytes(inputStream.available());
          break;
        }
      }
      Pdu responsePdu = Pdu.createPduFromByteArray(data);
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

}
