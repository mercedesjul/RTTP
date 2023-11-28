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

        // Length of Filename
        byte[] fileNameLengthBytes = new byte[4];
        System.arraycopy(dataBytes, 0, fileNameLengthBytes, 0, fileNameLengthBytes.length);
        fileNameLength = byteArrayToInt(fileNameLengthBytes);

        // Filename
        byte[] fileNameBytes = new byte[fileNameLength];
        System.arraycopy(dataBytes, fileNameLengthBytes.length, fileNameBytes, 0, fileNameLength);
        fileName = new String(fileNameBytes);

        // Length of data
        byte[] lengthBytes = new byte[4];
        System.arraycopy(dataBytes, fileNameLengthBytes.length + fileNameLength, lengthBytes, 0, lengthBytes.length);
        length = byteArrayToInt(lengthBytes);

        // Data
        byte[] fileContentBytes = new byte[length];
        System.arraycopy(dataBytes, fileNameLengthBytes.length + fileNameLength + lengthBytes.length, fileContentBytes,0, length);
        fileContent = fileContentBytes;

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
