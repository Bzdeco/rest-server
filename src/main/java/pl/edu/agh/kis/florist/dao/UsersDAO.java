package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import pl.edu.agh.kis.florist.controller.UserAuthenticationUtilities;
import pl.edu.agh.kis.florist.db.tables.pojos.Users;
import pl.edu.agh.kis.florist.db.tables.records.UsersRecord;
import pl.edu.agh.kis.florist.exceptions.FailedAuthenticationException;
import pl.edu.agh.kis.florist.exceptions.InvalidUserException;

import java.util.Optional;

import static pl.edu.agh.kis.florist.db.Tables.USERS;

/**
 * Created by bzdeco on 23.01.17.
 */
public class UsersDAO extends DefaultDAO {

    public Users create(Users user) {
        try(DSLContext create = DSL.using(DB_URL)) {

            // Check if user doesn't already exist
            Optional<UsersRecord> alreadyExisting = fetchRecordFromUserName(user.getUserName(), create);

            if(!alreadyExisting.isPresent()) {

                UsersRecord created = create.newRecord(USERS, user);
                created.store();

                return created.into(Users.class);
            }
            else
                throw new InvalidUserException("User " + user.getUserName() + " already exists");
        }
    }

    public Users authenticate(String login, String password) {
        String loginLowerCase = login.toLowerCase();

        try(DSLContext create = DSL.using(DB_URL)) {
            Optional<UsersRecord> userOpt = fetchRecordFromUserName(loginLowerCase, create);

            // if such user exists
            if(userOpt.isPresent()) {
                UsersRecord userRecord = userOpt.get();

                String storedHashedPassword = userRecord.getHashedPassword();
                if (UserAuthenticationUtilities.checkPassword(password, storedHashedPassword))
                    return userRecord.into(Users.class);
                else
                    throw new FailedAuthenticationException("Wrong password for user " + login);
            }
            else
                throw new FailedAuthenticationException("User " + login + " doesn't exist");
        }
    }

    private Optional<UsersRecord> fetchRecordFromUserName(String userName, DSLContext create) {
        UsersRecord record = create
                .selectFrom(USERS)
                .where(USERS.USER_NAME.eq(userName))
                .fetchOne();

        Optional<UsersRecord> result = Optional.ofNullable(record);
        return result;
    }
}
