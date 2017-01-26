package pl.edu.agh.kis.florist.model;

import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by bzdeco on 11.01.17.
 */
public class Folder extends FolderMetadata implements Resource {

    private static final long serialVersionUID = 4727158108618432308L;

    public Folder(FolderMetadata value) {
        super(value);
    }

    public Folder(Integer folderId, String name, String pathLower, String pathDisplay, Integer parentFolderId, Timestamp serverCreatedAt, Integer ownerId) {
        super(folderId, name, pathLower, pathDisplay, parentFolderId, serverCreatedAt, ownerId);
    }

    public static Folder fromPathDisplay(String pathDisplay) {
        String pathLower = pathDisplay.toLowerCase();
        String name = Resource.getNameFromPath(pathDisplay);

        return new Folder(null, name, pathLower, pathDisplay, 0, Resource.getCurrentTime(), 0);
    }

    public static Folder fromPathLower(String pathLower) {
        // This name will be always lowercase, but with correct length
        String lowercaseName = Resource.getNameFromPath(pathLower);
        return new Folder(null, lowercaseName, pathLower, "", 0, Resource.getCurrentTime(), 0);
    }

    public Folder updateFolderId(int id) {
        return new Folder(
                id,
                getName(),
                getPathLower(),
                getPathDisplay(),
                getParentFolderId(),
                getServerCreatedAt(),
                getOwnerId()
        );
    }

    @Override
    public Folder setOwnerID(int ownerId) {
        return new Folder(
                getFolderId(),
                getName(),
                getPathLower(),
                getPathDisplay(),
                getParentFolderId(),
                getServerCreatedAt(),
                ownerId
        );
    }

    @Override
    public Folder updateCreatedTime() {
        return new Folder(
                getFolderId(),
                getName(),
                getPathLower(),
                getPathDisplay(),
                getParentFolderId(),
                Resource.getCurrentTime(),
                getOwnerId()
        );

    }

    @Override
    public Resource updateChangedTime() {
        return this;
    }

    @Override
    public String getPathLowerToParent() {
        return getPathDisplayToParent().toLowerCase();
    }

    public String getPathDisplayToParent() {
        // This path should be already well formatted with forward slashes
        int parentPathEnd = getPathDisplay().length() - (getName().length() + 1);
        String pathToParent = getPathDisplay().substring(0, parentPathEnd);

        return pathToParent;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
