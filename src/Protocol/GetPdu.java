package Protocol;

import java.io.*;

public class GetPdu extends Pdu {

  protected static final byte PDU_IDENTIFIER = 0x01;
  protected static final byte IS_FILE = 0x00;
  protected static final byte IS_DIRECTORY = 0x01;

  private String path;
  private boolean isDirectory;

  public GetPdu(String path, boolean isDirectory) {
    this.path = path;
    this.isDirectory = isDirectory;
  }

  public GetPdu(InputStream inputStream) throws IOException {
    DataInputStream dataBytes = new DataInputStream(inputStream);
    isDirectory = dataBytes.readByte() == IS_DIRECTORY;
    path = dataBytes.readUTF();
  }
  public String getPath() {
    return path;
  }

  public boolean isDirectory() {
    return isDirectory;
  }



  @Override
  public void send(OutputStream outputStream) throws IOException {
    sendSuperHeader(outputStream);
    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
    dataOutputStream.writeByte(PDU_IDENTIFIER);
    dataOutputStream.write(isDirectory ? IS_DIRECTORY : IS_FILE);
    dataOutputStream.writeUTF(path);
  }
}
