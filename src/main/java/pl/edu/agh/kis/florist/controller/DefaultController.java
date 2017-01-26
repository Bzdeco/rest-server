package pl.edu.agh.kis.florist.controller;

import com.google.gson.Gson;

/**
 * Created by bzdeco on 23.01.17.
 */
public class DefaultController {

    protected static final int SUCCESSFUL = 200;
    protected static final int CREATED = 201;
    protected static final int SUCCESSFUL_DELETE = 204;

    protected final Gson gson = new Gson();
}
