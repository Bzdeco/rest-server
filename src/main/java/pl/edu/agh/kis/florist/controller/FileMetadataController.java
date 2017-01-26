package pl.edu.agh.kis.florist.controller;

import com.google.gson.Gson;
import pl.edu.agh.kis.florist.dao.FileContentsDAO;
import pl.edu.agh.kis.florist.dao.FileMetadataDAO;
import pl.edu.agh.kis.florist.db.tables.pojos.FileContents;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.exceptions.InvalidPathException;
import pl.edu.agh.kis.florist.model.File;
import pl.edu.agh.kis.florist.model.Folder;
import spark.Request;
import spark.Response;

/**
 * Created by bzdeco on 16.01.17.
 */
public class FileMetadataController extends ResourcesController {

    private final FileMetadataDAO fileMetadataDAO = new FileMetadataDAO();
    private final FileContentsDAO fileContentsDAO = new FileContentsDAO();
    private final Gson gson = new Gson();

    @Override
    public Object handleMove(Request request, Response response) {
        // Moved file can be specified by pathLower or pathDisplay
        String oldPath = request.params("path").toLowerCase();
        String newPath = request.queryParams("new_path");
        int ownerID = request.attribute("ownerID");

        // Create file and folder objects from given paths
        File source = File.fromPathLower(oldPath).setOwnerID(ownerID);
        Folder dest = Folder.fromPathDisplay(newPath).setOwnerID(ownerID);

        FileMetadata result = fileMetadataDAO.move(source, dest);
        response.status(SUCCESSFUL);
        return result;
    }

    @Override
    public Object handleRename(Request request, Response response) {
        String sourcePathLower = request.params("path").toLowerCase();
        String newName = request.queryParams("new_name");
        int ownerID = request.attribute("ownerID");

        File source = File.fromPathLower(sourcePathLower).setOwnerID(ownerID);

        // FIXME should work
        File fetched = new File(fileMetadataDAO.getMetadata(source));

        QueryParameters.validateResourceNameFormat(newName);
        File renamed = File.fromPathDisplay(fetched.getPathDisplayToParent() + newName).setOwnerID(ownerID);

        FileMetadata result = fileMetadataDAO.rename(source, renamed);
        response.status(SUCCESSFUL);
        return result;
    }

    @Override
    public Object handleDelete(Request request, Response response) {
        String deletedFilePath = request.params("path").toLowerCase();
        int ownerID = request.attribute("ownerID");

        File deletedFile = File.fromPathLower(deletedFilePath).setOwnerID(ownerID);

        FileMetadata result = fileMetadataDAO.delete(deletedFile);
        response.status(SUCCESSFUL_DELETE);
        return result;
    }

    @Override
    public Object handleGetMetadata(Request request, Response response) {
        String fileLowerPath = request.params("path").toLowerCase();
        int ownerID = request.attribute("ownerID");

        File retrieved = File.fromPathLower(fileLowerPath).setOwnerID(ownerID);

        FileMetadata result = fileMetadataDAO.getMetadata(retrieved);
        response.status(SUCCESSFUL);
        return result;
    }

    public Object handleUpload(Request request, Response response) {
        String uploadedFilePathDisplay = request.params("path");
        int ownerID = request.attribute("ownerID");
        byte[] uploadedFileContent = request.body().getBytes();

        if(!QueryParameters.validateFilePathFormat(uploadedFilePathDisplay))
            throw new InvalidPathException("File path " + uploadedFilePathDisplay + " has wrong format");

        // FileMetadata
        File uploadedFile = File.fromPathDisplay(uploadedFilePathDisplay)
                .setOwnerID(ownerID)
                .setSize(uploadedFileContent.length);
        FileMetadata result = fileMetadataDAO.upload(uploadedFile);

        // FileContents
        FileContents fileContents = new FileContents(result.getFileId(), uploadedFileContent);
        fileContentsDAO.upload(fileContents);

        response.status(CREATED);
        return result;
    }

    public Object handleDownload(Request request, Response response) {
        String downloadedFilePathLower = request.params("path").toLowerCase();
        int ownerID = request.attribute("ownerID");

        if(!QueryParameters.validateFilePathFormat(downloadedFilePathLower))
            throw new InvalidPathException("File path " + downloadedFilePathLower + " has wrong format");

        File downloadedFile = File.fromPathLower(downloadedFilePathLower).setOwnerID(ownerID);
        FileMetadata downloadedFileMetadata = fileMetadataDAO.download(downloadedFile);
        String downloadedFileContent = fileContentsDAO.download(new File(downloadedFileMetadata));
        System.out.println("==============================================");

        response.header("X-File-Metadata", gson.toJson(downloadedFileMetadata));
        response.status(SUCCESSFUL);
        return downloadedFileContent;
    }
}
