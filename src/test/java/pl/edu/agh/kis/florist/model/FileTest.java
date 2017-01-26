package pl.edu.agh.kis.florist.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bzdeco on 16.01.17.
 */
public class FileTest {
    @Test
    public void getPathToParent() throws Exception {
        Folder folder = new Folder(1, "folder", "/folder/", "/folder/", 0, Resource.getCurrentTime(), 1);
        File file = new File(1, "file", "/folder/file", "/folder/file", 1, 1024, Resource.getCurrentTime(), Resource.getCurrentTime(),1);

        assertThat(file.getPathLowerToParent()).isEqualTo(folder.getPathLower());
    }

}