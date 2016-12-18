package pl.edu.agh.kis.florist.controller;

import static spark.Spark.get;
import static spark.Spark.post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import pl.edu.agh.kis.florist.model.Author;
import spark.ResponseTransformer;

public class App {

	public static void main(String[] args) {
		final int CREATED = 201;
		final String AUTHORS = "/authors";
		final Logger LOGGER = LoggerFactory.getILoggerFactory().getLogger("requests");
		final Gson gson = new Gson();
		final ResponseTransformer json = gson::toJson;
		get(AUTHORS, (request,response) -> { 
			LOGGER.info(String.format(
					"{headers: %s, attributes: %s, body: %s, ",
					request.headers(),
					request.attributes(),
					request.body()));
			return Author.all();
		},json);
		post(AUTHORS, (request,response) -> {
			Author author = gson.fromJson(request.body(),Author.class);
			LOGGER.debug(author.toString());
			author.create();
			response.status(CREATED);
			return author;
		},json);	
	}

}
