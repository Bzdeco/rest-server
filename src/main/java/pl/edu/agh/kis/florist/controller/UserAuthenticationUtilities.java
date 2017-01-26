package pl.edu.agh.kis.florist.controller;

import com.sun.istack.internal.NotNull;
import org.mindrot.jbcrypt.BCrypt;
import pl.edu.agh.kis.florist.exceptions.FailedAuthenticationException;

import java.util.Base64;
import java.util.Scanner;

/**
 * Created by bzdeco on 23.01.17.
 */
public class UserAuthenticationUtilities {
    @NotNull
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String candidatePassword, String storedHashedPassword) {
        return BCrypt.checkpw(candidatePassword, storedHashedPassword);
    }

    public static String encodeAuthenticationCredentials(String login, String password) {
        String combined = login + ":" + password;

        Base64.Encoder encoder = Base64.getEncoder();
        String encoded = encoder.encodeToString(combined.getBytes());

        // Add authorization method info

        encoded = "Basic " + encoded;

        return encoded;
    }

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
