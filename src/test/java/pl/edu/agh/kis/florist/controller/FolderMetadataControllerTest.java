package pl.edu.agh.kis.florist.controller;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pl.edu.agh.kis.florist.dao.FolderMetadataDAO;
import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;
import pl.edu.agh.kis.florist.exceptions.InvalidPathException;
import pl.edu.agh.kis.florist.model.Folder;
import spark.Request;
import spark.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static pl.edu.agh.kis.florist.db.Tables.FILE_CONTENTS;
import static pl.edu.agh.kis.florist.db.Tables.FILE_METADATA;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

/**
 * Created by bzdeco on 25.01.17.
 */
public class FolderMetadataControllerTest {

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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void handleCreateNewFolder() throws Exception {
        Request request = mock(Request.class);
        Response response = mock(Response.class, withSettings().stubOnly());
        FolderMetadataController controller = new FolderMetadataController(new FolderMetadataDAO());

        when(request.params("path")).thenReturn("/newFolder/");
        when(request.attribute("ownerID")).thenReturn(1);
        FolderMetadata folder = (FolderMetadata)controller.handleCreateNewFolder(request, response);

        assertThat(folder).isNotNull();
        assertThat(folder).extracting(
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getOwnerId).containsOnly("newFolder", "/newfolder/", "/newFolder/", 1);
    }

    @Test
    public void handleCreateNewFolderInFolderThatDoesNotExist_FAIL() throws InvalidPathException {
        Request request = mock(Request.class);
        Response response = mock(Response.class, withSettings().stubOnly());
        FolderMetadataController controller = new FolderMetadataController(new FolderMetadataDAO());

        when(request.params("path")).thenReturn("/parent/inside/");
        when(request.attribute("ownerID")).thenReturn(0);
        thrown.expect(InvalidPathException.class);
        thrown.expectMessage("Parent folder /parent/ does not exist");

        FolderMetadata folder = (FolderMetadata)controller.handleCreateNewFolder(request, response);
    }

    @Test
    public void handleMoveFolderBetweenTwoExistingFolders() {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        FolderMetadataDAO dao = new FolderMetadataDAO();
        Folder folder1 = Folder.fromPathDisplay("/Folder1/").setOwnerID(1);
        Folder folder2 = Folder.fromPathDisplay("/Folder2/").setOwnerID(1);
        Folder moved = Folder.fromPathDisplay("/folder1/Moved/").setOwnerID(1);
        dao.store(folder1);
        dao.store(folder2);
        dao.store(moved);
        FolderMetadataController controller = new FolderMetadataController(dao);

        when(request.params("path")).thenReturn("/folder1/moved/");
        when(request.queryParams("new_path")).thenReturn("/folder2/");
        when(request.attribute("ownerID")).thenReturn(1);

        FolderMetadata result = (FolderMetadata)controller.handleMove(request, response);

        assertThat(result).extracting(
                FolderMetadata::getName,
                FolderMetadata::getPathLower,
                FolderMetadata::getPathDisplay,
                FolderMetadata::getOwnerId).containsOnly("Moved", "/folder2/moved/", "/Folder2/Moved/", 1);

    }

    @Test
    public void handleRenameFolderWhichAffectsItsChildren() {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        // TODO
    }

}