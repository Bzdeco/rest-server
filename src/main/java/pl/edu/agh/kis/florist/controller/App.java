package pl.edu.agh.kis.florist.controller;

import static pl.edu.agh.kis.florist.db.Tables.AUTHORS;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import pl.edu.agh.kis.florist.db.tables.pojos.Authors;
import pl.edu.agh.kis.florist.db.tables.records.AuthorsRecord;
import spark.Request;
import spark.ResponseTransformer;

public class App {

	final static private Logger LOGGER = LoggerFactory.getILoggerFactory().getLogger("requests");
	
	public static void main(String[] args) {
		final int CREATED = 201;
		final String AUTHORS_PATH = "/authors";
		
		final Gson gson = new Gson();
		final ResponseTransformer json = gson::toJson;
		final String DB_URL = "jdbc:sqlite:test.db";
		
		port(4567);
		
		before("/*/", (req, res) -> { 
		    info(req);
		});
		
		
			get(AUTHORS_PATH, (request,response) -> {
				try(DSLContext create = DSL.using(DB_URL)) {
					int numberOfRows = 100;
					List<Authors> authors = create.selectFrom(AUTHORS).limit(numberOfRows).fetchInto(Authors.class);
					return authors;
				}
			},json);
			
			post(AUTHORS_PATH, (request,response) -> {
				try(DSLContext create = DSL.using(DB_URL)) {
					Authors author = gson.fromJson(request.body(),Authors.class);
					AuthorsRecord record = create.newRecord(AUTHORS);
					record.from(author);
					record.store();
					response.status(CREATED);
					return record.into(Authors.class);
				}
			},json);
		
	}

	private static void info(Request req) {
		LOGGER.info("{}",req);
	}

}
