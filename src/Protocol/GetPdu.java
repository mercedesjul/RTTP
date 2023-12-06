package Protocol;

public class GetPdu extends Pdu implements Serializable {

  protected static final byte PDU_IDENTIFIER = 0x01;
  protected static final byte IS_FILE = 0x00;
  protected static final byte IS_DIRECTORY = 0x01;

  private String path;
  private boolean isDirectory;

  public GetPdu(String path, boolean isDirectory) {
    this.path = path;
    this.isDirectory = isDirectory;
    length = path.getBytes().length;
    contentBytes = serialize();
  }

  public GetPdu(byte[] dataBytes) {
    PduDataParser parser = new PduDataParser(dataBytes);
    isDirectory = parser.parseSingleByte() == IS_DIRECTORY;
    length = parser.parse4ByteIntData();
    path = parser.parseStringData(length);

    contentBytes = serialize();
  }
  public String getPath() {
    return path;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public String getJustFileName() {
    String[] pathParts = path.split("\\\\");
    return pathParts[pathParts.length - 1];
  }

  @Override
  public byte[] serialize() {
    return Pdu.concatByteArrays(
            Pdu.getSuperHeader(),
            new byte[]{PDU_IDENTIFIER},
            new byte[]{isDirectory ? IS_DIRECTORY : IS_FILE},
            Pdu.intToByteArray(length),
            path.getBytes()
    );
  }
}
