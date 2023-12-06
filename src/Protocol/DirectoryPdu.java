package Protocol;

import java.io.IOException;

public class DirectoryPdu extends Pdu implements Serializable {

  protected static final byte PDU_IDENTIFIER = 0x04;
  private static final byte FILENAME_DELIMITER = 0x0;

  private String directoryName;

  private String[] filePaths = new String[0];

  private int fileCount = 0;

  public DirectoryPdu(String directoryName, String[] filePaths) throws IOException {
    this.fileCount = filePaths.length;
    this.directoryName = directoryName;
    this.filePaths = filePaths;
    length = directoryName.getBytes().length;
    contentBytes = serialize();
  }

  public DirectoryPdu(byte[] dataBytes) {
    PduDataParser parser = new PduDataParser(dataBytes);
    length = parser.parse4ByteIntData();
    directoryName = parser.parseStringData(length);
    fileCount = parser.parse4ByteIntData();
    filePaths = constructFilePath(parser);

    contentBytes = serialize();
  }

  public String[] getFilePaths() {
    return filePaths;
  }

  public String getDirectoryName() {
    return directoryName;
  }

  private String[] constructFilePath(PduDataParser parser) {
    String[] filePaths = new String[0];
    byte[][] fileNameBytes = parser.readBytesToDelimiterRepeated(FILENAME_DELIMITER, fileCount);
    for (byte[] b : fileNameBytes) {
      filePaths = addStringElement(filePaths, new String(b));
    }
    return filePaths;
  }

  private byte[] filePathsToByteArray() {
    byte[] bytes = new byte[0];
    byte[] delimiterByte = new byte[]{FILENAME_DELIMITER};
    for (String filePath: filePaths) {
      bytes = concatByteArrays(bytes, filePath.getBytes(), delimiterByte);
    }
    return bytes;
  }

  @Override
  public byte[] serialize() {
    return Pdu.concatByteArrays(
            Pdu.getSuperHeader(),
            new byte[]{PDU_IDENTIFIER},
            Pdu.intToByteArray(length),
            directoryName.getBytes(),
            Pdu.intToByteArray(fileCount),
            filePathsToByteArray()
    );
  }
}
