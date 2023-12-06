package FileThreads;

import Protocol.ErrorPdu;
import Protocol.ErrorPdu.ERROR_CODES;

import java.io.IOException;
import java.net.Socket;

abstract public class FileThread implements Runnable {
  Socket socket;

  public FileThread(Socket socket) {
    this.socket = socket;
  }
  @Override
  public void run() {

  }

  ErrorPdu sendErrorPdu(ERROR_CODES errorCode) {
    ErrorPdu errorPdu = new ErrorPdu(errorCode);
    try {
      errorPdu.send(socket.getOutputStream());
    } catch (IOException ex) {
      throw new RuntimeException("IO Exception whilst sending ErrorPdu");
    }
    return errorPdu;
  }
}
