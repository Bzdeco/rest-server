package pl.edu.agh.kis.florist.controller;

import org.junit.Test;
import pl.edu.agh.kis.florist.exceptions.InvalidPathException;
import pl.edu.agh.kis.florist.exceptions.PathFormatException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by bzdeco on 16.01.17.
 */
public class ResourcesControllerTest {
    @Test
    public void getSpecifiedController_FolderMetadataController() throws Exception {
        ResourcesController folderMetadataController = ResourcesController.getSpecifiedController("/path/to/folder/");

        assertThat(folderMetadataController).isInstanceOf(FolderMetadataController.class);
    }

    @Test
    public void getSpecifiedController_FileMetadataController() throws Exception {
        ResourcesController folderMetadataController = ResourcesController.getSpecifiedController("/path/to/file");

        assertThat(folderMetadataController).isInstanceOf(FileMetadataController.class);
    }

    @Test (expected = PathFormatException.class)
    public void getSpecifiedController_invalidPathFormat() {
        ResourcesController folderMetadataController = ResourcesController.getSpecifiedController("/path//invalid");
    }
}