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
  public byte[] readBytesToDelimiter(byte delimiter) {
    byte[] bytes = new byte[0];
    byte b;
    while ((b = parseSingleByte()) != delimiter) {
      bytes = Pdu.addByteElement(bytes, b);
    }
    return bytes;
  }

  public byte[][] readBytesToDelimiterRepeated(byte delimiter, int repeat_count) {
    byte[][] bytes = new byte[repeat_count][];
    for (int i = 0; repeat_count > 0; i++, repeat_count--){
      bytes[i] = readBytesToDelimiter(delimiter);
    }
    return bytes;
  }

  public int parse4ByteIntData() {
    return Pdu.byteArrayToInt(parseByteData(4));
  }

  public String parseStringData(int length) {
    return new String(parseByteData(length));
  }
}
