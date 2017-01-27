package pl.edu.agh.kis.florist.controller;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.kis.florist.dao.FileMetadataDAO;
import pl.edu.agh.kis.florist.dao.FolderMetadataDAO;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;
import pl.edu.agh.kis.florist.model.FolderContents;
import spark.Request;
import spark.Response;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static pl.edu.agh.kis.florist.db.Tables.FILE_CONTENTS;
import static pl.edu.agh.kis.florist.db.Tables.FILE_METADATA;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

/**
 * Created by bzdeco on 27.01.17.
 */
public class FolderContentsControllerTest {

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
    public void handleListFolderContentsNonRecursively() throws Exception {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        FolderMetadataDAO folderDAO = new FolderMetadataDAO();
        FileMetadataDAO fileDAO = new FileMetadataDAO();
        Folder folder = Folder.fromPathDisplay("/Folder/").setOwnerID(1);
        Folder inside = Folder.fromPathDisplay("/Folder/Inside/").setOwnerID(1);
        File file1 = File.fromPathDisplay("/Folder/file").setOwnerID(1);
        File file2 = File.fromPathDisplay("/Folder/Inside/file").setOwnerID(1);
        folderDAO.store(folder);
        folderDAO.store(inside);
        fileDAO.upload(file1);
        fileDAO.upload(file2);
        FolderContentsController controller = new FolderContentsController();

        when(request.params("path")).thenReturn("/folder/");
        when(request.queryParams("recursive")).thenReturn("false");
        when(request.attribute("ownerID")).thenReturn(1);

        FolderContents folderContents = (FolderContents)controller.handleListFolderContents(request, response);

        assertThat(folderContents.getFolders()).hasSize(1);
        assertThat(folderContents.getFiles()).hasSize(1);
    }

    @Test
    public void handleListFolderContentsRecursively() {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        FolderMetadataDAO folderDAO = new FolderMetadataDAO();
        FileMetadataDAO fileDAO = new FileMetadataDAO();
        Folder folder = Folder.fromPathDisplay("/Folder/").setOwnerID(1);
        Folder inside = Folder.fromPathDisplay("/Folder/Inside/").setOwnerID(1);
        File file1 = File.fromPathDisplay("/Folder/file").setOwnerID(1);
        File file2 = File.fromPathDisplay("/Folder/Inside/file").setOwnerID(1);
        folderDAO.store(folder);
        folderDAO.store(inside);
        fileDAO.upload(file1);
        fileDAO.upload(file2);
        FolderContentsController controller = new FolderContentsController();

        when(request.params("path")).thenReturn("/"); // root folder
        when(request.queryParams("recursive")).thenReturn("true");
        when(request.attribute("ownerID")).thenReturn(1);

        FolderContents folderContents = (FolderContents)controller.handleListFolderContents(request, response);

        assertThat(folderContents.getFolders()).hasSize(2);
        assertThat(folderContents.getFiles()).hasSize(2);
    }

}