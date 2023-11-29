package Protocol;

public class PduDataParser {
  private int offset;
  private final byte[] data;

  public PduDataParser(byte[] data) {
    this.data = data;
    this.offset = 0;
  }

  public byte parseSingleByte() {
    return data[offset++];
  }
  public byte[] parseByteData(int length) {
    byte[] data = new byte[length];
    System.arraycopy(this.data, offset, data, 0, length);
    offset += length;
    return data;
  }

  public int parse4ByteIntData() {
    return Pdu.byteArrayToInt(parseByteData(4));
  }

  public String parseStringData(int length) {
    return new String(parseByteData(length));
  }
}
