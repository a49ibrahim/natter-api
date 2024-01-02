package com.manning.apisecurityinaction;

import java.nio.file.*;

import com.manning.apisecurityinaction.controller.*;
import org.dalesbred.Database;
import org.dalesbred.result.EmptyResultException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.json.*;
import spark.*;



import static spark.Spark.*;

public class Main {

    public static void main(String... args) throws Exception {
        var datasource = JdbcConnectionPool.create(
            "jdbc:h2:mem:natter", "natter", "password");
        var database = Database.forDataSource(datasource);
        createTables(database);
        datasource = JdbcConnectionPool.create(
            "jdbc:h2:mem:natter", "natter_api_user", "password");
        database = Database.forDataSource(datasource);
       
        // database = Database.forDataSource(datasource);
        var spaceController = new SpaceController(database);
        post("/spaces", spaceController::createSpace);

        after((request, response) -> {
            response.type("application/json");
    
        });

        internalServerError(new JSONObject()
            .put("error", "internal server error").toString());
        notFound(new JSONObject()
            .put("error", "not found").toString());

        Exception(IllegalArgumentException.class, Main::badRequest);
        Exception(JSONException.class, Main::badRequest);
        Exception(EmptyResultException.class, (e, request, response) -> response.status(404));

      
    }

    private static void badRequest(Exception ex, Request request, Response response) {
      response.status(400);
      response.body(new JSONObject().put("error", ex.getMessage()).toString());
    }

    private static void createTables(Database database) throws Exception {
        var path = Paths.get(
                Main.class.getResource("/schema.sql").toURI());
        database.update(Files.readString(path));
    }
}