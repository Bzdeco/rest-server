package pl.edu.agh.kis.florist.controller;

import pl.edu.agh.kis.florist.dao.FolderContentsDAO;
import pl.edu.agh.kis.florist.dao.FolderMetadataDAO;
import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;
import pl.edu.agh.kis.florist.model.Folder;
import spark.Request;
import spark.Response;

/**
 * Created by bzdeco on 18.01.17.
 */
public class FolderContentsController {

    private final FolderMetadataDAO folderMetadataDAO;
    private final FolderContentsDAO folderContentsDAO;

    public FolderContentsController() {
        this.folderMetadataDAO = new FolderMetadataDAO();
        this.folderContentsDAO = new FolderContentsDAO();
    }

    public FolderContentsController(FolderMetadataDAO folderMetadataDAO, FolderContentsDAO folderContentsDAO) {
        this.folderMetadataDAO = folderMetadataDAO;
        this.folderContentsDAO = folderContentsDAO;
    }

    public Object handleListFolderContents(Request request, Response response) {
        String pathDisplay = request.params("path");
        int ownerID = request.attribute("ownerID");
        QueryParameters.resolveResourceTypeFromPath(pathDisplay);

        String recursiveValue = request.queryParams("recursive");
        boolean recursive = recursiveValue.equals("true");

        Folder folder = Folder.fromPathDisplay(pathDisplay).setOwnerID(ownerID);
        FolderMetadata fetchedFolder = folderMetadataDAO.getMetadata(folder);

        return folderContentsDAO.getFolderContents(new Folder(fetchedFolder), recursive);
    }
}
