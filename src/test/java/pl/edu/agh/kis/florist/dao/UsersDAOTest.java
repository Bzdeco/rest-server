package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.kis.florist.controller.UserAuthenticationUtilities;
import pl.edu.agh.kis.florist.db.tables.pojos.Users;
import pl.edu.agh.kis.florist.exceptions.InvalidUserException;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.edu.agh.kis.florist.db.Tables.*;

/**
 * Created by bzdeco on 23.01.17.
 */
public class UsersDAOTest {

    private final String DB_URL = "jdbc:sqlite:test.db";
    private DSLContext create;

    @Before
    public void setup() {
        create = DSL.using(DB_URL);
        create.deleteFrom(USERS).execute();
    }

    @After
    public void tearDown() {
        create.deleteFrom(USERS).execute();
        create.close();
    }

    @Test
    public void createNewUser() throws Exception {
        String hashedPassword = UserAuthenticationUtilities.hashPassword("password");
        Users newUser = new Users(1, "kuba", "Kuba", hashedPassword);

        UsersDAO dao = new UsersDAO();
        Users stored = dao.create(newUser);

        assertThat(stored).extracting(
                Users::getId,
                Users::getUserName,
                Users::getDisplayName,
                Users::getHashedPassword).containsOnly(1, "kuba", "Kuba", hashedPassword);
    }

    @Test (expected = InvalidUserException.class)
    public void createNewUserWithSameNameAsExisting() {
        String hashedPassword = UserAuthenticationUtilities.hashPassword("password");
        Users newUser = new Users(1, "kuba", "Kuba", hashedPassword);
        Users sameNameUser = new Users(1, "kuba", "KUBA", hashedPassword);

        UsersDAO dao = new UsersDAO();
        dao.create(newUser);
        dao.create(sameNameUser);
    }

}