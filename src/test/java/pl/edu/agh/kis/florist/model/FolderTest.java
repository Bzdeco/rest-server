package pl.edu.agh.kis.florist.model;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bzdeco on 11.01.17.
 */
public class FolderTest {
    @Test
    public void getPathToParent() throws Exception {
        Folder outside = new Folder(1, "outside", "/outside/", "/outside/", 0, Resource.getCurrentTime(), 1);
        Folder inside = new Folder(2, "inside", "/outside/inside/", "/outside/inside/", 1, Resource.getCurrentTime(), 1);

        assertThat(inside.getPathLowerToParent()).isEqualTo(outside.getPathLower());
    }

}