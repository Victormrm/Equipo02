package com.uclm.equipo02.persistencia;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.uclm.equipo02.modelo.Modelo;

public class DAOAdmin {
	
	
	
	public MongoCollection<Document> getUsuarios() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> usuarios = broker.getCollection("Usuarios");
		return usuarios;
	}

	
	
	public Modelo buscarUsuarioEmail(String dni) {
		Modelo user = new Modelo();
		
		Document documento = new Document();
		MongoCursor<Document> elementos = getUsuarios().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();
				if(documento.get("dni").toString().equalsIgnoreCase(dni)) {
					user.setEmail(documento.get("email").toString());
					user.setNombre(documento.get("nombre").toString());
					user.setPassword(documento.get("pwd").toString());
					user.setRol(documento.get("rol").toString());
					user.setDni(dni);
					
				}
		}
		
		return user;
	}
	
	
	public boolean existeUser(String dni) {
		boolean bool=false;
		Document documento = new Document();
		MongoCursor<Document> elementos = getUsuarios().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();
				
					if(documento.get("dni").toString().equalsIgnoreCase(dni)) {
						bool=true;
	
				}
				
		}
		return bool;
		
	}
	
	
	
}
