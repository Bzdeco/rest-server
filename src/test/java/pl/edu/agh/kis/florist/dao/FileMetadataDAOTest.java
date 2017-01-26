package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;
import pl.edu.agh.kis.florist.model.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.edu.agh.kis.florist.db.Tables.FILE_CONTENTS;
import static pl.edu.agh.kis.florist.db.Tables.FILE_METADATA;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

/**
 * Created by bzdeco on 18.01.17.
 */
public class FileMetadataDAOTest {

    private final String DB_URL = "jdbc:sqlite:test.db";
    private DSLContext create;

    @Before
    public void setup() {
        create = DSL.using(DB_URL);
        create.deleteFrom(FOLDER_METADATA).execute();
        create.deleteFrom(FILE_METADATA).execute();
        create.deleteFrom(FILE_CONTENTS).execute();
    }

    @After
    public void tearDown() {
        create.deleteFrom(FILE_CONTENTS).execute();
        create.deleteFrom(FILE_METADATA).execute();
        create.deleteFrom(FOLDER_METADATA).execute();
        create.close();
    }

    @Test
    public void resolveEnclosingFolderId() throws Exception {
        Folder folder = new Folder(1, "folder", "/folder/", "/folder/", 0, Resource.getCurrentTime(), 0);
        Folder folder2 = new Folder(2, "folder2", "/folder2/", "/folder2/", 0, Resource.getCurrentTime(), 0);

        FolderMetadataDAO daoFolder = new FolderMetadataDAO();
        daoFolder.store(folder);
        daoFolder.store(folder2);

        File file = new File(1, "File", "/folder2/file", "/folder2/File", 2, 1024, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);
        assertThat(new FileMetadataDAO().resolveEnclosingFolderId(file)).isEqualTo(2);
    }

    @Test
    public void getFileMetadata() throws Exception {
        File file = new File(1, "File", "/file", "/File", 0, 1024, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);

        FileMetadataDAO dao = new FileMetadataDAO();
        FileMetadata uploaded = dao.upload(file);

        assertThat(dao.getMetadata(file)).extracting(
                FileMetadata::getFileId,
                FileMetadata::getName,
                FileMetadata::getPathLower,
                FileMetadata::getPathDisplay,
                FileMetadata::getEnclosingFolderId,
                FileMetadata::getSize,
                FileMetadata::getOwnerId).containsOnly(uploaded.getFileId(), "File", "/file", "/File", 0, 1024, 0);
    }

    @Test
    public void storeAndDeleteFile() {
        File file = new File(1, "File", "/file", "/File", 0, 1024, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);

        FileMetadataDAO dao = new FileMetadataDAO();
        dao.upload(file);
        dao.delete(file);

        assertThat(dao.loadAllFiles()).hasSize(0);
    }

    @Test
    public void moveFileFromOneFolderToRoot() {
        Folder root = new Folder(0, "", "/", "/", 0, Resource.getCurrentTime(), 0);
        Folder folder = new Folder(1, "folder", "/folder/", "/folder/", 0, Resource.getCurrentTime(), 0);
        File file = new File(1, "File", "/folder/file", "/folder/File", 0, 1024, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);

        FolderMetadataDAO folderDAO = new FolderMetadataDAO();
        FileMetadataDAO fileDAO = new FileMetadataDAO();

        folderDAO.store(folder);
        FileMetadata uploaded = fileDAO.upload(file);

        FileMetadata updated = fileDAO.move(file, root);

        assertThat(updated).extracting(
                FileMetadata::getFileId,
                FileMetadata::getName,
                FileMetadata::getPathLower,
                FileMetadata::getPathDisplay,
                FileMetadata::getEnclosingFolderId,
                FileMetadata::getSize,
                FileMetadata::getOwnerId).containsOnly(uploaded.getFileId(), "File", "/file", "/File", 0, 1024, 0);

        assertThat(updated).extracting(
                FileMetadata::getServerChangedAt).isNotEqualTo("2017-01-01T00:00:00Z");
    }

    @Test
    public void moveFileFromOneFolderToAnother() {
        Folder parent = new Folder(1, "folder", "/folder/", "/folder/", 0, Resource.getCurrentTime(), 0);
        Folder child = new Folder(2, "child", "/folder/child/", "/folder/child/", 1, Resource.getCurrentTime(), 0);
        File file = new File(1, "File", "/folder/file", "/folder/File", 0, 1024, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);

        FolderMetadataDAO folderDAO = new FolderMetadataDAO();
        FileMetadataDAO fileDAO = new FileMetadataDAO();

        folderDAO.store(parent);
        folderDAO.store(child);
        FileMetadata uploaded = fileDAO.upload(file);

        FileMetadata updated = fileDAO.move(file, child);

        assertThat(updated).extracting(
                FileMetadata::getFileId,
                FileMetadata::getName,
                FileMetadata::getPathLower,
                FileMetadata::getPathDisplay,
                FileMetadata::getEnclosingFolderId,
                FileMetadata::getSize,
                FileMetadata::getOwnerId).containsOnly(uploaded.getFileId(), "File", "/folder/child/file", "/folder/child/File", 2, 1024, 0);
    }

    @Test
    public void renameFile() {
        Folder grandparent = new Folder(1, "gp", "/gp/", "/gp/", 0, Resource.getCurrentTime(), 0);
        Folder parent = new Folder(2, "p", "/gp/p/", "/gp/p/", 1, Resource.getCurrentTime(), 0);
        File file = new File(1, "file", "/gp/p/file", "/gp/p/file", 2, 1024, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);
        File renamed = new File(1, "renamed", "/gp/p/renamed", "/gp/p/renamed", 2, 1024, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);

        FolderMetadataDAO folderDAO = new FolderMetadataDAO();
        FileMetadataDAO fileDAO = new FileMetadataDAO();

        folderDAO.store(grandparent);
        folderDAO.store(parent);
        FileMetadata uploaded = fileDAO.upload(file);
        FileMetadata updated = fileDAO.rename(file, renamed);

        assertThat(updated).extracting(
                FileMetadata::getFileId,
                FileMetadata::getName,
                FileMetadata::getPathLower,
                FileMetadata::getPathDisplay,
                FileMetadata::getEnclosingFolderId,
                FileMetadata::getSize,
                FileMetadata::getOwnerId).containsOnly(uploaded.getFileId(), "renamed", "/gp/p/renamed", "/gp/p/renamed", uploaded.getEnclosingFolderId(), uploaded.getSize(), uploaded.getOwnerId());

    }

    @Test
    public void uploadFile() {
        File file = new File(1, "file", "/file", "/file", 0, 0, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);

        FileMetadataDAO fileDAO = new FileMetadataDAO();
        FileMetadata uploaded = fileDAO.upload(file);

        assertThat(uploaded).extracting(
                FileMetadata::getName,
                FileMetadata::getPathLower,
                FileMetadata::getPathDisplay,
                FileMetadata::getEnclosingFolderId,
                FileMetadata::getSize,
                FileMetadata::getOwnerId).containsOnly("file", "/file", "/file", 0, 0, 0);
    }
}