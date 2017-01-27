package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import pl.edu.agh.kis.florist.db.tables.records.SessionDataRecord;

import static pl.edu.agh.kis.florist.db.Tables.SESSION_DATA;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by bzdeco on 26.01.17.
 */
public class SessionManager extends DefaultDAO implements Runnable {

    private final int ACCEPTED_IDLE_TIME; // in minutes
    private final int FREQUENCY; // in seconds

    public SessionManager() {
        super();
        ACCEPTED_IDLE_TIME = 1;
        FREQUENCY = 5;
    }

    public SessionManager(int acceptedIdleTime, int frequency) {
        super();
        ACCEPTED_IDLE_TIME = acceptedIdleTime;
        FREQUENCY = frequency;
    }

    public SessionManager(String dbUrl) {
        super(dbUrl);
        ACCEPTED_IDLE_TIME = 1;
        FREQUENCY = 5;
    }

    public SessionManager(String dbUrl, int acceptedIdleTime, int frequency) {
        super(dbUrl);
        ACCEPTED_IDLE_TIME = acceptedIdleTime;
        FREQUENCY = frequency;
    }

    @Override
    public void run() {
        while(true) {
            try (DSLContext create = DSL.using(DB_URL)) {
                List<SessionDataRecord> sessionRecords = create.selectFrom(SESSION_DATA).fetchInto(SessionDataRecord.class);

                Timestamp now = new Timestamp(new Date().getTime());
                for (SessionDataRecord record : sessionRecords) {
                    Timestamp lastAccessed = record.getLastAccessed();

                    long differenceInMillis = now.getTime() - lastAccessed.getTime();
                    if (differenceInMillis > 1000 * 60 * ACCEPTED_IDLE_TIME) {
                        record.delete();
                    }
                }
            }

            // check every 5 seconds
            try {
                Thread.sleep(FREQUENCY * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
