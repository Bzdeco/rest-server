package pl.edu.agh.kis.florist.controller;

import pl.edu.agh.kis.florist.dao.FolderMetadataDAO;
import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;
import spark.Request;
import spark.Response;

/**
 * Created by bzdeco on 16.01.17.
 */
public abstract class ResourcesController extends DefaultController {

    public abstract Object handleMove(Request request, Response response);
    public abstract Object handleRename(Request request, Response response);
    public abstract Object handleDelete(Request request, Response response);
    public abstract Object handleGetMetadata(Request request, Response response);

    public static ResourcesController getSpecifiedController(String resourcePathLower) {

        Class<?> resourceTypeClass = QueryParameters.resolveResourceTypeFromPath(resourcePathLower);

        if(resourceTypeClass.equals(FolderMetadata.class)) {
            return new FolderMetadataController(new FolderMetadataDAO());
        }
        else if(resourceTypeClass.equals(FileMetadata.class)) {
            // TODO correct constructor
            return new FileMetadataController();
        }
        else
            return null; // should never get here
    }
}
