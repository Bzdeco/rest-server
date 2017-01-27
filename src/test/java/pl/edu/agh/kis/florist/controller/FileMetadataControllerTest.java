package pl.edu.agh.kis.florist.controller;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.kis.florist.dao.FileContentsDAO;
import pl.edu.agh.kis.florist.dao.FileMetadataDAO;
import pl.edu.agh.kis.florist.dao.FolderMetadataDAO;
import pl.edu.agh.kis.florist.db.tables.pojos.FileContents;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;
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
public class FileMetadataControllerTest {

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
    public void handleMoveFileBetweenFolders() throws Exception {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        FolderMetadataDAO folderDAO = new FolderMetadataDAO();
        FileMetadataDAO fileDAO = new FileMetadataDAO();
        Folder folder = Folder.fromPathDisplay("/Folder/").setOwnerID(1);
        Folder target = Folder.fromPathDisplay("/Target/").setOwnerID(1);
        File file = File.fromPathDisplay("/Folder/File").setOwnerID(1);
        folderDAO.store(folder);
        folderDAO.store(target);
        fileDAO.upload(file);
        FileMetadataController controller = new FileMetadataController();

        when(request.params("path")).thenReturn("/folder/file");
        when(request.queryParams("new_path")).thenReturn("/target/");
        when(request.attribute("ownerID")).thenReturn(1);

        FileMetadata result = (FileMetadata)controller.handleMove(request, response);

        assertThat(result).extracting(
                FileMetadata::getName,
                FileMetadata::getPathLower,
                FileMetadata::getPathDisplay,
                FileMetadata::getOwnerId).containsOnly("File", "/target/file", "/Target/File", 1);
    }

    @Test
    public void handleRenameFileInsideFolder() throws Exception {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        FolderMetadataDAO folderDAO = new FolderMetadataDAO();
        FileMetadataDAO fileDAO = new FileMetadataDAO();
        Folder folder = Folder.fromPathDisplay("/Folder/").setOwnerID(1);
        File file = File.fromPathDisplay("/Folder/File").setOwnerID(1);
        folderDAO.store(folder);
        fileDAO.upload(file);
        FileMetadataController controller = new FileMetadataController();

        when(request.params("path")).thenReturn("/folder/file");
        when(request.queryParams("new_name")).thenReturn("Renamed");
        when(request.attribute("ownerID")).thenReturn(1);

        FileMetadata result = (FileMetadata)controller.handleRename(request, response);

        assertThat(result).extracting(
                FileMetadata::getName,
                FileMetadata::getPathLower,
                FileMetadata::getPathDisplay,
                FileMetadata::getOwnerId).containsOnly("Renamed", "/folder/renamed", "/Folder/Renamed", 1);
    }

    @Test
    public void handleUploadFileToRootFolder() throws Exception {
        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.params("path")).thenReturn("/File");
        when(request.body()).thenReturn("");
        when(request.attribute("ownerID")).thenReturn(1);

        FileMetadataController controller = new FileMetadataController();

        FileMetadata result = (FileMetadata)controller.handleUpload(request, response);

        assertThat(result).extracting(
                FileMetadata::getName,
                FileMetadata::getPathLower,
                FileMetadata::getPathDisplay,
                FileMetadata::getEnclosingFolderId,
                FileMetadata::getSize,
                FileMetadata::getOwnerId).containsOnly("File", "/file", "/File", 0, 0, 1);
    }

    @Test
    public void handleDownload() throws Exception {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        File file = File.fromPathDisplay("/File").setOwnerID(1);
        FileMetadataDAO fileDAO = new FileMetadataDAO();
        FileContentsDAO contentsDAO = new FileContentsDAO();
        FileMetadata uploadedFile = fileDAO.upload(file);
        FileContents fileContents = new FileContents(uploadedFile.getFileId(), "cos".getBytes());
        contentsDAO.upload(fileContents);

        when(request.params("path")).thenReturn("/file");
        when(request.attribute("ownerID")).thenReturn(1);

        FileMetadataController controller = new FileMetadataController();
        String downloadedContent = (String)controller.handleDownload(request, response);

        assertThat(downloadedContent).containsOnlyOnce("cos");
    }

}