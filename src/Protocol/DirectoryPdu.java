package Protocol;

import java.io.*;

public class DirectoryPdu extends Pdu  {

  protected static final byte PDU_IDENTIFIER = 0x04;

  private String directoryName;
  private final long fileCount;

  private String[] filePaths = new String[0];

  private final long subDirectoryCount;

  private String[] subDirectoryPaths = new String[0];


  public DirectoryPdu(String directoryName, String[] filePaths, String[] subDirectoryPaths) throws IOException {
    this.directoryName = directoryName;
    this.fileCount = filePaths.length;
    this.filePaths = filePaths;
    this.subDirectoryCount = subDirectoryPaths.length;
    this.subDirectoryPaths = subDirectoryPaths;

  }

  public DirectoryPdu(InputStream inputStream) throws IOException {
    DataInputStream dataInputStream = new DataInputStream(inputStream);
    directoryName = dataInputStream.readUTF();
    fileCount = dataInputStream.readLong();
    for (int i = 0; i < fileCount; i++) {
      filePaths = addStringElement(filePaths, dataInputStream.readUTF());
    }
    subDirectoryCount = dataInputStream.readLong();
    for (int i = 0; i < subDirectoryCount; i++) {
      subDirectoryPaths =  addStringElement(subDirectoryPaths, dataInputStream.readUTF());
    }

  }

  public String[] getFilePaths() {
    return filePaths;
  }
  public String[] getSubDirectoryPaths() {
    return subDirectoryPaths;
  }

  public String getDirectoryName() {
    return directoryName;
  }

  @Override
  public void send(OutputStream outputStream) throws IOException {
    sendSuperHeader(outputStream);
    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
    dataOutputStream.writeByte(PDU_IDENTIFIER);
    dataOutputStream.writeUTF(directoryName);
    dataOutputStream.writeLong(fileCount);
    for (String filePath : filePaths) {
      dataOutputStream.writeUTF(filePath);
    }
    dataOutputStream.writeLong(subDirectoryCount);
    for (String directoryPath : subDirectoryPaths) {
      dataOutputStream.writeUTF(directoryPath);
    }
  }
}
