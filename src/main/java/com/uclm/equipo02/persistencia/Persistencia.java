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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.uclm.equipo02.Auxiliar.Utilidades;
import com.uclm.equipo02.modelo.Fichaje;
import com.uclm.equipo02.modelo.Incidencia;
import com.uclm.equipo02.modelo.Usuario;

public class Persistencia {

	private MongoClientURI uri;
	private MongoClient mongoClient;
	private static MongoDatabase db;
	private final String name = "nombre";
	private final String password = "pwd";
	private final String email = "email";
	private final String rol = "rol";
	private final String dni = "dni";
	
	//MONGOBROKER
	public Persistencia(){
		this.uri= new MongoClientURI("mongodb://equipo02:equipo02gps@ds115740.mlab.com:15740/fichajes");
		this.mongoClient= new MongoClient(uri);
		this.db=mongoClient.getDatabase(uri.getDatabase());
	}

	
	public static MongoCollection<Document> getCollection (String collection){
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

	//MONGOBROKER
	
	//DAOAdmin

	public MongoCollection<Document> getUsuarios() {
		MongoCollection<Document> usuarios = getCollection("Usuarios");
		return usuarios;
	}
	
	public Usuario buscarUsuarioEmail(String dni) {
		Usuario user = new Usuario();
		
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
	
	//DAOAdmin
	
	//DAOFichaje

	public static MongoCollection<Document> getFichajes() {
		
		MongoCollection<Document> fichajes =  getCollection("Fichajes");
		return fichajes;
	}


	/**
	 * 
	 * 
	 * @method metodo usado para obtener la hora exacta en Espana
	 * 
	 **/

	public static String getCurrentTimeUsingCalendarFichaje() {
		Calendar cal = Calendar.getInstance();
		Date date=cal.getTime();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		String formattedTime=dateFormat.format(date);
		return formattedTime;

	}

	
	public ObjectId getIDFichaje(Fichaje fichaje) {
		String idfichajebd="";
		Document documento = new Document();
		MongoCursor<Document> elementos = getFichajes().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();
			if(documento.get("dniEmpleado").toString().equalsIgnoreCase(fichaje.getDNIFichaje()))
				if(documento.get("fechaFichaje").toString().equalsIgnoreCase(fichaje.getFechaFichaje()))
					if(documento.get("horaEntrada").toString().equalsIgnoreCase(fichaje.getHoraEntrada()))
						if(documento.get("estado").toString().equals(Boolean.toString(true)))
							idfichajebd=documento.get("_id").toString();
							
					

		}
		
		
		ObjectId id=new ObjectId(idfichajebd);
		return id;
		
	}

	public ObjectId abrirFichaje(Fichaje fichaje) {
		Document documento = new Document();
		

		documento.append("dniEmpleado", fichaje.getDNIFichaje());
		documento.append("fechaFichaje", fichaje.getFechaFichaje());
		documento.append("horaEntrada", fichaje.getHoraEntrada());
		documento.append("horaSalida", null);
		documento.append("estado", fichaje.getEstado());

		MongoCollection<Document> fichajes = getFichajes();
		fichajes.insertOne(documento);
		
		ObjectId id=getIDFichaje(fichaje);
		
		return id;
		
		
	}


	/**
	 * 
	 * @method Metodo de cierre de Fichajes, el metodo utiliza el criterio de nombredeEmpleado (el de la current sesion), 
	 * y la fecha del fichaje (asumiendo que no se necesitan fichajes entre dias) y cambia el estado y la horaSalida accediendo al mongoBroker
	 * que updatea el documento
	 */
	public void cerrarFichaje(Usuario usuario, Fichaje fichaje) {
		MongoCollection<Document> fichajes = getFichajes();

		Document criteria=new Document();
		criteria.put("_id", fichaje.get_id());
		criteria.put("dniEmpleado", usuario.getDni());
		criteria.put("fechaFichaje", fichaje.getFechaFichaje());

		Document changes=new Document();

		changes.put("estado", fichaje.getEstado());
		changes.put("horaSalida", fichaje.getHoraSalida());
		Document doc = new Document();
		doc.put("$set", changes);
		
		updateDoc(fichajes, criteria, doc);


	}




	public String getHoraEntrada(String dniEmpleado, String fechaFichaje) {
		String horaentrada="";
		Document documento = new Document();
		MongoCursor<Document> elementos = getFichajes().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();
			if(documento.get("dniEmpleado").toString().equalsIgnoreCase(dniEmpleado))
				if(documento.get("fechaFichaje").toString().equals(fechaFichaje))
					horaentrada=documento.getString("horaEntrada");

		}
		return horaentrada;

	}



	/**
	 * 
	 * @method Comprueba que si hay un fichaje abierto, no puedas abrir otro fichaje antes de cerrar el anterior
	 * 
	 */
	public boolean validezAbierto(Fichaje fichaje) {
		Document documento = new Document();
		MongoCursor<Document> elementos = getFichajes().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();
			if(documento.get("dniEmpleado").toString().equalsIgnoreCase(fichaje.getDNIFichaje()))//usuario sesion
				if(documento.get("fechaFichaje").toString().equals(fichaje.getFechaFichaje()))
					if(documento.get("estado").toString().equals(Boolean.toString(true)))
						return false;

		}
		return true;
	}


	/**
	 * 
	 * @method Comprueba si no hay algun fichaje abierto que se puede cerrar. Si no hay ninguno abierto, tienes que crear uno nuevo.
	 * Tambien comprueba que se actualiza el ultimo fichaje creado
	 */

	public boolean validezCerrado(Fichaje fichaje) {
		Document documento = new Document();
		MongoCursor<Document> elementos = getFichajes().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();
			if(documento.get("dniEmpleado").toString().equalsIgnoreCase(fichaje.getDNIFichaje()))//usuario sesion
				if(documento.get("fechaFichaje").toString().equals(fichaje.getFechaFichaje()))
					if(documento.get("estado").toString().equals(Boolean.toString(true))) 
						return true;

		}

		return false;
	}



	public List<Document> fichajesEmpleado(String dniEmpleado){
		List<Document> fichajesempleado = new ArrayList<Document>();
		Document documento = new Document();
		MongoCursor<Document> elementos = getFichajes().find().iterator();
		while(elementos.hasNext()) {
			documento = elementos.next();
			if(documento.get("dniEmpleado").toString().equalsIgnoreCase(dniEmpleado))
				fichajesempleado.add(documento);
		}

		return fichajesempleado;
	}




	public static List<Document> listarFichajesPeriodo(String dni, String fecha1,String fecha2) {
		List<Date> periodo=calculoPeriodoFechas(fecha1,fecha2);

		List<Document> fichajesFechaEmpleado = new ArrayList<Document>();
		Document documento = new Document();
		MongoCursor<Document> elementos = getFichajes().find().iterator();

		while(elementos.hasNext()) {
			documento = elementos.next();
			if((documento.get("dniEmpleado").toString()).equalsIgnoreCase(dni))
				if(comparacionFichajePeriodo(periodo,documento.get("fechaFichaje").toString()))
				fichajesFechaEmpleado.add(documento);
		}
		
		return fichajesFechaEmpleado;
	}

	
	
	
	public static boolean existeFichajesPeriodo(String dni, String fecha1,String fecha2) {
			List<Date> periodo=calculoPeriodoFechas(fecha1,fecha2);
			
			boolean bool=false;
			Document documento = new Document();
			MongoCursor<Document> elementos = getFichajes().find().iterator();
	
			while(elementos.hasNext()) {
				documento = elementos.next();
				if((documento.get("dniEmpleado").toString()).equalsIgnoreCase(dni))
					if(comparacionFichajePeriodo(periodo,documento.get("fechaFichaje").toString()))	
					bool=true;
			}
			
			return bool;
		}
	
	
	public static boolean comparacionFichajePeriodo(List<Date> periodo, String fechaFichaje) {
		boolean bool=false;
		 
		Date fechafichaje=parserFechaFichaje(fechaFichaje);
		
		if(periodo.contains(fechafichaje)) {
			bool= true;
		}
		
		return bool;
		
	}
	
	public static Date parserFechaFichaje(String fecha) {
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
	
	public static List<Date> calculoPeriodoFechas(String fecha1, String fecha2) {
		
		Date startdate = parserFechaFichaje(fecha1);
		Date enddate = parserFechaFichaje(fecha2);
		
		List<Date> dates = new ArrayList<Date>();
	    Calendar calendar = new GregorianCalendar();
	    calendar.setTime(startdate);

	    while (calendar.getTime().before(enddate))
	    {
	        Date result = calendar.getTime();
	        dates.add(result);
	        calendar.add(Calendar.DATE, 1);
	    }
	    return dates;
		
	}
	
	//DAOFichaje
	
	//DAOIncidencia

	public static MongoCollection<Document> getIncidencias() {
		MongoCollection<Document> incidencias = getCollection("Incidencias");
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

	public void insert(Incidencia incidencia) {
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

	public static Date parserFechaIncidencia(String fecha) {
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


	public Incidencia buscarIncidenciaID(ObjectId id) {
		Incidencia inci=new Incidencia();

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
				inci.setEstado(documento.get("estado").toString());
				inci.setFechaCreacion(documento.get("fechaCreacion").toString());
				inci.setComentarioGestor(documento.get("comentarioGestor").toString());
			}

		}

		return inci;
	}

	public Incidencia resolverIncidencia(ObjectId id,String textoGestor) {
		Incidencia inci=new Incidencia();

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
				inci.setEstado("Resuelta");
				inci.setFechaCreacion(documento.get("fechaCreacion").toString());
				inci.setComentarioGestor(textoGestor);
			}

		}

		return inci;
	}

	public Incidencia denegarIncidencia(ObjectId id,String textoGestor) {
		Incidencia inci=new Incidencia();

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
				inci.setEstado("Denegada");
				inci.setFechaCreacion(documento.get("fechaCreacion").toString());
				inci.setComentarioGestor(textoGestor);
			}

		}

		return inci;
	}

	public void updateIncidencia(Incidencia incidencia,String modo) throws Exception {
		MongoCollection<Document> incidencias = getIncidencias();


		if(modo.equalsIgnoreCase("denegar") || modo.equalsIgnoreCase("resolver")) {


			Document criteria=new Document();

			criteria.put("_id", incidencia.get_id());

			Document changes=new Document();

			changes.put("estado", incidencia.getEstado());
			changes.put("comentarioGestor", incidencia.getComentarioGestor());
			Document doc = new Document();
			doc.put("$set", changes);

			updateDoc(incidencias, criteria, doc);
		}else if(modo.equalsIgnoreCase("modificar")){
			Document criteria=new Document();
			Document changes=new Document();
			Document doc = new Document();
			
			criteria.put("_id", incidencia.get_id());
			
			changes.put("categoria", incidencia.getCategoria());
			changes.put("fechaCreacion", incidencia.getFechaCreacion());
			changes.put("descripcion", incidencia.getDescripcion());
			
			doc.put("$set", changes);
			
			updateDoc(incidencias, criteria, doc);
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
	
	public Incidencia devolverIncidencia(ObjectId id, String categoria, String fecha, String descripcion) {
		Incidencia inci=new Incidencia();

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
				inci.setEstado("En espera");
				inci.setFechaCreacion(fecha);
			}

		}

		return inci;
	}
	public Incidencia devolverIncidencia(ObjectId id) {
		Incidencia inci=new Incidencia();

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
				inci.setEstado(documento.getString("estado").toString());
				inci.setFechaCreacion(documento.get("fechaCreacion").toString());
				inci.setComentarioGestor(documento.get("comentarioGestor").toString());
			}

		}

		return inci;
	}
	
	public void delete (Incidencia incidencia){
		Document bso = new Document();
		MongoCollection<Document> incidencias = getIncidencias();
		
		incidencias.deleteOne(new Document("_id", new ObjectId(incidencia.get_id().toString())));
	}
	//DAOIncidencia
	
	//UsuarioDaoImplement


	public boolean login(Usuario usuario) {

		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(email, new BsonString(usuario.getEmail()));
		criterio.append(password, new BsonString(Utilidades.encrypt(usuario.getPassword())));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuarioBson = resultado.first();
		if (usuarioBson==null) {
			return false;
		}
		return true;
	}


	//Inserta un nuevo usuario en la BBDD
	public void insert(Usuario usuario) throws Exception {
		if(!selectNombre(usuario)) {
			Document bso = new Document();
			Document bso2 = new Document();
			bso.append(name, new BsonString(usuario.getNombre()));
			bso.append(password, new BsonString(usuario.getPassword()));
			bso.append(email, new BsonString(usuario.getEmail()));
			bso.append(rol, new BsonString(usuario.getRol()));
			bso.append(dni, new BsonString(usuario.getDni()));
			
			bso2.append(dni, new BsonString(usuario.getDni()));
			bso2.append(password, new BsonString(usuario.getPassword()));
			MongoCollection<Document> usuarios = obtenerUsuarios();
			usuarios.insertOne(bso);
			MongoCollection<Document> contrasenas = getContrasenas();
			contrasenas.insertOne(bso2);
			
		}else
			throw new Exception("Cuenta existente");
	}
	
	public static MongoCollection<Document> getContrasenas() {
		MongoCollection<Document> incidencias = getCollection("Contrasenas");
		return incidencias;
	}
	//Devuelve un true si existe y false si no existe
	private boolean selectNombre(Usuario usuario) {
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(name, new BsonString(usuario.getNombre()));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuarioBson = resultado.first();
		if (usuarioBson == null) {
			return false;
		}
		return true;
	}
	
public Usuario selectNombre(String nombreParam) {
		
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(name, new BsonString(nombreParam));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuario = resultado.first();
		Usuario result;
		if (usuario==null) {
			return null;
		}
		else {
			
			String nombreUser = usuario.getString(name);
			String pwdUser = usuario.getString(password);
			String mailUser = usuario.getString(email);
			String rolUser = usuario.getString(rol);
			String dniUser = usuario.getString(dni);
			result = new Usuario(nombreUser, pwdUser, mailUser, rolUser,dniUser);
		}
		return result;
}

	public String devolverRol(Usuario usuario) {
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(dni, new BsonString(usuario.getDni()));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuariobso = resultado.first();
		if (usuario==null){
			return null;
		}else {
			String rolUser = usuariobso.getString(rol);
			usuario.setRol(rolUser);

		}
		return usuario.getRol();
	}
	public String devolverUser(Usuario usuario) {
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(email, new BsonString(usuario.getEmail()));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuariobso = resultado.first();
		if (usuario==null || usuariobso ==null){
			return null;
		}else {
			String nombreFinal= usuariobso.getString(name);
			usuario.setNombre(nombreFinal);
		}
		return usuario.getNombre();
	}
	public String devolverDni(Usuario usuario) {
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(email, new BsonString(usuario.getEmail()));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuariobso = resultado.first();
		if (usuario==null){
			return null;
		}else {
			String dniUser = usuariobso.getString(dni);
			usuario.setDni(dniUser);

		}
		return usuario.getDni();
		
	}

	//Obtener todos los usuarios
	private MongoCollection<Document> obtenerUsuarios() {
		MongoCollection<Document> usuarios = getCollection("Usuarios");
		return usuarios;
	}


	//Devuelve los usuarios que no son administradores
	public List<Usuario> list() {
		MongoCollection<Document> usuarios = obtenerUsuarios();
		FindIterable<Document> resultado=usuarios.find();
		String nombre;
		Document usuario;
		Iterator<Document> lista=resultado.iterator();
		List<Usuario> retorno=new ArrayList<Usuario>();
		while(lista.hasNext()) {
			usuario=lista.next();
			nombre=usuario.getString(name);
			//if(administradorDao.selectNombre(nombre)==null)retorno.add(new Usuario(nombre));
		}
		return retorno;
	}

	//Devuelve los usuarios que son gestores
public List<String> obtenerGestores() {
	Document documento = new Document();
	MongoCursor<Document> elementos = obtenerUsuarios().find().iterator();
	List<String> retorno=new ArrayList<String>();
	while(elementos.hasNext()) {
		documento = elementos.next();
		if(documento.get("rol").toString().equalsIgnoreCase("Gestor de incidencias")) {
			String mailGestor = documento.getString("email");
			retorno.add(mailGestor);
			
		}
	}
	return retorno;
}

	//Borrar usuario
	public void delete (Usuario usuario){
		//List<Usuario> todos=selectAll();
		Document bso = new Document();
		Document bso2 = new Document();
		bso.append(name, new BsonString(usuario.getNombre()));
		MongoCollection<Document> usuarios = obtenerUsuarios();
		usuarios.deleteOne(bso);
		
		bso2.append(dni, new BsonString(usuario.getDni()));
		MongoCollection<Document> contrasenas = getContrasenas();
		contrasenas.deleteMany(bso2);
	}

	//Devuelve una lista de todos los usuarios
	public List<Usuario> selectAll() {
		MongoCollection<Document> usuarios = obtenerUsuarios();
		FindIterable<Document> resultado=usuarios.find();
		String nombre;
		Document usuario;
		Iterator<Document> lista=resultado.iterator();
		List<Usuario> retorno=new ArrayList<Usuario>();
		while(lista.hasNext()) {
			usuario=lista.next();
			nombre=usuario.getString(name);
			retorno.add(new Usuario(nombre));
		}
		return retorno;
	}


	public String devolverMail(Usuario usuario) {
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(dni, new BsonString(usuario.getDni()));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuariobso = resultado.first();
		if (usuario==null){
			return null;
		}else {

			String mailFinal=usuariobso.getString(email);

			usuario.setRol(mailFinal);
		}
		return usuario.getRol();
	}




public void updatePwd(Usuario usuario) throws Exception{
	MongoCollection<Document> usuarios = obtenerUsuarios();
	MongoCollection<Document> contrasenas = getContrasenas();
	Document criterio = new Document();
	criterio.append(name, new BsonString(usuario.getNombre()));
	FindIterable<Document> resultado=usuarios.find(criterio);
	Document usuarioBso = resultado.first();
	Document bso2 = new Document();
	if (usuarioBso==null)
		throw new Exception("Fallo la actualizacion de los datos del usuario.");

	bso2.append(dni, new BsonString(usuario.getDni()));
	bso2.append(password, Utilidades.encrypt(usuario.getPassword()));
	contrasenas.insertOne(bso2);
	
	Document actualizacion= new Document("$set", new Document(password, new BsonString(Utilidades.encrypt(usuario.getPassword()))));
	usuarios.findOneAndUpdate(usuarioBso, actualizacion);
}

	public void updateRol(Usuario usuario, String rolNuevo) throws Exception{
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(dni, new BsonString(usuario.getDni()));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuarioBso = resultado.first();
		if (usuarioBso==null)
			throw new Exception("Fallo la actualizacion de los datos del usuario.");

		Document actualizacion= new Document("$set", new Document(rol, new BsonString(rolNuevo)));
		usuarios.findOneAndUpdate(usuarioBso, actualizacion);
	}
	public void updateNombre(Usuario usuario, String nombreNuevo) throws Exception{
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(email, new BsonString(usuario.getEmail()));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuarioBso = resultado.first();
		if (usuarioBso==null)
			throw new Exception("Fallo la actualizacion de los datos del usuario.");

		Document actualizacion= new Document("$set", new Document(name, new BsonString(nombreNuevo)));
		usuarios.findOneAndUpdate(usuarioBso, actualizacion);
		
	}
	public void updateEmail(Usuario usuario, String emailNuevo) throws Exception{
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append(dni, new BsonString(usuario.getDni()));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuarioBso = resultado.first();
		if (usuarioBso==null)
			throw new Exception("Fallo la actualizacion de los datos del usuario.");

		Document actualizacion= new Document("$set", new Document(email, new BsonString(emailNuevo)));
		usuarios.findOneAndUpdate(usuarioBso, actualizacion);
		
	}

	//UsuarioDaoImplement
}
