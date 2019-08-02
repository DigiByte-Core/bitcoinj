package co.digibyte.crypto;

import co.digibyte.cryptohash.Groestl512;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Hash Engineering on 4/24/14 for the Groestl algorithm
 */
public class Groestl {

    private static final Logger log = LoggerFactory.getLogger(Groestl.class);
    private static boolean native_library_loaded = false;
    private static final MessageDigest digestSHA256;
    private static final Groestl512 digestGroestl = new Groestl512();

    public static byte[] sha256_digest(byte[] input, int offset, int length) {
        synchronized (digestSHA256) {
            digestSHA256.reset();
            digestSHA256.update(input, offset, length);
            return digestSHA256.digest();
        }
    }
    static {

            try {
                digestSHA256 = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);  // Can't happen.
            }

        try {
            System.loadLibrary("groestld");
            native_library_loaded = true;
        }
        catch(UnsatisfiedLinkError x)
        {
            native_library_loaded = false;
        }
        catch(Exception e)
        {
            native_library_loaded = false;
        }
    }

    public static byte[] digest(byte[] input, int offset, int length)
    {
        //return groestl(input, offset, length);
        try {
            return native_library_loaded ? groestld_native(input, offset, length) : groestl(input, offset, length);
            /*byte[] r1 = groestld_native(input, offset, length);
            byte[] r2 = groestl(input, offset, length);
            if(r1.equals(r2))
            {
                int x = 0;
                ++x;
            }
            return r2;*/
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("X11 Hash time: {} ms per block", time);
        }
    }

    public static byte[] digest(byte[] input) {
        //long start = System.currentTimeMillis();
        try {
            return native_library_loaded ? groestld_native(input, 0, input.length) : groestl(input);
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("X11 Hash time: {} ms per block", time);
        }

        //return groestl(input);
    }

    static native byte [] groestld_native(byte [] input, int offset, int len);

    static byte [] groestl(byte header[])
    {
        //digestGroestl.reset();
        //byte [] hash512 = digestGroestl.digest(header);
        //digestGroestl.reset();
        //byte [] doubleHash512 = digestGroestl.digest(hash512);
        //Initialize
        //return new Sha512Hash(doubleHash512).trim256().getBytes();

        Groestl512 hasher1 = new Groestl512();
        Groestl512 hasher2 = new Groestl512();

        /*digestGroestl.reset();
        byte [] hash512 = digestGroestl.digest(header);
        //digestGroestl.reset();
        byte [] doubleHash512 = digestGroestl.digest(hash512);
        //Initialize
        return new Sha512Hash(doubleHash512).trim256().getBytes();
        */
        byte [] hash1 = hasher1.digest(header);
        byte [] hash2 = hasher2.digest(hash1);
        return new Sha512Hash(hash2).trim256().getBytes();
    }

    static byte [] groestl(byte header[], int offset, int length)
    {
        digestGroestl.reset();
        digestGroestl.update(header, offset, length);
        byte [] hash512 = digestGroestl.digest();

        //digestGroestl.update(hash512);
        Sha512Hash doubleHash512 = new Sha512Hash(digestGroestl.digest(hash512));
        //Initialize

        return doubleHash512.trim256().getBytes();
    }

}
