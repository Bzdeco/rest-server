package pl.edu.agh.kis.florist.controller;

import pl.edu.agh.kis.florist.dao.FolderMetadataDAO;
import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;
import pl.edu.agh.kis.florist.exceptions.PathFormatException;
import pl.edu.agh.kis.florist.model.Folder;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * Created by bzdeco on 09.01.17.
 */
public class FolderMetadataController extends ResourcesController {

    private final FolderMetadataDAO folderMetadataDAO;

    public FolderMetadataController(FolderMetadataDAO folderMetadataDAO) {
        this.folderMetadataDAO = folderMetadataDAO;
    }

    public Object handleAllFolders(Request request, Response response) {
        List<FolderMetadata> result = folderMetadataDAO.loadAllFolders();
        return result;
    }

    public Object handleCreateNewFolder(Request request, Response response) {
        String pathDisplay = request.params("path");
        int ownerID = request.attribute("ownerID");

        if(!QueryParameters.validateFolderPathFormat(pathDisplay))
            throw new PathFormatException("Path " + pathDisplay + " has wrong format");

        Folder folderMetadata = (Folder)Folder.fromPathDisplay(pathDisplay).updateCreatedTime().setOwnerID(ownerID);

        FolderMetadata result = folderMetadataDAO.store(folderMetadata);
        response.status(CREATED);
        return result;
    }

    @Override
    public Object handleMove(Request request, Response response) {
        // Moved folder can be specified by pathLower or pathDisplay
        String oldPath = request.params("path").toLowerCase();
        String newPath = request.queryParams("new_path");
        int ownerID = request.attribute("ownerID");

        if(!QueryParameters.validateFolderPathFormat(newPath))
            throw new PathFormatException("Path " + newPath + " has wrong format");

        // Create folder objects from given paths
        Folder source = (Folder)Folder.fromPathLower(oldPath).setOwnerID(ownerID);
        Folder dest = (Folder)Folder.fromPathDisplay(newPath).setOwnerID(ownerID);

        FolderMetadata result = folderMetadataDAO.move(source, dest);
        response.status(SUCCESSFUL);
        return result;
    }

    @Override
    public Object handleDelete(Request request, Response response) {
        String deletedFolderPath = request.params("path").toLowerCase();
        int ownerID = request.attribute("ownerID");

        Folder deletedFolder = (Folder)Folder.fromPathLower(deletedFolderPath).setOwnerID(ownerID);
        FolderMetadata result = folderMetadataDAO.delete(deletedFolder);
        response.status(SUCCESSFUL_DELETE);
        return result;
    }

    @Override
    public Object handleGetMetadata(Request request, Response response) {
        String folderLowerPath = request.params("path").toLowerCase();
        int ownerID = request.attribute("ownerID");

        Folder retrieved = (Folder)Folder.fromPathLower(folderLowerPath).setOwnerID(ownerID);
        FolderMetadata result = folderMetadataDAO.getMetadata(retrieved);
        response.status(SUCCESSFUL);
        return result;
    }

    @Override
    public Object handleRename(Request request, Response response) {
        String sourcePathLower = request.params("path").toLowerCase();
        String renamedName = request.queryParams("new_name");
        int ownerID = request.attribute("ownerID");

        Folder source = (Folder)Folder.fromPathLower(sourcePathLower).setOwnerID(ownerID);
        Folder fetchedSource = new Folder(folderMetadataDAO.getMetadata(source));

        QueryParameters.validateResourceNameFormat(renamedName);
        Folder renamed = (Folder)Folder.fromPathDisplay(fetchedSource.getPathDisplayToParent() + renamedName + "/").setOwnerID(ownerID);

        FolderMetadata result = folderMetadataDAO.rename(source, renamed);
        response.status(SUCCESSFUL);
        return result;
    }
}
