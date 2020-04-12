/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.com.sec.ynlogurlscan.commom;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public class MongoDBClient {

    private MongoClient client;
    private MongoCollection<Document> collection;
    private MongoDatabase db;

    public MongoDBClient(String url, String collectionName) {
        //Creates a MongoURI from the given string.
        MongoClientURI uri = new MongoClientURI(url);
        //Creates a MongoClient described by a URI.
        this.client = new MongoClient(uri);
        
        //Gets a Database.
        this.db = client.getDatabase(uri.getDatabase());
        //Gets a collection.
        this.collection = db.getCollection(collectionName);
    }

    public MongoDBClient(String url, String collectionName, String user, String passwd) {

        MongoClientURI uri = new MongoClientURI(url);

        this.client = new MongoClient(uri);

        this.db = client.getDatabase(uri.getDatabase());

        this.collection = db.getCollection(collectionName);
    }

        public MongoDBClient(String url) {
            //Creates a MongoURI from the given string.
            MongoClientURI uri = new MongoClientURI(url);
            //Creates a MongoClient described by a URI.
            this.client = new MongoClient(uri);
            //Gets a Database.
            this.db = client.getDatabase(uri.getDatabase());
        }

    public MongoDatabase getdb() {
        return this.db;
    }

    /**
     * Inserts one or more documents.
     * This method is equivalent to a call to the bulkWrite method.
     * The documents will be inserted in the order provided, 
     * stopping on the first failed insertion. 
     * 
     * @param documents
     */
    public void insertAll(List<Document> documents, boolean ordered) {
        InsertManyOptions options = new InsertManyOptions();
        if (!ordered) {
            options.ordered(false);
        }
        collection.insertMany(documents, options);
    }

    public void insert(Document document) {
        collection.insertOne(document);
    }

    //    public void insert(String collectionName,Document document){
    //    	this.collection = db.getCollection(collectionName);
    //    	this.collection.insertOne(document);
    //    }    

    /**
     * Update a single or all documents in the collection according to the specified arguments.
     * When upsert set to true, the new document will be inserted if there are no matches to the query filter.
     * 
     * @param filter
     * @param document
     * @param upsert a new document should be inserted if there are no matches to the query filter
     * @param many whether find all documents according to the query filter
     */
    public void update(Bson filter, Bson document, boolean upsert, boolean many) {
        //TODO batch updating
        UpdateOptions options = new UpdateOptions();
        if (upsert) {
            options.upsert(true);
        }
        if (many) {
            collection.updateMany(filter, document, options);
        } else {
            collection.updateOne(filter, document, options);
        }
    }


    public void newUpdate(Bson filter, Document document, boolean upsert, boolean many) {
        //TODO batch updating
        UpdateOptions options = new UpdateOptions();
        if (upsert) {
            options.upsert(true);
        }
        if (many) {
            collection.updateMany(filter, document, options);
        } else {
            collection.replaceOne(filter, document, options);
        }
    }

    /**
     * Finds a single document in the collection according to the specified arguments.
     *
     * @param filter
     */
    //    public Document find(String collectionName,Bson filter) {
    //    	this.collection = db.getCollection(collectionName);
    //        //TODO batch finding
    //        return collection.find(filter).first();
    //    }

    //    public FindIterable<Document> find(String collectionName) {
    //    	this.collection = db.getCollection(collectionName);
    //        //TODO batch finding
    //        return collection.find();
    //    }

    public Document find(Bson filter) {
        //TODO batch finding
        return collection.find(filter).limit(1).first();
    }
    
    public Document findByIp(Bson filter) {
        //TODO batch finding
        return collection.find(filter).sort(new Document("ipdeci",1)).limit(1).first();
    }

    public FindIterable<Document> findAll() {
        //TODO batch finding
        return collection.find();
    }

    /**
     * Closes all resources associated with this instance.
     */
    public void close() {
        client.close();
    }

}
