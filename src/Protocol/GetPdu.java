package Protocol;

public class GetPdu extends Pdu implements Serializable {

  protected static final byte PDU_IDENTIFIER = 0x01;

  private String fileName;

  public GetPdu(String filename) {
    fileName = filename;
    length = filename.getBytes().length;
    contentBytes = serialize();
  }

  public GetPdu(byte[] dataBytes) {
    PduDataParser parser = new PduDataParser(dataBytes);

    length = parser.parse4ByteIntData();
    fileName = parser.parseStringData(length);

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
