package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pl.edu.agh.kis.florist.db.tables.pojos.Users;
import pl.edu.agh.kis.florist.exceptions.FailedAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.edu.agh.kis.florist.db.Tables.SESSION_DATA;
import static pl.edu.agh.kis.florist.db.Tables.USERS;

/**
 * Created by bzdeco on 26.01.17.
 */
public class SessionDataDAOTest {
    private final String DB_URL = "jdbc:sqlite:test.db";
    private DSLContext create;

    @Before
    public void setup() {
        create = DSL.using(DB_URL);
        create.deleteFrom(USERS).execute();
        create.deleteFrom(SESSION_DATA).execute();
    }

    @After
    public void tearDown() {
        create.deleteFrom(USERS).execute();
        create.deleteFrom(SESSION_DATA).execute();
        create.close();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createSessionIdForUser() throws Exception {
        Users user = new Users(1, "kuba", "Kuba", "$2a$10$gBv6DJCxZnifg3yaTLCDv.oAQ7ASoeNkTYymqj0c1r//rJpED61Zm"); //pass: 123
        UsersDAO usersDAO = new UsersDAO();
        SessionDataDAO sessionDataDAO = new SessionDataDAO();

        usersDAO.create(user);
        sessionDataDAO.createSessionIdForUser(user);
    }

    @Test
    public void getSessionIdOfLoggedUser() throws Exception {
        Users user = new Users(1, "kuba", "Kuba", "$2a$10$gBv6DJCxZnifg3yaTLCDv.oAQ7ASoeNkTYymqj0c1r//rJpED61Zm"); //pass: 123
        UsersDAO usersDAO = new UsersDAO();
        SessionDataDAO sessionDataDAO = new SessionDataDAO();

        usersDAO.create(user);
        String savedSessionID = sessionDataDAO.createSessionIdForUser(user);
        String fetchedSessionID = sessionDataDAO.getSessionIdOfLoggedUser(user);

        assertThat(fetchedSessionID).isEqualTo(savedSessionID);
    }

    @Test
    public void getSessionIdOfNotLoggedUser() throws FailedAuthenticationException {
        thrown.expect(FailedAuthenticationException.class);
        thrown.expectMessage("User kuba is not logged in");

        Users user = new Users(1, "kuba", "Kuba", "$2a$10$gBv6DJCxZnifg3yaTLCDv.oAQ7ASoeNkTYymqj0c1r//rJpED61Zm"); //pass: 123
        UsersDAO usersDAO = new UsersDAO();
        SessionDataDAO sessionDataDAO = new SessionDataDAO();

        usersDAO.create(user);

        sessionDataDAO.getSessionIdOfLoggedUser(user);
    }

}