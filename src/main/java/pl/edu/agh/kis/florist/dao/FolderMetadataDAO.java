package pl.edu.agh.kis.florist.dao;

import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;
import pl.edu.agh.kis.florist.db.tables.records.FolderMetadataRecord;
import pl.edu.agh.kis.florist.exceptions.InvalidPathException;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;
import pl.edu.agh.kis.florist.model.FolderContents;
import pl.edu.agh.kis.florist.model.Resource;

import java.util.List;
import java.util.Optional;

/**
 * Created by bzdeco on 09.01.17.
 */
public class FolderMetadataDAO extends ResourcesDAO {

    private final FolderContentsDAO folderContentsDAO = new FolderContentsDAO();
    private final FileMetadataDAO fileMetadataDAO = new FileMetadataDAO();

    public FolderMetadata store(Folder folder) {
        try(DSLContext create = DSL.using(DB_URL)) {
            // Check if such folder does not already exist
            Optional<FolderMetadataRecord> alreadyExisting = fetchRecordFromFolder(folder, create);

            if(!alreadyExisting.isPresent()) {
                FolderMetadataRecord record = create.newRecord(FOLDER_METADATA, folder);

                record.setOwnerId(folder.getOwnerId());
                record.setParentFolderId(resolveParentFolderID(folder));

                record.store();

                return record.into(FolderMetadata.class);
            }
            else
                throw new InvalidPathException("Folder " + folder.getPathLower() + " already exists");
        }
    }

    public FolderMetadata move(Folder source, Folder dest) {
        String currentPath = source.getPathLower();
        String newParentPath = dest.getPathLower();

        try(DSLContext create = DSL.using(DB_URL)) {
            Optional<FolderMetadataRecord> movedOpt = fetchRecordFromFolder(source, create);

            // Check if folder exists
            if(movedOpt.isPresent()) {
                FolderMetadataRecord movedRecord = movedOpt.get();
                // Check if target path exists
                System.out.println(" ----------------- FOUND MOVED");
                System.out.println(" -------------------" + newParentPath  + " " + source.getPathLowerToParent());
                Optional<FolderMetadataRecord> targetOpt = fetchRecordFromFolder(dest, create);

                // If we don't move folder to same parent folder and parent folder is root or exists and is not the same folder we move
                if(!newParentPath.equals(source.getPathLowerToParent())) {
                    System.out.println("-------------- CONDITION MET");
                    if (targetOpt.isPresent()) {
                        System.out.println("------------ TARGET IS PRESENT");
                        FolderMetadataRecord targetRecord = targetOpt.get();

                        System.out.println(movedRecord + "\n" + targetRecord);

                        // Check if it won't override existing folder
                        Folder result = new Folder(
                                movedRecord.getFolderId(),
                                movedRecord.getName(),
                                targetRecord.getPathLower() + movedRecord.getName().toLowerCase() + "/",
                                targetRecord.getPathDisplay() + movedRecord.getName() + "/",
                                targetRecord.getFolderId(),
                                movedRecord.getServerCreatedAt(),
                                movedRecord.getOwnerId()
                        );

                        Optional<FolderMetadataRecord> alreadyExisting = fetchRecordFromFolder(result, create);

                        if(!alreadyExisting.isPresent()) {

                            // Move folder
                            movedRecord
                                    .setPathLower(result.getPathLower())
                                    .setPathDisplay(result.getPathDisplay())
                                    .setParentFolderId(result.getParentFolderId());

                            // Save changes
                            movedRecord.store();

                            source = source
                                    .updateFolderId(movedRecord.getFolderId())
                                    .setOwnerID(movedRecord.getOwnerId());

                            FolderContents folderContents = folderContentsDAO.getFolderContents(source, false);

                            // Move folder folder contents
                            for (Folder childFolder : folderContents.getFolders()) {
                                String destPathDisplay = targetRecord.getPathDisplay() + movedRecord.getName() + "/";
                                Folder childDest = new Folder(
                                        null,
                                        movedRecord.getName(),
                                        destPathDisplay.toLowerCase(),
                                        destPathDisplay,
                                        movedRecord.getParentFolderId(),
                                        movedRecord.getServerCreatedAt(),
                                        movedRecord.getOwnerId()
                                );
                                move(childFolder, childDest);
                            }

                            // Move folder file contents
                            for (File childFile : folderContents.getFiles()) {
                                // FIXME possibly might not work
                                Folder childDest = (Folder)Folder
                                        .fromPathDisplay(targetRecord.getPathDisplay() + movedRecord.getName() + "/")
                                        .setOwnerID(source.getOwnerId());
                                fileMetadataDAO.move(childFile, childDest);
                            }

                            return movedRecord.into(FolderMetadata.class);
                        }
                        else
                            throw new InvalidPathException("Folder " + result.getPathLower() + " already exists");
                    }
                    else
                        throw new InvalidPathException("Target parent folder " + newParentPath + " does not exist");
                }
                else
                    throw new InvalidPathException("Cannot move folder to the same location");
            }
            else
                throw new InvalidPathException("Folder " + currentPath + " does not exist and cannot be moved");
        }
    }

    // requires prepared folder renamed with correct path display
    public FolderMetadata rename(Folder source, Folder renamed) {
        String path = source.getPathLower();

        try(DSLContext create = DSL.using(DB_URL)) {
            Optional<FolderMetadataRecord> renamedOpt = fetchRecordFromFolder(source, create);

            if(renamedOpt.isPresent()) {
                FolderMetadataRecord renamedRecord = renamedOpt.get();

                // Check if it won't override existing folder
                Optional<FolderMetadataRecord> alreadyExisting = fetchRecordFromFolder(renamed, create);

                if(!alreadyExisting.isPresent()) {

                    // Get folder contents before it's renamed
                    source = source
                            .updateFolderId(renamedRecord.getFolderId())
                            .setOwnerID(renamedRecord.getOwnerId());

                    FolderContents folderContents = folderContentsDAO.getFolderContents(source, false);

                    renamedRecord
                            .setName(renamed.getName())
                            .setPathLower(renamed.getPathLower())
                            .setPathDisplay(renamed.getPathDisplay());

                    // Save changes
                    renamedRecord.store();

                    // Rename folder file contents
                    for (File childFile : folderContents.getFiles()) {
                        File fileWithRenamedParent = new File(
                                childFile.getFileId(),
                                childFile.getName(),
                                renamed.getPathLower() + childFile.getName(),
                                renamed.getPathDisplay() + childFile.getName(),
                                childFile.getEnclosingFolderId(), childFile.getSize(),
                                childFile.getServerCreatedAt(), Resource.getCurrentTime(),
                                childFile.getOwnerId()
                        );
                        fileMetadataDAO.rename(childFile, fileWithRenamedParent);
                    }

                    // Rename folder folder contents
                    for (Folder childFolder : folderContents.getFolders()) {
                        Folder folderWithRenamedParent = new Folder(
                                childFolder.getFolderId(),
                                childFolder.getName(),
                                renamed.getPathLower() + childFolder.getName().toLowerCase() + "/",
                                renamed.getPathDisplay() + childFolder.getName() + "/",
                                childFolder.getParentFolderId(),
                                childFolder.getServerCreatedAt(),
                                childFolder.getOwnerId()
                        );
                        // Rename recursively further
                        rename(childFolder, folderWithRenamedParent);
                    }


                    return renamedRecord.into(FolderMetadata.class);
                }
                else
                    throw new InvalidPathException("Folder " + renamed.getPathLower() + " already exists");
            }
            else
                throw new InvalidPathException("Folder " + path + " does not exist");
        }
    }

    public FolderMetadata delete(Folder folder) {
        String deletedFolderPath = folder.getPathLower();

        try(DSLContext create = DSL.using(DB_URL)) {
            // Check if folder exists
            Optional<FolderMetadataRecord> deletedOpt = fetchRecordFromFolder(folder, create);

            if(deletedOpt.isPresent()) {
                FolderMetadataRecord deletedRecord = deletedOpt.get();
                FolderMetadata deletedFolder = deletedRecord.into(FolderMetadata.class);

                folder = folder
                        .updateFolderId(deletedFolder.getFolderId())
                        .setOwnerID(deletedFolder.getOwnerId());

                FolderContents folderContents = folderContentsDAO.getFolderContents(folder, false);

                // Delete contained folders
                for(Folder childFolder : folderContents.getFolders())
                    delete(childFolder);

                // Delete contained files
                for(File childFile : folderContents.getFiles()) {
                    fileMetadataDAO.delete(childFile);
                }

                // Delete the folder
                deletedRecord.delete();
                return deletedFolder;
            }
            else
                throw new InvalidPathException("Folder " + deletedFolderPath + " does not exist");
        }
    }

    public FolderMetadata getMetadata(Folder folder) {
        String path = folder.getPathLower();

        try(DSLContext create = DSL.using(DB_URL)) {
            Optional<FolderMetadataRecord> retrievedOpt = fetchRecordFromFolder(folder, create);

            // Check if folder exists
            if(retrievedOpt.isPresent()) {
                FolderMetadataRecord retrievedRecord = retrievedOpt.get();
                return retrievedRecord.into(FolderMetadata.class);
            }
            else
                throw new InvalidPathException("Folder " + path + " does not exist");

        }
    }

    public List<FolderMetadata> loadAllFolders() {
        try(DSLContext create = DSL.using(DB_URL)) {
            List<FolderMetadata> folders = create
                    .select(FOLDER_METADATA.fields())
                    .from(FOLDER_METADATA)
                    .fetchInto(FolderMetadata.class);

            return folders;
        }
    }

    public int resolveParentFolderID(Folder folderMetadata) {
        try(DSLContext create = DSL.using(DB_URL)) {
            String pathToParent = folderMetadata.getPathLowerToParent();
            int ownerID = folderMetadata.getOwnerId();

            // If folder is the child of root directory
            if(pathToParent.equals("/"))
                return 0;

            Integer parentID = create
                    .select(FOLDER_METADATA.FOLDER_ID)
                    .from(FOLDER_METADATA)
                    .where(FOLDER_METADATA.PATH_LOWER.eq(pathToParent))
                    .and(FOLDER_METADATA.OWNER_ID.eq(ownerID))
                    .fetchOneInto(Integer.class);

            // throw InvalidPathException if parent folder does not exist
            if(parentID != null)
                return parentID;
            else
                throw new InvalidPathException("Parent folder " + pathToParent + " does not exist");
        }
    }
}
