package nav.com.nfproject1;

import java.util.Random;

/**
 * Created by naveensingh on 14/05/16.
 */
public class Utils {

    public static  String getRandomNumberString(int length) {
        if (length<1)
            return null;

        final String SALTCHARS = "1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}
