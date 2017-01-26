package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.db.tables.records.FileMetadataRecord;
import pl.edu.agh.kis.florist.db.tables.records.FolderMetadataRecord;
import pl.edu.agh.kis.florist.exceptions.InvalidPathException;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;
import pl.edu.agh.kis.florist.model.Resource;

import java.util.List;
import java.util.Optional;

import static pl.edu.agh.kis.florist.db.Tables.FILE_METADATA;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

/**
 * Created by bzdeco on 18.01.17.
 */
public class FileMetadataDAO extends ResourcesDAO {

    public FileMetadata move(File source, Folder dest) {
        try(DSLContext create = DSL.using(DB_URL)) {
            Optional<FileMetadataRecord> movedOpt = fetchRecordFromFile(source, create);

            // If file exists
            if(movedOpt.isPresent()) {
                FileMetadataRecord movedRecord = movedOpt.get();

                Optional<FolderMetadataRecord> targetOpt = fetchRecordFromFolder(dest, create);

                // If target folder is not the same folder in which the file is
                if(!source.getPathLowerToParent().equals(dest.getPathLower())) {

                    // If target folder exists
                    if(targetOpt.isPresent()) {
                        FolderMetadataRecord targetRecord = targetOpt.get();

                        // Check if it won't override existing file
                        File result = new File(
                                movedRecord.getFileId(),
                                movedRecord.getName(),
                                targetRecord.getPathLower() + movedRecord.getName().toLowerCase(),
                                targetRecord.getPathDisplay() + movedRecord.getName(),
                                targetOpt.get().getFolderId(),
                                movedRecord.getSize(),
                                movedRecord.getServerCreatedAt(),
                                Resource.getCurrentTime(),
                                movedRecord.getOwnerId()
                        );

                        Optional<FileMetadataRecord> alreadyExists = fetchRecordFromFile(result, create);

                        if(!alreadyExists.isPresent()) {

                            // Move the file
                            movedRecord
                                    .setPathLower(result.getPathLower())
                                    .setPathDisplay(result.getPathDisplay())
                                    .setEnclosingFolderId(result.getEnclosingFolderId())
                                    .setServerChangedAt(result.getServerChangedAt());

                            // Save changes
                            movedRecord.store();

                            return movedRecord.into(FileMetadata.class);
                        }
                        else
                            throw new InvalidPathException("File " + result.getPathLower() + " already exists");
                    }
                    else
                        throw new InvalidPathException("Target parent folder " + dest.getPathLower() + " does not exist");
                }
                else
                    throw new InvalidPathException("Cannot move file within same enclosing folder");
            }
            else
                throw new InvalidPathException("File " + source.getPathLower() + " does not exist");
        }
    }

    public FileMetadata upload(File uploaded, int sizeInBytes) {
        try(DSLContext create = DSL.using(DB_URL)) {
            String parentFolderPathLower = uploaded.getPathLowerToParent();
            Optional<FolderMetadataRecord> parentFolderOpt = fetchRecordFromFolder(Folder.fromPathLower(parentFolderPathLower), create);

            if(parentFolderOpt.isPresent()) {
                FolderMetadataRecord parentFolderRecord = parentFolderOpt.get();

                // Check if such file doesn't already exist
                Optional<FileMetadataRecord> alreadyExisting = fetchRecordFromFile(uploaded, create);

                if (!alreadyExisting.isPresent()) {

                    // Add record to FILE_METADATA
                    // Corrects possibly not valid path display
                    uploaded = File.fromPathDisplay(parentFolderRecord.getPathDisplay() + uploaded.getName());

                    FileMetadataRecord record = create.newRecord(FILE_METADATA, uploaded);

                    record.setOwnerId(uploaded.getOwnerId());
                    record.setSize(sizeInBytes);
                    record.setEnclosingFolderId(resolveEnclosingFolderId(uploaded));

                    record.store();

                    return record.into(FileMetadata.class);
                }
                else
                    throw new InvalidPathException("File " + uploaded.getPathLower() + " already exists");
            }
            else
                throw new InvalidPathException("Parent folder " + parentFolderPathLower + " does not exist");
        }
    }

    public FileMetadata download(File downloadedFile) {
        String downloadedFilePathLower = downloadedFile.getPathLower();

        try(DSLContext create = DSL.using(DB_URL)) {
            Optional<FileMetadataRecord> downloadedOpt = fetchRecordFromFile(downloadedFile, create);

            if(downloadedOpt.isPresent()) {
                FileMetadataRecord downloadedRecord = downloadedOpt.get();
                FileMetadata downloadedFileMetadata = downloadedRecord.into(FileMetadata.class);

                return downloadedFileMetadata;
            }
            else
                throw new InvalidPathException("File " + downloadedFilePathLower + " does not exist");
        }
    }

    public FileMetadata delete(File deleted) {
        String deletedFilePath = deleted.getPathLower();

        try(DSLContext create = DSL.using(DB_URL)) {
            Optional<FileMetadataRecord> deletedOpt = fetchRecordFromFile(deleted, create);

            if(deletedOpt.isPresent()) {
                FileMetadataRecord deletedRecord = deletedOpt.get();
                FileMetadata deletedFile = deletedRecord.into(FileMetadata.class);

                // Delete the file
                deletedRecord.delete();

                return deletedFile;
            }
            else
                throw new InvalidPathException("File " + deletedFilePath + " does not exist");
        }
    }

    // Needs to get whole changed path in renamed
    public FileMetadata rename(File source, File renamed) {
        String path = source.getPathLower();

        try(DSLContext create = DSL.using(DB_URL)) {
            Optional<FileMetadataRecord> renamedOpt = fetchRecordFromFile(source, create);

            if(renamedOpt.isPresent()) {
                FileMetadataRecord renamedRecord = renamedOpt.get();

                // Check if it won't override existing file
                Optional<FileMetadataRecord> alreadyExisting = fetchRecordFromFile(renamed, create);

                if(!alreadyExisting.isPresent()) {
                    renamedRecord
                            .setName(renamed.getName())
                            .setPathLower(renamed.getPathLower())
                            .setPathDisplay(renamed.getPathDisplay())
                            .setServerChangedAt(Resource.getCurrentTime());

                    renamedRecord.store();

                    return renamedRecord.into(FileMetadata.class);
                }
                else
                    throw new InvalidPathException("File " + renamed.getPathLower() + " already exists");
            }
            else
                throw new InvalidPathException("File " + path + " does not exist");
        }
    }

    public FileMetadata getMetadata(File file) {
        String path = file.getPathLower();

        try(DSLContext create = DSL.using(DB_URL)) {
            Optional<FileMetadataRecord> retrievedOpt = fetchRecordFromFile(file, create);

            // Check if file exists
            if(retrievedOpt.isPresent()) {
                FileMetadataRecord retrievedRecord = retrievedOpt.get();
                return retrievedRecord.into(FileMetadata.class);
            }
            else
                throw new InvalidPathException("File " + path + " does not exist");

        }
    }

    public List<FileMetadata> loadAllFiles() {
        try(DSLContext create = DSL.using(DB_URL)) {
            List<FileMetadata> files = create.select(FILE_METADATA.fields())
                                            .from(FILE_METADATA)
                                            .fetchInto(FileMetadata.class);

            return files;
        }
    }

    public int resolveEnclosingFolderId(File file) {
        try(DSLContext create = DSL.using(DB_URL)) {
            String pathToEnclosingFolder = file.getPathLowerToParent();
            int ownerID = file.getOwnerId();

            // If file is to be stored in root directory
            if(pathToEnclosingFolder.equals("/"))
                return 0;

            Integer parentID = create
                    .select(FOLDER_METADATA.FOLDER_ID)
                    .from(FOLDER_METADATA)
                    .where(FOLDER_METADATA.PATH_LOWER.eq(pathToEnclosingFolder))
                    .and(FOLDER_METADATA.OWNER_ID.eq(ownerID))
                    .fetchOneInto(Integer.class);

            if(parentID != null) {
                return parentID;
            }
            else
                throw new InvalidPathException("Resolving enclosing folder id failed: enclosing folder " + pathToEnclosingFolder + " does not exist");
        }
    }
}
