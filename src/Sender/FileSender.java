package Sender;

import FileThreads.FileSenderThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FileSender {

  private final ServerSocket serverSocket;
  private final ArrayList<Thread> connections = new ArrayList<>();

  public static void main(String[] args) throws IOException{
    if (args.length < 1) {
      System.out.println("Usage: filesender port");
    }

    int port = Integer.parseInt(args[0]);
    FileSender fileSender = new FileSender(port);
    fileSender.awaitConnections();
  }
  FileSender(int port) {
    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  public void awaitConnections() {
    try {
      System.out.println("Awaiting connection on port " + serverSocket.getLocalPort() + '\n');
      while(true) {
        Socket socket = serverSocket.accept();
        System.out.println("Accepted connection: " + socket.getInetAddress());
        FileSenderThread fileSenderThread = new FileSenderThread(socket);
        Thread threadWrapper = new Thread(fileSenderThread);
        threadWrapper.start();
        connections.add(threadWrapper);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }


  }

}
