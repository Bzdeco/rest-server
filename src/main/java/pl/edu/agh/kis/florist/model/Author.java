package pl.edu.agh.kis.florist.model;

import static pl.edu.agh.kis.florist.db.Tables.AUTHORS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;


public class Author {
	
	public Author(String firstName, String lastName) {
		super();
		this.id = null;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public void create() {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            DSLContext context = DSL.using(conn, SQLDialect.MYSQL);
			Record record =
					context.insertInto(AUTHORS, AUTHORS.FIRST_NAME, AUTHORS.LAST_NAME)
					      .values(firstName, lastName)
					      .returning(AUTHORS.ID)
					      .fetchOne();
	
			System.out.println(record.getValue(AUTHORS.ID));
			this.id = record.getValue(AUTHORS.ID);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	public static Collection<Author> all() {
		Collection<Author> result = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db")) {
			
            DSLContext context = DSL.using(conn, SQLDialect.MYSQL);
			Result<Record3<Integer, String, String>> records =
					context.select(AUTHORS.ID,  AUTHORS.FIRST_NAME, AUTHORS.LAST_NAME).from(AUTHORS).fetch();
	
			for (Record record : records) {
				System.out.println(record.getValue(AUTHORS.ID));
				Author a = new Author(record.getValue(AUTHORS.FIRST_NAME),record.getValue(AUTHORS.LAST_NAME));
				a.id = record.getValue(AUTHORS.ID);
				result.add(a);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	

	private Integer id;
	private String firstName;
	private String lastName;
}
