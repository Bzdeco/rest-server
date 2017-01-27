package pl.edu.agh.kis.florist.controller;

import static spark.Spark.*;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import pl.edu.agh.kis.florist.dao.FolderMetadataDAO;
import pl.edu.agh.kis.florist.dao.SessionManager;
import pl.edu.agh.kis.florist.exceptions.*;
import spark.Request;
import spark.ResponseTransformer;

public class App {

	final static private Logger LOGGER = LoggerFactory.getILoggerFactory().getLogger("requests");

	public static void main(String[] args) {

		final String CREATE_USER = "/users/create_user";
		final String LOG_USER = "/users/access";
		final String DIRECTORIES_CREATE = "/files/:path/create_directory";
		final String MOVE = "/files/:path/move";
		final String DELETE = "/files/:path/delete";
		final String METADATA = "/files/:path/get_meta_data";
		final String RENAME = "/files/:path/rename";
		final String FOLDER_CONTENTS = "/files/:path/list_folder_content";
		final String UPLOAD = "/files/:path/upload";
		final String DOWNLOAD = "/files/:path/download";
		final String DIRECTORIES_PATH = "/directories";

		final Gson gson = new Gson();
		final ResponseTransformer json = gson::toJson;

		final UsersController usersController = new UsersController();
		final FileMetadataController fileMetadataController = new FileMetadataController();
		final FolderMetadataController folderMetadataController = new FolderMetadataController(new FolderMetadataDAO());
		final FolderContentsController folderContentsController = new FolderContentsController();

		// Enable HTTPS
		String keyStoreLocation = "deploy/keystore.jks";
		String keyStorePassword = "password";
		secure(keyStoreLocation, keyStorePassword, null, null);

		// Set port
		port(4567);

		// Run SessionManager
		Thread sessionManagerThread = new Thread(new SessionManager(1, 5));
		sessionManagerThread.start();

		// Logger
		before("/*/", (req, res) -> {
			info(req);
		});

		// Filter for authorizing access to resources
		before("/files/*", (request, response) -> {
			int ownerID = (int)usersController.handleVerifyAccess(request, response);

			// Modify request so it contains information about ownerID for upcoming operations
			request.attribute("ownerID", ownerID);
		});

		// Create new user
		post(CREATE_USER, (request, response) -> {
			return usersController.handleCreateUser(request, response);
		}, json);

		// Upload file
		post(UPLOAD, (request, response) -> {
			return fileMetadataController.handleUpload(request, response);
		}, json);

		// Create directory with a given path
		put(DIRECTORIES_CREATE, (request, response) -> {
			return folderMetadataController.handleCreateNewFolder(request, response);
		}, json);

		// Move existing directory or file to a new location
		put(MOVE, (request, response) -> {
			return ResourcesController.getSpecifiedController(request.params("path")).handleMove(request, response);
		}, json);

		// Rename existing directory or file
		put(RENAME, (request, response) -> {
			return ResourcesController.getSpecifiedController(request.params("path")).handleRename(request, response);
		}, json);

		// Delete existing directory or file
		delete(DELETE, (request, response) -> {
			return ResourcesController.getSpecifiedController(request.params("path")).handleDelete(request, response);
		}, json);

		// Log in user (assign sessionID for user)
		get(LOG_USER, (request, response) -> {
			return usersController.handleLogUser(request, response);
		}, json);

		// Get metadata of the resource
		get(METADATA, (request, response) -> {
			return ResourcesController.getSpecifiedController(request.params("path")).handleGetMetadata(request, response);
		}, json);

		// Get content of the given folder
		get(FOLDER_CONTENTS, (request, response) -> {
			return folderContentsController.handleListFolderContents(request, response);
		}, json);

		// List all directories
		get(DIRECTORIES_PATH, (request, response) -> {
			return folderMetadataController.handleAllFolders(request, response);
		}, json);

		// Download a file
		get(DOWNLOAD, (request, response) -> {
			return fileMetadataController.handleDownload(request, response);
		}, json);


		// Exceptions

		exception(InvalidUserException.class, (ex, request, response) -> {
			response.status(400);
			response.body(ex.getMessage());
		});

		exception(FileUploadSQLException.class, (ex, request, response) -> {
			response.status(400); // bad request
			response.body(ex.getMessage());
		});

		exception(AuthorizationRequiredException.class, (ex, request, response) -> {
			response.status(401);
			response.body(ex.getMessage());
		});

		exception(FailedAuthenticationException.class, (ex, request, response) -> {
			response.status(403);
			response.body(ex.getMessage());
		});

		exception(InvalidPathException.class, (ex, request, response) -> {
			response.status(404);
			response.body(ex.getMessage());
		});

		exception(PathFormatException.class, (ex, request, response) -> {
			response.status(405);
			response.body(ex.getMessage());
		});
	}

	private static void info(Request req) {
		LOGGER.info("{}", req);
	}

}
