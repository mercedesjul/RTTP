package Protocol;

public class PutPdu extends Pdu implements Serializable {

    protected static final byte PDU_IDENTIFIER = 0x02;
    private String fileName;
    private int fileNameLength;
    private byte[] fileContent;


    public PutPdu(String fileName, byte[] fileContent) {
        this.fileName = fileName;
        fileNameLength = fileName.getBytes().length;
        this.fileContent = fileContent;
        length = fileContent.length;
        contentBytes = serialize();
    }

    public PutPdu(byte[] dataBytes) {
        PduDataParser parser = new PduDataParser(dataBytes);

        fileNameLength = parser.parse4ByteIntData();
        fileName = parser.parseStringData(fileNameLength);
        length = parser.parse4ByteIntData();
        fileContent = parser.parseByteData(length);

        contentBytes = serialize();
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileNameLength() {
        return fileNameLength;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    @Override
    public byte[] serialize() {
        return Pdu.concatByteArrays(
                getSuperHeader(),
                new byte[]{PDU_IDENTIFIER},
                Pdu.intToByteArray(fileNameLength),
                fileName.getBytes(),
                Pdu.intToByteArray(length),
                fileContent
        );
    }
}
