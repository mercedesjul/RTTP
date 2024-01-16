package Protocol;

import java.io.*;

public class PutPdu extends Pdu implements Serializable {

    protected static final byte PDU_IDENTIFIER = 0x02;
    private String fileName;
    private long fileLength;
    private InputStream contentInputStream;


    public PutPdu(String fileName, int fileLength, InputStream contentInputStream) {
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.contentInputStream = contentInputStream;
    }

    public PutPdu(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        this.fileName = dataInputStream.readUTF();
        this.fileLength = dataInputStream.readLong();
        this.contentInputStream = inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public InputStream getContentInputStream() {
        return contentInputStream;
    }

    @Override
    public void send(OutputStream outputStream) throws IOException {
        sendSuperHeader(outputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeByte(PDU_IDENTIFIER);
        dataOutputStream.writeUTF(fileName);
        dataOutputStream.writeLong(fileLength);
        stream(outputStream, contentInputStream, fileLength);
    }
}
