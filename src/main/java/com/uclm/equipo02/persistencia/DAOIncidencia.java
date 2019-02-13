package com.uclm.equipo02.persistencia;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.ObjectId;


import com.uclm.equipo02.modelo.Modelo;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

public class DAOIncidencia{



	public static MongoCollection<Document> getIncidencias() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> incidencias = broker.getCollection("Incidencias");
		return incidencias;
	}


	/**
	 * 
	 * 
	 * @method metodo usado para obtener la hora exacta en Espana
	 * 
	 **/

	public static String getCurrentTimeUsingCalendar() {
		Calendar cal = Calendar.getInstance();
		Date date=cal.getTime();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		String formattedTime=dateFormat.format(date);
		return formattedTime;

	}

	public void insert(Modelo incidencia) {
		Document documento = new Document();

		documento.append("nombreUsuario", incidencia.getNombreUsuario());
		documento.append("dniUsuario", incidencia.getDniUsuario());
		documento.append("categoria", incidencia.getCategoria());
		documento.append("fechaCreacion", incidencia.getFechaCreacion());
		documento.append("descripcion", incidencia.getDescripcion());
		documento.append("estado", incidencia.getEstado());
		documento.append("comentarioGestor", incidencia.getComentarioGestor());

		MongoCollection<Document> incidencias = getIncidencias();
		incidencias.insertOne(documento);
	}

	public static Date parserFecha(String fecha) {
		Date fechaparseada=new Date();

		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			fechaparseada=format.parse(fecha);
			return fechaparseada;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fechaparseada;

	}

	public static List<Document> getIncidenciasGestor() {


		List<Document> incidenciasGestor = new ArrayList<Document>();
		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();

		while(elementos.hasNext()) {
			documento = elementos.next();
			if(documento.get("estado").toString().equalsIgnoreCase("En espera"))

				incidenciasGestor.add(documento);
		}

		return incidenciasGestor;
	}

	public boolean existeIncidenciasEspera() {
		boolean bool=false;
		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();

			if(documento.get("estado").toString().equalsIgnoreCase("En espera")) {
				bool=true;

			}

		}
		return bool;

	}


	public Modelo buscarIncidenciaID(ObjectId id) {
		Modelo inci=new Modelo();

		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();

			if(documento.get("_id").toString().equalsIgnoreCase(id.toString())) {
				inci.set_id(id);
				inci.setNombreUsuario(documento.get("nombreUsuario").toString());
				inci.setDniUsuario(documento.get("dniUsuario").toString());
				inci.setCategoria(documento.get("categoria").toString());
				inci.setDescripcion(documento.get("descripcion").toString());
				inci.setEstadoIncidencia(documento.get("estado").toString());
				inci.setFechaCreacion(documento.get("fechaCreacion").toString());
				inci.setComentarioGestor(documento.get("comentarioGestor").toString());
			}

		}

		return inci;
	}

	public Modelo resolverIncidencia(ObjectId id,String textoGestor) {
		Modelo inci=new Modelo();

		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();

			if(documento.get("_id").toString().equalsIgnoreCase(id.toString())) {
				inci.set_id(id);
				inci.setNombreUsuario(documento.get("nombreUsuario").toString());
				inci.setDniUsuario(documento.get("dniUsuario").toString());
				inci.setCategoria(documento.get("categoria").toString());
				inci.setDescripcion(documento.get("descripcion").toString());
				inci.setEstadoIncidencia("Resuelta");
				inci.setFechaCreacion(documento.get("fechaCreacion").toString());
				inci.setComentarioGestor(textoGestor);
			}

		}

		return inci;
	}

	public Modelo denegarIncidencia(ObjectId id,String textoGestor) {
		Modelo inci=new Modelo();

		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();

			if(documento.get("_id").toString().equalsIgnoreCase(id.toString())) {
				inci.set_id(id);
				inci.setNombreUsuario(documento.get("nombreUsuario").toString());
				inci.setDniUsuario(documento.get("dniUsuario").toString());
				inci.setCategoria(documento.get("categoria").toString());
				inci.setDescripcion(documento.get("descripcion").toString());
				inci.setEstadoIncidencia("Denegada");
				inci.setFechaCreacion(documento.get("fechaCreacion").toString());
				inci.setComentarioGestor(textoGestor);
			}

		}

		return inci;
	}

	public void updateIncidencia(Modelo incidencia,String modo) throws Exception {
		MongoCollection<Document> incidencias = getIncidencias();
		MongoBroker broker = MongoBroker.get();


		if(modo.equalsIgnoreCase("denegar") || modo.equalsIgnoreCase("resolver")) {


			Document criteria=new Document();

			criteria.put("_id", incidencia.get_id());

			Document changes=new Document();

			changes.put("estado", incidencia.getEstado());
			changes.put("comentarioGestor", incidencia.getComentarioGestor());
			Document doc = new Document();
			doc.put("$set", changes);

			broker.updateDoc(incidencias, criteria, doc);
		}else if(modo.equalsIgnoreCase("modificar")){
			Document criteria=new Document();
			Document changes=new Document();
			Document doc = new Document();
			
			criteria.put("_id", incidencia.get_id());
			
			changes.put("categoria", incidencia.getCategoria());
			changes.put("fechaCreacion", incidencia.getFechaCreacion());
			changes.put("descripcion", incidencia.getDescripcion());
			
			doc.put("$set", changes);
			
			broker.updateDoc(incidencias, criteria, doc);
		}


	}
	public static List<Document> devolverIncidencias(String dniEmpleado){
		List<Document> incidencias = new ArrayList<Document>();
		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();
			if(documento.get("dniUsuario").toString().equalsIgnoreCase(dniEmpleado))
				incidencias.add(documento);
		}
		return incidencias;
	}

	public boolean existeIncidencias(String dni) {
		boolean bool=false;
		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();

			if(documento.get("dniUsuario").toString().equalsIgnoreCase(dni)) {
				bool=true;

			}

		}
		return bool;

	}
	public static List<Document> getIncidencias(String dni) {
		List<Document> incidenciasGestor = new ArrayList<Document>();
		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();
			if(documento.get("dniUsuario").toString().equalsIgnoreCase(dni))

				incidenciasGestor.add(documento);
		}

		return incidenciasGestor;
	}
	
	public Modelo devolverIncidencia(ObjectId id, String categoria, String fecha, String descripcion) {
		Modelo inci=new Modelo();

		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();

			if(documento.get("_id").toString().equalsIgnoreCase(id.toString())) {
				inci.set_id(id);
				inci.setNombreUsuario(documento.get("nombreUsuario").toString());
				inci.setDniUsuario(documento.get("dniUsuario").toString());
				inci.setCategoria(categoria);
				inci.setDescripcion(descripcion);
				inci.setEstadoIncidencia("En espera");
				inci.setFechaCreacion(fecha);
			}

		}

		return inci;
	}
	public Modelo devolverIncidencia(ObjectId id) {
		Modelo inci=new Modelo();

		Document documento = new Document();
		MongoCursor<Document> elementos = getIncidencias().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();

			if(documento.get("_id").toString().equalsIgnoreCase(id.toString())) {
				inci.set_id(id);
				inci.setNombreUsuario(documento.get("nombreUsuario").toString());
				inci.setDniUsuario(documento.get("dniUsuario").toString());
				inci.setCategoria(documento.get("categoria").toString());
				inci.setDescripcion(documento.get("descripcion").toString());
				inci.setEstadoIncidencia(documento.getString("estado").toString());
				inci.setFechaCreacion(documento.get("fechaCreacion").toString());
				inci.setComentarioGestor(documento.get("comentarioGestor").toString());
			}

		}

		return inci;
	}
	
	public void delete (Modelo incidencia){
		Document bso = new Document();
		MongoCollection<Document> incidencias = getIncidencias();
		
		incidencias.deleteOne(new Document("_id", new ObjectId(incidencia.get_id().toString())));
	}

}
