package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import pl.edu.agh.kis.florist.db.tables.pojos.SessionData;
import pl.edu.agh.kis.florist.db.tables.pojos.Users;
import pl.edu.agh.kis.florist.db.tables.records.SessionDataRecord;
import pl.edu.agh.kis.florist.exceptions.FailedAuthenticationException;

import static pl.edu.agh.kis.florist.db.Tables.SESSION_DATA;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by bzdeco on 23.01.17.
 */
public class SessionDataDAO extends DefaultDAO {

    private static final float ACCEPTED_IDLE_TIME = 1f; // in minutes

    public String createSessionIdForUser(Users user) {
        try(DSLContext create = DSL.using(DB_URL)) {
            UUID sessionID = UUID.randomUUID();
            String UUIDString = sessionID.toString();
            SessionData createdSession = new SessionData(
                    UUIDString,
                    user.getId(),
                    new Timestamp(new Date().getTime())
            );

            /*SessionDataRecord storedSession = create.newRecord(SESSION_DATA, createdSession);
            storedSession.store();*/
            create.insertInto(SESSION_DATA, SESSION_DATA.SESSION_ID, SESSION_DATA.USER_ID, SESSION_DATA.LAST_ACCESSED)
                    .values(createdSession.getSessionId(), createdSession.getUserId(), createdSession.getLastAccessed())
                    .execute();

            return UUIDString;
        }
    }

    public String getSessionIdOfLoggedUser(Users user) {
        try(DSLContext create = DSL.using(DB_URL)) {
            SessionDataRecord sessionDataRecord = create
                    .selectFrom(SESSION_DATA)
                    .where(SESSION_DATA.USER_ID.eq(user.getId()))
                    .fetchOne();

            // if user is still logged
            if(sessionDataRecord != null) {
                String sessionID = sessionDataRecord.getSessionId();

                return sessionID;
            }
            else {
                throw new FailedAuthenticationException("User " + user.getUserName() + " is not logged in");
            }
        }
    }

    public Optional<Integer> getUserIDassignedToSessionID(String sessionID) {
        try(DSLContext create = DSL.using(DB_URL)) {
            // Get userID
            Integer userID = null;
            SessionDataRecord sessionDataRecord = create
                    .selectFrom(SESSION_DATA)
                    .where(SESSION_DATA.SESSION_ID.eq(sessionID))
                    .fetchOne();

            if(sessionDataRecord != null) {
                userID = sessionDataRecord.getUserId();

                // Update last accessed field
                create.update(SESSION_DATA)
                        .set(SESSION_DATA.LAST_ACCESSED, new Timestamp(new Date().getTime()))
                        .where(SESSION_DATA.SESSION_ID.eq(sessionID))
                        .execute();
            }

            return Optional.ofNullable(userID);
        }
    }
}
