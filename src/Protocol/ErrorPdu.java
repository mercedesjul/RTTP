package Protocol;

import java.io.*;
import java.util.HashMap;

public class ErrorPdu extends Pdu implements Serializable {

  public enum ERROR_CODES {
    FILE_NOT_FOUND,
    FILE_EMPTY,
    NOT_A_DIRECTORY,
    PARSE_ERROR,
  }
  protected static final byte PDU_IDENTIFIER = 0x03;
  public static final HashMap<ERROR_CODES, Byte> ERROR_CODES_MAPPING = new HashMap<>(){{
    put(ERROR_CODES.FILE_NOT_FOUND, (byte) 0x1);
    put(ERROR_CODES.FILE_EMPTY, (byte) 0x2);
    put(ERROR_CODES.NOT_A_DIRECTORY, (byte) 0x3);
    put(ERROR_CODES.PARSE_ERROR, (byte) 0x4);
  }};
  public static final HashMap<ERROR_CODES, String> ERROR_STRING_MAPPING = new HashMap<>(){{
    put(ERROR_CODES.FILE_NOT_FOUND, "File not Found");
    put(ERROR_CODES.FILE_EMPTY, "File content is empty");
    put(ERROR_CODES.NOT_A_DIRECTORY, "Path is not a directory");
    put(ERROR_CODES.PARSE_ERROR, "Could not parse Pdu");

  }};

  private byte errorCode;

  private String errorString;

  public ErrorPdu(byte errorCode, String errorString) {
    this.errorCode = errorCode;
    this.errorString = errorString;
  }
  public ErrorPdu(ERROR_CODES errorCode) {
    this.errorCode = ERROR_CODES_MAPPING.get(errorCode);
    this.errorString = ERROR_STRING_MAPPING.get(errorCode);
  }

  public ErrorPdu(InputStream inputStream) throws IOException {
    DataInputStream dataInputStream = new DataInputStream(inputStream);
    this.errorCode = dataInputStream.readByte();
    this.errorString = dataInputStream.readUTF();
  }

  public byte getErrorCode() {
    return errorCode;
  }

  public String getErrorString() {
    return errorString;
  }

  @Override
  public void send(OutputStream outputStream) throws IOException {
    sendSuperHeader(outputStream);
    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
    dataOutputStream.writeByte(PDU_IDENTIFIER);
    dataOutputStream.writeByte(errorCode);
    dataOutputStream.writeUTF(errorString);
  }
}
