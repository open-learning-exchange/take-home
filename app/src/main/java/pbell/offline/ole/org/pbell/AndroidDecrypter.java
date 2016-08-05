package pbell.offline.ole.org.pbell;

        import android.app.Activity;
        import com.chilkatsoft.*;

        import android.util.Log;
        import android.widget.TextView;
        import android.os.Bundle;

        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;

public class AndroidDecrypter {

    private static final String TAG = "Chilkat";

    // Called when the activity is first created.
    AndroidDecrypter(){
        CkCrypt2 crypt = new CkCrypt2();

        boolean success = crypt.UnlockComponent("D");
        if (success != true) {
            Log.i(TAG, crypt.lastErrorText());
            return;
        }

        String hexKey;
        //  http://www.di-mgt.com.au/cryptoKDFs.html#examplespbkdf

        String pw = "password";
        String pwCharset = "ansi";
        //  Hash algorithms may be: sha1, md2, md5, etc.
        String hashAlg = "sha1";
        //  The salt should be 8 bytes:
        String saltHex = "56942958ed379a8c7d4a03a701814561";
        ///String saltHex = "56942958ed379a8c7d4a03a701814561";
        int iterationCount = 100;
        //int iterationCount = 2048;
        //  Derive a 192-bit key from the password.
        //int outputBitLen = 192;
        int outputBitLen = 160;

        String md5ofLogin = md5("leomaxi");

        Log.i(TAG, "MD5 = "+ md5ofLogin);

        //  The derived key is returned as a hex or base64 encoded string.
        //  (Note: The salt argument must be a string that also uses
        //  the same encoding.)
        String enc = "hex";

        ///hexKey = crypt.pbkdf2(pw,pwCharset,hashAlg,saltHex,iterationCount,outputBitLen,enc);
        for(int m=0;m<=1000;m++) {
            hexKey = crypt.pbkdf2(pw, pwCharset, hashAlg, saltHex, m, outputBitLen, enc);
            Log.i(TAG, hexKey);
        }

        //  The output should have this value:
        //  BFDE6BE94DF7E11DD409BCE20A0255EC327CB936FFE93643

    }

    static {
        // Important: Make sure the name passed to loadLibrary matches the shared library
        // found in your project's libs/armeabi directory.
        //  for "libchilkat.so", pass "chilkat" to loadLibrary
        //  for "libchilkatemail.so", pass "chilkatemail" to loadLibrary
        //  etc.
        //
        System.loadLibrary("chilkat");

        // Note: If the incorrect library name is passed to System.loadLibrary,
        // then you will see the following error message at application startup:
        //"The application <your-application-name> has stopped unexpectedly. Please try again."
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}