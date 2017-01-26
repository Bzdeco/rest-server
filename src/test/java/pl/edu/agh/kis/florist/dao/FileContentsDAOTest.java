package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.edu.agh.kis.florist.db.tables.pojos.FileContents;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Resource;

import static pl.edu.agh.kis.florist.db.Tables.FILE_CONTENTS;
import static pl.edu.agh.kis.florist.db.Tables.FILE_METADATA;
import static pl.edu.agh.kis.florist.db.Tables.FOLDER_METADATA;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bzdeco on 21.01.17.
 */
public class FileContentsDAOTest {

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
    public void uploadFileContent() throws Exception {
        byte[] content = "content".getBytes();
        FileMetadata uploadedFile = new FileMetadata(1, "file", "/file", "/file", 0, content.length, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);
        FileContents fileContents = new FileContents(1, content);
        FileContentsDAO dao = new FileContentsDAO();

        FileContents uploaded = dao.upload(fileContents);

        assertThat(uploaded).extracting(
                FileContents::getFileId,
                FileContents::getContents).containsOnly(1, content);
    }

    @Test
    public void downloadFileContent() throws Exception {
        byte[] content = "content".getBytes();
        FileMetadata uploadedFileMetadata = new FileMetadata(1, "file", "/file", "/file", 0, content.length, Resource.getCurrentTime(), Resource.getCurrentTime(), 0);
        File uploadedFile = new File(uploadedFileMetadata);
        FileContents fileContents = new FileContents(1, content);
        FileContentsDAO dao = new FileContentsDAO();
        dao.upload(fileContents);

        String retrievedContent = dao.download(uploadedFile);

        assertThat(retrievedContent).isEqualTo(new String(content));
    }

}