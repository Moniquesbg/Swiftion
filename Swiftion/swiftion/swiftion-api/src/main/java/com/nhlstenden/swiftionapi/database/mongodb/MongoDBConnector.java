package com.nhlstenden.swiftionapi.database.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;

/*****************************************************************
 * The MongoDB connector class is responsible for everything that
 * is related to the mongoDB.
 * For instantiating a connection, to inserting a complete .940 file
 ****************************************************************/
public class MongoDBConnector {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDBConnector() {
        Dotenv dotenv = Dotenv.load();
        this.mongoClient = MongoClients.create(new ConnectionString(dotenv.get("MONGO_URL")));
        this.database = this.mongoClient.getDatabase(dotenv.get("MONGODB_DB"));
    }

    /**
     * create a mongoDB collection
     * @return true if it succeeded, otherwise false
     */
    public boolean createCollectionSolution() {
        try {
            if (this.database.getCollection("MT940") == null) {
                this.database.createCollection("MT940");
            }

            return true;
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            return false;
        }
    }

    /**
     * insert a .940 file in the MongoDB database
     * @param file file to be inserted in the MongoDB (it is validated beforehand)
     * @param userId id of the user that has uploaded the file that has uploaded the file
     * @return true if it was succesfully inserted, otherwise false
     */
    public boolean insertMT940(MultipartFile file, String userId) {
        try {
            Document document = new Document();
            document.append("file", new String(file.getBytes()));
            document.append("userId_added", userId);
            document.append("date_added", new Timestamp(System.currentTimeMillis()).toString());
            this.database.getCollection("MT940").insertOne(document);
            System.out.println("Document inserted successfully");

            return true;
        } catch (Exception exception) {
            System.err.println("Failed to insert MT940 " + exception.getMessage());

            return false;
        }
    }

}