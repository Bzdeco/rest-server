package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import pl.edu.agh.kis.florist.db.tables.records.FileMetadataRecord;
import pl.edu.agh.kis.florist.db.tables.records.FolderMetadataRecord;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

import static pl.edu.agh.kis.florist.db.Tables.FILE_METADATA;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

/**
 * Created by bzdeco on 18.01.17.
 */
public class ResourcesDAO extends DefaultDAO {

    public Optional<FolderMetadataRecord> fetchRecordFromFolder(Folder folder, DSLContext create) {
        String pathLower = folder.getPathLower();
        int ownerID = folder.getOwnerId();

        // Root folder
        if(pathLower.equals("/")) {
            Optional<FolderMetadataRecord> root = Optional.of(new FolderMetadataRecord(0, "", "/", "/", 0, new Timestamp(new Date().getTime()), ownerID));
            return root;
        }

        FolderMetadataRecord record = create
                .selectFrom(FOLDER_METADATA)
                .where(FOLDER_METADATA.PATH_LOWER.eq(pathLower))
                .and(FOLDER_METADATA.OWNER_ID.eq(ownerID))
                .fetchOne();

        Optional<FolderMetadataRecord> result = Optional.ofNullable(record);
        return result;
    }

    public Optional<FileMetadataRecord> fetchRecordFromFile(File file, DSLContext create) {
        String pathLower = file.getPathLower();
        int ownerID = file.getOwnerId();

        FileMetadataRecord record = create
                .selectFrom(FILE_METADATA)
                .where(FILE_METADATA.PATH_LOWER.eq(pathLower))
                .and(FILE_METADATA.OWNER_ID.eq(ownerID))
                .fetchOne();

        Optional<FileMetadataRecord> result = Optional.ofNullable(record);
        return result;
    }
}
