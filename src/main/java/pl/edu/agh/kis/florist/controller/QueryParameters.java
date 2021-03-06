package pl.edu.agh.kis.florist.controller;

import pl.edu.agh.kis.florist.db.tables.pojos.FileMetadata;
import pl.edu.agh.kis.florist.db.tables.pojos.FolderMetadata;
import pl.edu.agh.kis.florist.exceptions.PathFormatException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is an utility class used to validate parameters provided in a query from client. It checks if paths don't contain forbidden characters (any other than a-zA-Z_0-9).
 */
public class QueryParameters {
    public static Class<?> resolveResourceTypeFromPath(String resourcePath) {

        if(validateFolderPathFormat(resourcePath))
            return FolderMetadata.class;
        else if(validateFilePathFormat(resourcePath))
            return FileMetadata.class;
        else
            throw new PathFormatException("Path " + resourcePath + " has wrong format");
    }

    public static boolean validateFilePathFormat(String resourcePath) {
        // No non-word characters and slashes inside names of folders and files
        String filePathRegex = "(/[\\w]+)+$";
        Pattern filePattern = Pattern.compile(filePathRegex);
        Matcher fileMatcher = filePattern.matcher(resourcePath);

        return fileMatcher.matches();
    }

    public static boolean validateFolderPathFormat(String resourcePath) {
        // No non-word characters and slashes inside names of folders
        String folderPathRegex = "(/[\\w]+)*/$"; // root can have path "/"
        Pattern folderPattern = Pattern.compile(folderPathRegex);
        Matcher folderMatcher = folderPattern.matcher(resourcePath);

        return folderMatcher.matches();
    }

    public static boolean validateResourceNameFormat(String newName) {
        // No non-word characters and slashes inside resource name, at least one-character name
        String resourceNameRegex = "[\\w]+";
        Pattern resourcePatter = Pattern.compile(resourceNameRegex);
        Matcher resourceMatcher = resourcePatter.matcher(newName);

        return resourceMatcher.matches();
    }
}
