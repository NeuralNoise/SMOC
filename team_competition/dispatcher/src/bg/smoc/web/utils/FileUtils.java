package bg.smoc.web.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * 
     * @param inputStream
     * @param outputStream is being closed
     * @return
     */
    public static boolean copyStreams(InputStream inputStream, OutputStream outputStream) {
        byte[] readBuffer = new byte[1024];
        int read = -1;
        try {
            while ((read = inputStream.read(readBuffer)) > 0) {
                outputStream.write(readBuffer, 0, read);
            }
            outputStream.close();
        } catch (IOException e) {
            return false;
        }
        if (read != -1) {
            return false;
        }
        return true;
    }
}
