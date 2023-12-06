package Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.MalformedParametersException;
import java.nio.file.Path;
import java.util.Arrays;

abstract public class Pdu {
  // Bitmask for signing ints
  private static final int BITMASK = 0xFF;

  // Identifier and version information
  protected static final byte IDENTIFIER = 0x01;
  protected static final byte MAJOR_VERSION = 0x00;
  protected static final byte MINOR_VERSION = 0x01;
  protected static final byte PATCH_VERSION = 0x01;

  protected int length;
  protected byte[] contentBytes;


  /**
   * @param outputStream the output stream to write to
   * @return 0 on success, 1 on failure
   * @throws RuntimeException if the content of the pdu has not been initialized / is null
   */
  public int send(OutputStream outputStream) throws RuntimeException {
    try {
      if (contentBytes == null) {
        throw new RuntimeException("Content has not been initialized");
      }
      outputStream.write(contentBytes);
      outputStream.flush();
      return 0;
    } catch (IOException e) {
      return -1;
    }
  }

  /**
   * @return The default header bytes
   */
  public static byte[] getSuperHeader() {
    return new byte[]{
      IDENTIFIER,MAJOR_VERSION,MINOR_VERSION,PATCH_VERSION
    };
  }

  public void setContentBytes(byte[] data) {
    contentBytes = data;
  }

  /**
   * @param integer an Integer to be converted to a byte array
   * @return a byte array representing the given Integer
   */
  public static byte[] intToByteArray(int integer) {
    return new byte[]{
      (byte) (integer >> 24),
      (byte) (integer >> 16),
      (byte) (integer >> 8),
      (byte) integer,
    };
  }

  /**
   * @param bytes bytes representing an Integer
   * @return the represented Integer
   */
  public static int byteArrayToInt(byte[] bytes) {
    if (bytes.length != 4) {
      throw new IllegalArgumentException(String.format("Byte array expected to have 4 values, got %d", bytes.length));
    }
    return (bytes[0] << 24) | ((bytes[1] & BITMASK) << 16) | ((bytes[2] & BITMASK) << 8) | (bytes[3] & BITMASK);
  }

  /**
   * @param byteArrays Array of byte Array to concat
   * @return A byte Array consisting of all provided arrays concat together
   */
  public static byte[] concatByteArrays(byte[] ...byteArrays) {
    byte[] byteArray = new byte[0];
    for (byte[] b : byteArrays) {
      byteArray = Arrays.copyOf(byteArray, byteArray.length + b.length);
      System.arraycopy(b, 0, byteArray, byteArray.length - b.length, b.length);
    }
    return byteArray;
  }

  public static String[] addStringElement(String[] array, String string) {
    String[] newArray = Arrays.copyOf(array, array.length + 1);
    newArray[array.length] = string;
    return newArray;
  }
  public static byte[] addByteElement(byte[] array, byte b) {
    byte[] newArray = Arrays.copyOf(array, array.length + 1);
    newArray[array.length] = b;
    return newArray;
  }


  /**
   * @param bytes Bytes representing a PDU
   * @return the respective Protocol.Pdu with its data filled
   * @throws IllegalStateException if the given byte array can not be parsed into a PDU
   */
  public static Pdu createPduFromByteArray(byte[] bytes) throws IllegalStateException {
    if (bytes[0] != IDENTIFIER) {
      throw new MalformedParametersException("Bytes do not start with identifier bit");
    }
    if (bytes[1] != MAJOR_VERSION || bytes[2] != MINOR_VERSION || bytes[3] != PATCH_VERSION) {
      throw new MalformedParametersException("Version mismatch");
    }
    byte[] dataBytes = new byte[bytes.length - 5];
    System.arraycopy(bytes, 5, dataBytes, 0, dataBytes.length);
    return switch (bytes[4]) {
      case GetPdu.PDU_IDENTIFIER -> new GetPdu(dataBytes);
      case PutPdu.PDU_IDENTIFIER -> new PutPdu(dataBytes);
      case ErrorPdu.PDU_IDENTIFIER -> new ErrorPdu(dataBytes);
      case DirectoryPdu.PDU_IDENTIFIER -> new DirectoryPdu(dataBytes);

      default -> throw new IllegalStateException("PduIdentifier not recognized: " + bytes[4]);
    };
  }
}
