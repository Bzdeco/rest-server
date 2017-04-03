package pl.edu.agh.kis.florist.controller;

import com.sun.istack.internal.NotNull;
import org.mindrot.jbcrypt.BCrypt;
import pl.edu.agh.kis.florist.exceptions.FailedAuthenticationException;

import java.util.Base64;
import java.util.Scanner;

/**
 * This is an utility class for dealing with data provided during authentication process. It enables hashing passwords, comparing hashed password to plain text, decoding and encoding authentication credentials provided via HTTP Basic Authentication.
 * @throws FailedAuthenticationException exception throw if data provided via basic authentication was missing or formatted in a wrong way
 */
public class UserAuthenticationUtilities {
    /**
     * Hashes given plain text password using <code>BCrypt</code> class.
     * @param password plain text password
     * @return hash or provided password
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Checks if given plain text password matches with given password hash. Used to authenticate user after decoding password provided via basic authentication.
     * @param candidatePassword plain text password to be compared with password hash stored in database
     * @param storedHashedPassword password hash fetched from database to which we compare <code>candidatePassword</code>
     * @return <code>true</code> if passwords match, <code>false</code> false otherwise
     */
    public static boolean checkPassword(String candidatePassword, String storedHashedPassword) {
        return BCrypt.checkpw(candidatePassword, storedHashedPassword);
    }

    /**
     * Encodes authentication credentials using HTTP Basic Authentication provided by <code>Base64</code> class. It combines user login and password seperating them with semicolon and after hashing adding used method name followed by single space before encoded credentials.
     * @param login user login to encode
     * @param password user password to encode
     * @return text "Basic encodedCredentials", where encoded credentials is <code>String</code> "login:password" encoded by <code>Base64.Encoder</code>
     */
    public static String encodeAuthenticationCredentials(String login, String password) {
        String combined = login + ":" + password;

        Base64.Encoder encoder = Base64.getEncoder();
        String encoded = encoder.encodeToString(combined.getBytes());

        // Add authorization method info

        encoded = "Basic " + encoded;

        return encoded;
    }

    /**
     * Decodes authentication credentials sent to server. It uses <code>Base64.Decoder</code> which decodes credentails provided via HTTP Basic Authentication.
     * @param encodedCredentials encoded credentials in for of a text "Basic encodedCredentials", where encoded credentials is <code>String</code> "login:password" encoded by Basic Authentication
     * @return array of <code>length = 2</code> of decoded credentials, which first element is decoded plain text login and the second is decoded plain text password
     */
    public static String[] decodeAuthenticationCredentials(String encodedCredentials) {
        // Remove method information
        Scanner scanner = new Scanner(encodedCredentials);
        scanner.useDelimiter(" ");

        if(scanner.hasNext()) {
            // drop the method information
            scanner.next();
            // get the encoded login:password
            if(scanner.hasNext()) {
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] decodedBytes = decoder.decode(scanner.next());

                String decodedString = new String(decodedBytes);
                Scanner credentialsScanner = new Scanner(decodedString);
                credentialsScanner.useDelimiter(":");

                // get login
                if(credentialsScanner.hasNext()) {
                    String username = credentialsScanner.next();
                    // get password
                    if(credentialsScanner.hasNext()) {
                        String password = credentialsScanner.next();

                        String[] result = {username, password};
                        return result;
                    }
                    else
                        throw new FailedAuthenticationException("Password not specified");
                }
                else
                    throw new FailedAuthenticationException("Username not specified");
            }
            else
                throw new FailedAuthenticationException("Wrong authentication credentials format: user credentials not specified");
        }
        else {
            throw new FailedAuthenticationException("Wrong authentication credentials format: method information not specified");
        }
    }

}
