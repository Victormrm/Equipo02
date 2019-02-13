package com.uclm.equipo02.persistencia;

import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoBroker {
	private static MongoBroker yo;
	private MongoClientURI uri;
	private MongoClient mongoClient;
	private MongoDatabase db;

	private MongoBroker(){
		this.uri= new MongoClientURI("mongodb://equipo02:equipo02gps@ds115740.mlab.com:15740/fichajes");
		this.mongoClient= new MongoClient(uri);
		this.db=mongoClient.getDatabase(uri.getDatabase());
	}

	public static MongoBroker get(){
		if (yo==null){
			yo = new MongoBroker();
		}
		return yo;
	}

	public MongoCollection<Document> getCollection (String collection){
		MongoCollection <Document> result=db.getCollection(collection, Document.class);

		if(result==null){
			db.createCollection(collection);
			result=db.getCollection(collection,Document.class);
		}

		return result;
	}

	public void insertDoc(MongoCollection<Document> collection, Document documento) {
		collection.insertOne(documento);
	}

	public void updateDoc(MongoCollection<Document> collection, Document filtro, Document documento) {
		collection.updateOne(filtro, documento);
	}
	public void deleteDoc(MongoCollection<Document> collection, Document documento) {
		collection.deleteOne(documento);
	}



}