package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;
import pl.edu.agh.kis.florist.exceptions.InvalidPathException;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;
import pl.edu.agh.kis.florist.model.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static pl.edu.agh.kis.florist.db.Tables.FILE_CONTENTS;
import static pl.edu.agh.kis.florist.db.Tables.FILE_METADATA;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

/**
 * Created by bzdeco on 10.01.17.
 */
public class FolderMetadataDAOTest {

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
        create.deleteFrom(FOLDER_METADATA).execute();
        create.deleteFrom(FILE_METADATA).execute();
        create.deleteFrom(FILE_CONTENTS).execute();
        create.close();
    }

    @Test
    public void storeNewFolder() throws Exception {
        Folder folder = new Folder(1, "test", "/test/", "/test/", 0, Resource.getCurrentTime(), 0);
        FolderMetadata retrieved = new FolderMetadataDAO().store(folder);

        assertNotNull(retrieved);
        assertThat(retrieved).extracting(
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getParentFolderId,
                FolderMetadata::getOwnerId).containsOnly("test", "/test/", "/test/", 0, 0);
        assertThat(retrieved.getFolderId()).isGreaterThan(0);
    }

    @Test
    public void storeOneNewFolderAndFetchFromDB() throws Exception {
        storeNewFolder();

        List<FolderMetadata> result = new FolderMetadataDAO().loadAllFolders();

        assertThat(result).hasSize(1);
    }

    @Test
    public void getFolderParentID() throws Exception {
        Folder outside = new Folder(1, "outside", "/outside/", "/outside/", 0, Resource.getCurrentTime(), 1);
        Folder inside = new Folder(2, "inside", "/outside/inside/", "/outside/inside/", 1, Resource.getCurrentTime(), 1);

        new FolderMetadataDAO().store(outside);
        assertThat(new FolderMetadataDAO().resolveParentFolderID(inside)).isEqualTo(outside.getFolderId());

    }

    @Test
    public void storeFolderInsideAnother() throws Exception {
        Folder outside = new Folder(1, "outside", "/outside/", "/outside/", 0, Resource.getCurrentTime(), 1);
        Folder inside = new Folder(2, "inside", "/outside/inside/", "/outside/inside/", 1, Resource.getCurrentTime(), 1);

        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(outside);
        dao.store(inside);

        List<FolderMetadata> result = dao.loadAllFolders();

        assertThat(result).hasSize(2);
        assertThat(inside.getParentFolderId()).isEqualTo(dao.resolveParentFolderID(inside));
    }

    @Test
    public void storeTwoFoldersAndMoveOneInsideAnother() throws Exception {
        Folder parent = new Folder(1, "parent", "/parent/", "/parent/", 0, Resource.getCurrentTime(), 1);
        Folder moved = new Folder(2, "moved", "/moved/", "/moved/", 0, Resource.getCurrentTime(), 1);

        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(parent);
        dao.store(moved);

        Folder newLocation = new Folder(0, "parent", "/parent/", "/parent/", 0, Resource.getCurrentTime(), 1);
        FolderMetadata updated = dao.move(moved, newLocation);

        List<FolderMetadata> result = dao.loadAllFolders();

        assertThat(result).hasSize(2);
        assertThat(updated).extracting(
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getParentFolderId,
                FolderMetadata::getOwnerId).containsOnly("moved", "/parent/moved/", "/parent/moved/", 1, 1);
    }

    @Test
    public void storeTwoFoldersOneInsideAnotherAndMoveTheInnerFolderToRoot() throws Exception {
        Folder parent = new Folder(1, "parent", "/parent/", "/parent/", 0, Resource.getCurrentTime(), 1);
        Folder moved = new Folder(2, "moved", "/parent/moved/", "/parent/moved/", 1, Resource.getCurrentTime(), 1);
        Folder root = new Folder(0, "", "/", "/", 0, Resource.getCurrentTime(), 0);

        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(parent);
        dao.store(moved);
        FolderMetadata updated = dao.move(moved, root);

        assertThat(updated).extracting(
                FolderMetadata::getFolderId,
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getParentFolderId,
                FolderMetadata::getOwnerId).containsOnly(2, "moved", "/moved/", "/moved/", 0, 1);
    }

    @Test (expected = InvalidPathException.class)
    public void cannotChangeFolderNameWithMove() throws Exception {
        Folder old = new Folder(1, "old", "/old/", "/old/", 0, Resource.getCurrentTime(), 1);
        Folder changed = Folder.fromPathDisplay("/Changed/");

        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(old);
        FolderMetadata updated = dao.move(old, changed);
    }

    @Test
    public void moveFolderContainingOtherFolders() throws Exception {
        Folder parent1 = new Folder(1, "parent1", "/parent1/", "/parent1/", 0, Resource.getCurrentTime(), 1);
        Folder parent2 = new Folder(2, "parent2", "/parent2/", "/parent2/", 0, Resource.getCurrentTime(), 1);
        Folder child1 = new Folder(3, "child1", "/parent1/child1/", "/parent1/child1/", 1, Resource.getCurrentTime(), 1);
        Folder child2 = new Folder(4, "child2", "/parent1/child2/", "/parent1/child2/", 1, Resource.getCurrentTime(), 1);
        Folder grandchild1 = new Folder(5, "grandchild1", "/parent1/child1/grandchild1/", "/parent1/child1/grandchild1/", 3, Resource.getCurrentTime(), 1);

        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(parent1);
        dao.store(parent2);
        dao.store(child1);
        dao.store(child2);
        dao.store(grandchild1);

        FolderMetadata updated = dao.move(parent1, parent2);
        assertThat(updated).extracting(
                FolderMetadata::getPathLower,
                FolderMetadata::getParentFolderId).containsOnly("/parent2/parent1/", 2);
        parent1 = new Folder(updated);
        List<Folder> children = new FolderContentsDAO().getFolderContents(parent1, false).getFolders();

        assertThat(children).hasSize(2);
        assertThat(children.get(0)).extracting(
                FolderMetadata::getPathLower,
                FolderMetadata::getParentFolderId).containsOnly("/parent2/parent1/child1/", 1);
        assertThat(children.get(1)).extracting(
                FolderMetadata::getPathLower,
                FolderMetadata::getParentFolderId).containsOnly("/parent2/parent1/child2/", 1);

    }

    @Test
    public void getFolderMetadata() throws Exception {
        Folder folder = new Folder(1, "folder", "/folder/", "/folder/", 0, Resource.getCurrentTime(), 1);

        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(folder);

        FolderMetadata retrieved = dao.getMetadata(folder);

        assertThat(retrieved).isNotNull();
        assertThat(retrieved).extracting(
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getParentFolderId,
                FolderMetadata::getOwnerId).containsOnly("folder", "/folder/", "/folder/", 0, 1);

    }

    @Test
    public void storeAndDeleteFolder() throws Exception {
        Folder folder = new Folder(1, "folder", "/folder/", "/folder/", 0, Resource.getCurrentTime(), 1);

        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(folder);
        dao.delete(folder);

        assertThat(dao.loadAllFolders()).hasSize(0);
    }

    @Test
    public void storeHierarchyOfFoldersAndDeleteTheirRootParent() throws Exception {
        Folder folder1 = new Folder(1, "folder1", "/folder1/", "/folder1/", 0, Resource.getCurrentTime(), 1);
        Folder folder2 = new Folder(2, "folder2", "/folder1/folder2/", "/folder1/folder2/", 1, Resource.getCurrentTime(), 1);
        Folder folder3 = new Folder(3, "folder3", "/folder1/folder2/folder3/", "/folder1/folder2/folder3/", 2, Resource.getCurrentTime(), 1);

        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(folder1);
        dao.store(folder2);
        dao.store(folder3);

        dao.delete(folder1);

        assertThat(dao.loadAllFolders()).hasSize(0);
    }

    @Test
    public void renameFolderWithinHierarchy() throws Exception {
        // Designed hierarchy:
        // grandparent/
        // --- parent1/
        // --- --- child1/
        // --- --- --- file1
        // --- --- file2
        // --- parent2/
        // --- file3
        Folder grandparent = new Folder(1, "gp", "/gp/", "/gp/", 0, Resource.getCurrentTime(), 0);
        Folder parent1 = new Folder(2, "parent1", "/gp/parent1/", "/gp/parent1/", 1, Resource.getCurrentTime(), 0);
        Folder parent2 = new Folder(3, "parent2", "/gp/parent2/", "/gp/parent2/", 1, Resource.getCurrentTime(), 0);
        Folder child1 = new Folder(4, "child1", "/gp/parent1/child1/", "/gp/parent1/child1/", 2, Resource.getCurrentTime(), 0);

        File file1 = new File(1, "file1", "/gp/parent1/child1/file1", "/gp/parent1/child1/file1", 4, 0, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);
        File file2 = new File(2, "file2", "/gp/parent1/file2", "/gp/parent1/file2", 2, 0, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);
        File file3 = new File(3, "file3", "/gp/file3", "/gp/file3", 1, 0, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);

        FolderMetadataDAO folderDAO = new FolderMetadataDAO();
        FileMetadataDAO fileDAO = new FileMetadataDAO();

        folderDAO.store(grandparent);
        folderDAO.store(parent1);
        folderDAO.store(parent2);
        folderDAO.store(child1);
        folderDAO.loadAllFolders();
        fileDAO.upload(file1);
        fileDAO.upload(file2);
        fileDAO.upload(file3);

        Folder renamed = new Folder(1, "renamed", "/renamed/", "/renamed/", 0, Resource.getCurrentTime(), 1);
        folderDAO.rename(grandparent, renamed);

        List<FolderMetadata> renamedFolders = folderDAO.loadAllFolders();
        List<FileMetadata> renamedFiles = fileDAO.loadAllFiles();

        for(FolderMetadata folder : renamedFolders)
            assertThat(folder.getPathLower()).startsWith("/renamed/");
        for(FileMetadata file : renamedFiles)
            assertThat(file.getPathLower()).startsWith("/renamed/");
    }

}