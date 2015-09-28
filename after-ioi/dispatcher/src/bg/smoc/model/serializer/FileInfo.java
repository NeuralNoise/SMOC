package bg.smoc.model.serializer;

import java.io.InputStream;

public class FileInfo {

    private InputStream inputStream;

    private long size;

    private String absolutePath;

    FileInfo(InputStream stream, long size, String path) {
        inputStream = stream;
        this.size = size;
        absolutePath = path;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

}
