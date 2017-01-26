package pl.edu.agh.kis.florist.model;

import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import static pl.edu.agh.kis.florist.db.Tables.FILE_METADATA;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bzdeco on 16.01.17.
 */
public class File extends FileMetadata implements Resource {
    private static final long serialVersionUID = -6667884423986501734L;

    public File(FileMetadata value) {
        super(value);
    }

    public File(Integer fileId, String name, String pathLower, String pathDisplay, Integer enclosingFolderId, Integer size, Timestamp serverCreatedAt, Timestamp serverChangedAt, Integer ownerId) {
        super(fileId, name, pathLower, pathDisplay, enclosingFolderId, size, serverCreatedAt, serverChangedAt, ownerId);
    }

    public static File fromPathDisplay(String pathDisplay) {
        String pathLower = pathDisplay.toLowerCase();
        String name = Resource.getNameFromPath(pathDisplay);

        return new File(null, name, pathLower, pathDisplay, 0, 0, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);
    }

    public static File fromPathLower(String pathLower) {
        // This name will be always lowercase, but with correct length
        String lowercaseName = Resource.getNameFromPath(pathLower);
        return new File(null, lowercaseName, pathLower, "", 0, 0, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);
    }

    @Override
    public String getPathLowerToParent() {
        return getPathDisplayToParent().toLowerCase();
    }

    public String getPathDisplayToParent() {
        int parentPathEnd = getPathDisplay().length() - getName().length();
        String pathToParent = getPathDisplay().substring(0, parentPathEnd);

        return pathToParent;
    }

    @Override
    public Resource updateCreatedTime() {
        Timestamp serverCreatedAt = Resource.getCurrentTime();

        return new File(
                getFileId(),
                getName(),
                getPathLower(),
                getPathDisplay(),
                getEnclosingFolderId(),
                getSize(),
                serverCreatedAt,
                getServerChangedAt(),
                getOwnerId()
        );
    }

    @Override
    public Resource updateChangedTime() {
        Timestamp serverChangedAt = Resource.getCurrentTime();

        return new File(
                getFileId(),
                getName(),
                getPathLower(),
                getPathDisplay(),
                getEnclosingFolderId(),
                getSize(),
                getServerCreatedAt(),
                serverChangedAt,
                getOwnerId()
        );
    }

    @Override
    public Resource setOwnerID(int ownerID) {
        return new File(
                getFileId(),
                getName(),
                getPathLower(),
                getPathDisplay(),
                getEnclosingFolderId(),
                getSize(),
                getServerCreatedAt(),
                getServerChangedAt(),
                ownerID
        );
    }
}
