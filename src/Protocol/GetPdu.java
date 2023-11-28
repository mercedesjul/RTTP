package Protocol;

public class GetPdu extends Pdu implements Serializable {

  protected static final byte PDU_IDENTIFIER = 1;

  private String fileName;


  public GetPdu(String filename) {
    fileName = filename;
    length = filename.getBytes().length;
    contentBytes = serialize();
  }

  public GetPdu(byte[] dataBytes) {
    byte[] lengthBytes = new byte[4];
    System.arraycopy(dataBytes, 0, lengthBytes, 0, lengthBytes.length);
    length = Pdu.byteArrayToInt(lengthBytes);

    byte[] filenameBytes = new byte[length];
    System.arraycopy(dataBytes, lengthBytes.length, filenameBytes, 0, length);
    fileName = new String(filenameBytes);

    contentBytes = serialize();
  }
  public String getFileName() {
    return fileName;
  }

  public String getJustFileName() {
    String[] pathParts = fileName.split("\\\\");
    return pathParts[pathParts.length - 1];
  }

  @Override
  public byte[] serialize() {
    return Pdu.concatByteArrays(
            Pdu.getSuperHeader(),
            new byte[]{PDU_IDENTIFIER},
            Pdu.intToByteArray(length),
            fileName.getBytes()
    );
  }
}
