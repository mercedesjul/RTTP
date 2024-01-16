package Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract public class Pdu {

  // Identifier and version information
  protected static final byte IDENTIFIER = 0x01;
  protected static final byte MAJOR_VERSION = 0x00;
  protected static final byte MINOR_VERSION = 0x01;
  protected static final byte PATCH_VERSION = 0x01;

  protected static final byte[] HEADER_BYTES = new byte[]{
    IDENTIFIER,MAJOR_VERSION,MINOR_VERSION,PATCH_VERSION
  };

  /**
   * @param outputStream the output stream to write to
   * @throws RuntimeException if the content of the pdu has not been initialized / is null
   */
  public static void sendSuperHeader(OutputStream outputStream) throws IOException {
    outputStream.write(HEADER_BYTES);
  }

  abstract public void send(OutputStream outputStream) throws IOException;

  public static void stream(OutputStream outputStream, InputStream inputStream, long length) throws IOException {
    ArrayList<Integer> ints = longToIntsChunks(length);
    for (int i : ints) {
      outputStream.write(inputStream.readNBytes(i));
    }
  }

  public static String[] addStringElement(String[] array, String string) {
    String[] newArray = Arrays.copyOf(array, array.length + 1);
    newArray[array.length] = string;
    return newArray;
  }

  /**
   * @param l long to convert to int chunks
   * @return List of Integers representing the given long
   */
  public static ArrayList<Integer> longToIntsChunks(long l){
    ArrayList<Integer> chunks = new ArrayList<>();
    while (l > 0) {
      if (l > Integer.MAX_VALUE) {
        chunks.add(Integer.MAX_VALUE);
        l -= Integer.MAX_VALUE;
      } else {
        chunks.add((int) l);
        l = 0;
      }
    }
    return chunks;
  }

  public static String getExplodedLastElement(String string, String delimiter) {
    String[] pathParts = string.split(delimiter);
    return pathParts[pathParts.length - 1];
  }

  /**
   * @param inputStream representing a PDU
   * @return the respective Protocol.Pdu with its data filled
   * @throws IllegalStateException if the given byte array can not be parsed into a PDU
   */
  public static Pdu createPduFromInputStream(InputStream inputStream) throws IllegalStateException, IOException {
    if (inputStream.read() != IDENTIFIER) {
      throw new MalformedParametersException("Bytes do not start with identifier bit");
    }
    if (inputStream.read() != MAJOR_VERSION || inputStream.read() != MINOR_VERSION || inputStream.read() != PATCH_VERSION) {
      throw new MalformedParametersException("Version mismatch");
    }
    int id;
    return switch (id = inputStream.read()) {
      case GetPdu.PDU_IDENTIFIER -> new GetPdu(inputStream);
      case PutPdu.PDU_IDENTIFIER -> new PutPdu(inputStream);
      case ErrorPdu.PDU_IDENTIFIER -> new ErrorPdu(inputStream);
      case DirectoryPdu.PDU_IDENTIFIER -> new DirectoryPdu(inputStream);

      default -> throw new IllegalStateException("PduIdentifier not recognized: " + id);
    };
  }
}
