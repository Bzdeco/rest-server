package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;
import pl.edu.agh.kis.florist.model.Folder;
import pl.edu.agh.kis.florist.model.FolderContents;
import pl.edu.agh.kis.florist.model.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

/**
 * Created by bzdeco on 16.01.17.
 */
public class FolderContentsDAOTest {

    private final String DB_URL = "jdbc:sqlite:test.db";
    private DSLContext create;

    @Before
    public void setup() {
        create = DSL.using(DB_URL);
        create.deleteFrom(FOLDER_METADATA).execute();
    }

    @After
    public void tearDown() {
        create.deleteFrom(FOLDER_METADATA).execute();
        create.close();
    }

    @Test
    public void getFolderContentsNonRecursive() throws Exception {
        Folder parent = new Folder(1, "parent", "/parent/", "/parent/", 0, Resource.getCurrentTime(), 0);
        Folder child1 = new Folder(2, "child1", "/parent/child1/", "/parent/child1/", 1, Resource.getCurrentTime(), 0);
        Folder child2 = new Folder(3, "child2", "/parent/child2/", "/parent/child2/", 1, Resource.getCurrentTime(), 0);
        Folder child21 = new Folder(4, "child2.1", "/parent/child2/child2.1/", "/parent/child2/child2.1/", 3, Resource.getCurrentTime(), 0);

        // Store all folders
        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(parent);
        dao.store(child1);
        dao.store(child2);
        dao.store(child21);

        FolderContents parentFolderContents = new FolderContentsDAO().getFolderContents(parent, false);

        assertThat(parentFolderContents.getFolders()).hasSize(2);
        assertThat(parentFolderContents.getFolders().get(0)).extracting(
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getParentFolderId).containsOnly("child1", "/parent/child1/", "/parent/child1/", 1);
    }

    @Test
    public void getFolderContentsRecursive() throws Exception {
        Folder parent = new Folder(1, "parent", "/parent/", "/parent/", 0, Resource.getCurrentTime(), 0);
        Folder child1 = new Folder(2, "child1", "/parent/child1/", "/parent/child1/", 1, Resource.getCurrentTime(), 0);
        Folder child2 = new Folder(3, "child2", "/parent/child2/", "/parent/child2/", 1, Resource.getCurrentTime(), 0);
        Folder child21 = new Folder(4, "child2.1", "/parent/child2/child2.1/", "/parent/child2/child2.1/", 3, Resource.getCurrentTime(), 0);

        // Store all folders
        FolderMetadataDAO dao = new FolderMetadataDAO();
        dao.store(parent);
        dao.store(child1);
        dao.store(child2);
        dao.store(child21);

        FolderContents parentFolderContents = new FolderContentsDAO().getFolderContents(parent, true);

        assertThat(parentFolderContents.getFolders()).hasSize(3);
        assertThat(parentFolderContents.getFolders().get(0)).extracting(
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getParentFolderId).containsOnly("child1", "/parent/child1/", "/parent/child1/", 1);
        assertThat(parentFolderContents.getFolders().get(1)).extracting(
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getParentFolderId).containsOnly("child2", "/parent/child2/", "/parent/child2/", 1);
        assertThat(parentFolderContents.getFolders().get(2)).extracting(
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getParentFolderId).containsOnly("child2.1", "/parent/child2/child2.1/", "/parent/child2/child2.1/", 3);
    }

}