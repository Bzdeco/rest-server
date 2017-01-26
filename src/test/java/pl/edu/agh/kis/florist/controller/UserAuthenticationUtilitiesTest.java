package pl.edu.agh.kis.florist.controller;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bzdeco on 23.01.17.
 */
public class UserAuthenticationUtilitiesTest {
    @Test
    public void encodeAuthenticationCredentials() throws Exception {
        String login = "Alladin";
        String password = "OpenSesame";

        String encoded = UserAuthenticationUtilities.encodeAuthenticationCredentials(login, password);
        System.out.println(encoded);
    }

    @Test
    public void decodeAuthenticationCredentials() throws Exception {
        String login = "Alladin";
        String password = "OpenSesame";

        String encoded = UserAuthenticationUtilities.encodeAuthenticationCredentials(login, password);
        String[] decoded = UserAuthenticationUtilities.decodeAuthenticationCredentials(encoded);

        assertThat(decoded).containsExactly(login, password);
    }

    @Test
    public void hashPassword() {
        System.out.println(UserAuthenticationUtilities.hashPassword("123"));
    }

}