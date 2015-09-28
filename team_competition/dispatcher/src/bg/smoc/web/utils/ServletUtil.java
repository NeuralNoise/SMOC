/**
 * 
 */
package bg.smoc.web.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class ServletUtil {
    static public boolean isCheckboxSelected(String parameter) {
        if (parameter == null)
            return false;
        String value = parameter.toLowerCase();
        return ("on".equals(value) || "yes".equals(value) || "true".equals(value));
    }

    public static String encryptPassword(String password) {
        try {
            return new BASE64Encoder().encode(MessageDigest.getInstance("SHA1").digest(password
                    .getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
