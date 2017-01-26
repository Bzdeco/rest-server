package pl.edu.agh.kis.florist.model;

import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by bzdeco on 16.01.17.
 */
public interface Resource {
    String getPathLower();
    String getPathLowerToParent();
    Resource updateCreatedTime();
    Resource updateChangedTime();
    Resource setOwnerID(int ownerID);

    static String getNameFromPath(String path) {
        String name = "";
        Scanner resourceNameScanner = new Scanner(path);
        resourceNameScanner.useDelimiter("/");

        // Get the last part of the path which is the resource name
        while(resourceNameScanner.hasNext()) {
            name = resourceNameScanner.next();
        }

        return name;
    }

    static Timestamp getCurrentTime() {
        return new Timestamp(new Date().getTime());
    }
}
