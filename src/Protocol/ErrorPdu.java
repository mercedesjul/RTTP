package Protocol;

import java.util.HashMap;

public class ErrorPdu extends Pdu implements Serializable {

  public enum ERROR_CODES {
    FILE_NOT_FOUND,
    FILE_EMPTY,
  }
  protected static final byte PDU_IDENTIFIER = 0x03;
  public static final HashMap<ERROR_CODES, Byte> ERROR_CODES_MAPPING = new HashMap<>(){{
    put(ERROR_CODES.FILE_NOT_FOUND, (byte) 0x1);
    put(ERROR_CODES.FILE_EMPTY, (byte) 0x2);
  }};
  public static final HashMap<ERROR_CODES, String> ERROR_STRING_MAPPING = new HashMap<>(){{
    put(ERROR_CODES.FILE_NOT_FOUND, "File not Found");
    put(ERROR_CODES.FILE_EMPTY, "File content is empty");
  }};

  private byte errorCode;

  private String errorString;

  public ErrorPdu(byte errorCode, String errorString) {
    this.errorCode = errorCode;
    this.errorString = errorString;
    length = errorString.getBytes().length;
    contentBytes = serialize();
  }

  public ErrorPdu(byte[] dataBytes) {
    errorCode = dataBytes[0];
    byte[] lengthBytes = new byte[4];
    System.arraycopy(dataBytes, 1, lengthBytes, 0, lengthBytes.length);
    length = Pdu.byteArrayToInt(lengthBytes);

    byte[] errorStringBytes = new byte[length];
    System.arraycopy(dataBytes, lengthBytes.length + 1, errorStringBytes, 0, length);
    errorString = new String(errorStringBytes);

    contentBytes = serialize();
  }

  public byte getErrorCode() {
    return errorCode;
  }

  public String getErrorString() {
    return errorString;
  }

  @Override
  public byte[] serialize() {
    return Pdu.concatByteArrays(
            Pdu.getSuperHeader(),
            new byte[]{PDU_IDENTIFIER},
            new byte[]{errorCode},
            Pdu.intToByteArray(length),
            errorString.getBytes()
    );
  }
}
