package pl.edu.agh.kis.florist.model;

import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by bzdeco on 16.01.17.
 */
public class FolderContents {
    public FolderContents() {
        folders = new LinkedList<>();
        files = new LinkedList<>();
    }

    public FolderContents(List<Folder> folders, List<File> files) {
        this.folders = folders;
        this.files = files;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public List<File> getFiles() {
        return files;
    }

    private List<Folder> folders;
    private List<File> files;
}
