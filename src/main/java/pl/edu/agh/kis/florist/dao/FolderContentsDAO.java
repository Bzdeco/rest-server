package pl.edu.agh.kis.florist.dao;

import static pl.edu.agh.kis.florist.db.Tables.FILE_METADATA;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;
import pl.edu.agh.kis.florist.model.FolderContents;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bzdeco on 16.01.17.
 */
public class FolderContentsDAO {

    private final String DB_URL = "jdbc:sqlite:test.db";

    // must get folder with ID AND OWNER ID
    public FolderContents getFolderContents(Folder folder, boolean recursive) {
        // fills only folders list
        try(DSLContext create = DSL.using(DB_URL)) {
            List<Folder> resultFolders;

            // First level
            List<Folder> containedFolders = create
                    .select(FOLDER_METADATA.fields())
                    .from(FOLDER_METADATA)
                    .where(FOLDER_METADATA.PARENT_FOLDER_ID.eq(folder.getFolderId()))
                    .and(FOLDER_METADATA.OWNER_ID.eq(folder.getOwnerId()))
                    .fetchInto(Folder.class);
            List<File> containedFiles = create
                    .select(FILE_METADATA.fields())
                    .from(FILE_METADATA)
                    .where(FILE_METADATA.ENCLOSING_FOLDER_ID.eq(folder.getFolderId()))
                    .and(FILE_METADATA.OWNER_ID.eq(folder.getOwnerId()))
                    .fetchInto(File.class);

            resultFolders = new ArrayList<>(containedFolders);

            // Deeper levels
            if(recursive) {
                for(Folder lowerLevelFolder : containedFolders) {
                    FolderContents lowerLevelContents = getFolderContents(lowerLevelFolder, true);
                    resultFolders.addAll(lowerLevelContents.getFolders());
                    containedFiles.addAll(lowerLevelContents.getFiles());
                }
            }


            return new FolderContents(resultFolders, containedFiles);
        }
    }
}
