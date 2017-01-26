package pl.edu.agh.kis.florist.dao;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import pl.edu.agh.kis.florist.db.tables.pojos.FileContents;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.db.tables.records.FileContentsRecord;
import pl.edu.agh.kis.florist.exceptions.FileUploadSQLException;
import pl.edu.agh.kis.florist.model.File;

import java.sql.*;

import static pl.edu.agh.kis.florist.db.Tables.FILE_CONTENTS;

/**
 * Created by bzdeco on 21.01.17.
 */
public class FileContentsDAO extends ResourcesDAO {

    public FileContentsDAO() {
        super();
    }

    public FileContentsDAO(String dbUrl) {
        super(dbUrl);
    }

    public String download(File downloadedFile) {
        try(DSLContext create = DSL.using(DB_URL)) {
            FileContents fileContents = create
                    .select(FILE_CONTENTS.fields())
                    .from(FILE_CONTENTS)
                    .where(FILE_CONTENTS.FILE_ID.eq(downloadedFile.getFileId()))
                    .fetchOneInto(FileContents.class);

            return new String(fileContents.getContents());
        }
    }

    public FileContents upload(FileContents fileContents) {
        try(DSLContext create = DSL.using(DB_URL)) {
            //uploadFileContent(fileContent);

            FileContentsRecord contentRecord = create.newRecord(FILE_CONTENTS, fileContents);
            contentRecord.store();

            return contentRecord.into(FileContents.class);
        }
    }

    // JDBC doesn't support createBlob yet
    private Blob uploadFileContent(byte[] fileContent) {
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            Blob uploadedFileBlob = con.createBlob();
            uploadedFileBlob.setBytes(1, fileContent);

            String sql = "INSERT INTO file_contents (contents) VALUES(?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql);

            preparedStatement.setBlob(1, uploadedFileBlob);
            preparedStatement.executeUpdate();

            return uploadedFileBlob;
        }
        catch (SQLException e) {
            throw new FileUploadSQLException("Could not upload file due to SQLException", e);
        }
    }
}
