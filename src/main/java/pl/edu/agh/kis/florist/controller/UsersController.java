package pl.edu.agh.kis.florist.controller;

import pl.edu.agh.kis.florist.dao.SessionDataDAO;
import pl.edu.agh.kis.florist.dao.UsersDAO;
import pl.edu.agh.kis.florist.db.tables.pojos.Users;
import pl.edu.agh.kis.florist.exceptions.AuthorizationRequiredException;
import spark.Request;
import spark.Response;

import java.util.Optional;

/**
 * Created by bzdeco on 23.01.17.
 */
public class UsersController extends DefaultController {

    private final UsersDAO usersDAO = new UsersDAO();
    private final SessionDataDAO sessionDataDAO = new SessionDataDAO();

    public Object handleCreateUser(Request request, Response response) {
        Users user = gson.fromJson(request.body(), Users.class);

        Users storedUser = new Users(null, user.getUserName(), user.getDisplayName(), user.getHashedPassword());
        Users result = usersDAO.create(storedUser);

        response.status(CREATED);
        return result;
    }

    public Object handleLogUser(Request request, Response response) {
        String encodedCredentials = request.headers("Authorization");
        String[] decodedCredentials = UserAuthenticationUtilities.decodeAuthenticationCredentials(encodedCredentials);

        // authenticate user
        Users authenticatedUser = usersDAO.authenticate(decodedCredentials[0], decodedCredentials[1]);

        // create or get existing session id for authenticated user
        String sessionID = sessionDataDAO.createSessionIdForUser(authenticatedUser);

        response.status(SUCCESSFUL);
        response.cookie("sessionID", sessionID);
        return sessionID;
    }

    public Object handleVerifyAccess(Request request, Response response) {
        String sessionIDCookie = request.cookie("sessionID");

        if(sessionIDCookie == null) {
            throw new AuthorizationRequiredException("Cookie sessionID not set");
        }

        Optional<Integer> userID = sessionDataDAO.getUserIDassignedToSessionID(sessionIDCookie);

        if(userID.isPresent())
            return userID.get();
        else
            throw new AuthorizationRequiredException("Unauthorized access to resources");
    }
}
