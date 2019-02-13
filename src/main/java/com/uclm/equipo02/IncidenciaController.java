package com.uclm.equipo02;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mongodb.client.*;
import com.uclm.equipo02.mail.MailSender;
import com.uclm.equipo02.modelo.Incidencia;
import com.uclm.equipo02.modelo.Usuario;
import com.uclm.equipo02.persistencia.MongoBroker;


@Controller
public class IncidenciaController {
	
	private final String fichajes = "fichajes";
	private final String interfazAdministrador="interfazAdministrador";
	private final String interfazGestor="interfazGestor";
	private final String usuario_conect = "usuarioConectado";
	
	
	@RequestMapping(value = "/crearIncidenciaGeneral", method = RequestMethod.POST)
	public String crearIncidenciaGeneral(HttpServletRequest request, Model model) throws Exception {
		String returned="";
		Usuario usuario;
	    usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
	   
		String nombreUsuario = usuario.getNombre();
		String dniUsuario = usuario.getDni();
		String categoria = request.getParameter("listaTiposIncidencia");
		String fechaCreacion =(java.time.LocalDate.now()).toString();
		String descripcion = request.getParameter("textoIncidencia");
		String estado = "En espera";
		String comentarioGestor = "";
		
		Incidencia incidencia = new Incidencia(nombreUsuario, dniUsuario, categoria, descripcion, estado, 
				fechaCreacion, comentarioGestor);
		
		try {
			insert(incidencia);
		} catch (Exception e) {

		}
		
		String  asunto = "Nueva incidencia";
		String cuerpo = "Tiene una nueva incidencia por resolver\n"
				+ "	   Usuario: "+ nombreUsuario+"\n"
				+ "    Tipo: " + categoria+"\n"
				+ "    Fecha: " + fechaCreacion+"\n\n\n"
				+ "                 InTime Corporation";
		MailSender mailSender = new MailSender();

		List<String> gestores =obtenerGestores();
		for (String email : gestores) {
			mailSender.enviarConGMail(email, asunto, cuerpo);
		}
		if(usuario.getRol().equalsIgnoreCase("Empleado")) {
			returned=fichajes;
		}else if(usuario.getRol().equalsIgnoreCase("administrador")){
			returned=interfazAdministrador;

		}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
			returned=interfazGestor;
		}
		return returned;
	}
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
	private MongoCollection<Document> obtenerUsuarios() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> usuarios = broker.getCollection("Usuarios");
		return usuarios;
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
	public static MongoCollection<Document> getIncidencias() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> incidencias = broker.getCollection("Incidencias");
		return incidencias;
	}

	


	@RequestMapping(value = "seleccionarIncidencia", method = RequestMethod.GET)
	public String seleccionarIncidencia(HttpServletRequest request, Model model) {
		
		String idIncidencia=request.getParameter("idI");
		ObjectId id=new ObjectId(idIncidencia);
		
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		
		
		Incidencia inci = buscarIncidenciaID(id);
		model.addAttribute("seleccionadaInci", inci); 
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =getIncidenciasGestor();
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
		
		return "resolverIncidencia";

	
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
	
	@RequestMapping(value = "resolverIncidencia", method = RequestMethod.GET)
	public String resolverIncidencia(HttpServletRequest request, Model model) throws Exception {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		String texto=request.getParameter("textoGestor");
		String modo="resolver";
		
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		Incidencia resuelta=resolverIncidencia(id,texto);
		
		updateIncidencia(resuelta,modo);
		
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =getIncidenciasGestor();
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
	
		return "resolverIncidencia";
	}
	
	public void updateIncidencia(Incidencia incidencia,String modo) throws Exception {
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

	
	@RequestMapping(value = "denegarIncidencia", method = RequestMethod.GET)
	public String denegarIncidencia(HttpServletRequest request, Model model) throws Exception {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		String texto=request.getParameter("textoGestor");
		String modo="denegar";
		
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		Incidencia denegada=denegarIncidencia(id,texto);
		updateIncidencia(denegada,modo);

		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =getIncidenciasGestor();
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);

		return "resolverIncidencia";
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
	
	@RequestMapping(value = "listarIncidenciasGestor", method = RequestMethod.GET)
	public String listarIncidenciasGestor(HttpServletRequest request, Model model) {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);

		String dni = usuario.getDni();
		String fecha1= request.getParameter("fecha1");
		String fecha2= request.getParameter("fecha2");

		if(!existeIncidenciasEspera()) {
			model.addAttribute("nullIncidencia","No existe ning&uacutena incidencia en estado de espera");
			return "resolverIncidencia";
		}else {
			List<Document> listaIncidenciasGestor =getIncidenciasGestor();
			model.addAttribute("listaIncidencias", listaIncidenciasGestor);
			return "resolverIncidencia";
		}
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
	@RequestMapping(value = "/listarIncidencias", method = RequestMethod.GET)
	public String listarIncidencia(HttpServletRequest request, Model model) {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		request.setAttribute("nombreUser", usuario.getNombre());
		request.setAttribute("dniUser", usuario.getDni());
		String dni = usuario.getDni();
		
		if(!existeIncidencias(dni)) {
			model.addAttribute("nullIncidencia","No existe ning&uacutena incidencia en estado de espera");
			return "modificarIncidencia";
		}else {
			List<Document> listaIncidencias =getIncidencias(dni);
			model.addAttribute("listaIncidencias", listaIncidencias);
			System.out.println("LISTA INCIDENCIAS"+listaIncidencias.toString());
			return "modificarIncidencia";
		}
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
	@RequestMapping(value = "/listarIncidenciasEliminar", method = RequestMethod.GET)
	public String listarIncidenciaEliminar(HttpServletRequest request, Model model) {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		request.setAttribute("nombreUser", usuario.getNombre());
		request.setAttribute("dniUser", usuario.getDni());
		String dni = usuario.getDni();
		
		if(!existeIncidencias(dni)) {
			model.addAttribute("nullIncidencia","No existe ning&uacutena incidencia en estado de espera");
			List<Document> listaIncidencias =getIncidencias(dni);
			model.addAttribute("listaIncidencias", listaIncidencias);
			return "eliminarIncidencia";
		}else {
			List<Document> listaIncidencias =getIncidencias(dni);
			model.addAttribute("listaIncidencias", listaIncidencias);
			System.out.println("LISTA INCIDENCIAS"+listaIncidencias.toString());
			
			return "eliminarIncidencia";
		}
	}
	@RequestMapping(value = "seleccionarIncidenciaUsuario", method = RequestMethod.GET)
	public String seleccionarIncidenciaUsuario(HttpServletRequest request, Model model) {
		
		String idIncidencia=request.getParameter("idI");
		ObjectId id=new ObjectId(idIncidencia);
		
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		
		
		Incidencia inci =buscarIncidenciaID(id);
		model.addAttribute("seleccionadaInci", inci); 
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =getIncidencias(usuario.getDni());
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
		
		return "modificarIncidencia";	
	}
	@RequestMapping(value = "seleccionarIncidenciaEliminar", method = RequestMethod.GET)
	public String seleccionarIncidenciaEliminar(HttpServletRequest request, Model model) {
		
		String idIncidencia=request.getParameter("idI");
		ObjectId id=new ObjectId(idIncidencia);
		
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		
		
		Incidencia inci = buscarIncidenciaID(id);
		model.addAttribute("seleccionadaInci", inci); 
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =getIncidencias(usuario.getDni());
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
		
		return "eliminarIncidencia";	
	}
	@RequestMapping(value = "modificarIncidenciaUser", method = RequestMethod.GET)
	public String modificarIncidencia(HttpServletRequest request, Model model) {
		String modo="modificar";
		String returned="";
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		String categoria = request.getParameter("listaTiposIncidencia");
		String fecha = request.getParameter("txtFecha");
		String descripcion = request.getParameter("textoIncidencia");
		
		if(categoria.equalsIgnoreCase("")||fecha.equalsIgnoreCase("")||descripcion.equalsIgnoreCase("")) {
			model.addAttribute("alerta", "Por favor rellene los campos");
			return "modificarIncidencia";
		}
		
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		Incidencia incidencia= devolverIncidencia(id, categoria,fecha,descripcion);
		try {
			updateIncidencia(incidencia,modo);
		}catch(Exception e) {
			
		}
		List<Document> listaIncidencias =getIncidencias(usuario.getDni());
		model.addAttribute("listaIncidencias", listaIncidencias);
		
		if(usuario.getRol().equalsIgnoreCase("Empleado")) {
			returned=fichajes;
		}else if(usuario.getRol().equalsIgnoreCase("administrador")){
			returned=interfazAdministrador;

		}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
			returned=interfazGestor;
		}
		
		return returned;	
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
	@RequestMapping(value = "eliminarIncidenciaUser", method = RequestMethod.GET)
	public String eliminarIncidencia(HttpServletRequest request, Model model) {
		String returned="";
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		System.out.println("id object id " + id);
		Incidencia incidencia= devolverIncidencia(id);
		try {
			delete(incidencia);
		}catch(Exception e) {
			
		}
		if(usuario.getRol().equalsIgnoreCase("Empleado")) {
			returned=fichajes;
		}else if(usuario.getRol().equalsIgnoreCase("administrador")){
			returned=interfazAdministrador;

		}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
			returned=interfazGestor;
		}
		return returned;
	}
	public void delete (Incidencia incidencia){
		Document bso = new Document();
		MongoCollection<Document> incidencias = getIncidencias();
		
		incidencias.deleteOne(new Document("_id", new ObjectId(incidencia.get_id().toString())));
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
	
	@RequestMapping(value = "/REEliminarIncidencia", method = RequestMethod.GET)
	public ModelAndView REEliminarIncidencia() {
		return new ModelAndView("eliminarIncidencia");
	}
	@RequestMapping(value = "/REModificarIncidencia", method = RequestMethod.GET)
	public ModelAndView REModificarIncidencia() {
		return new ModelAndView("modificarIncidencia");
	}
	
	@RequestMapping(value = "/RECrearIncidencia", method = RequestMethod.GET)
	public ModelAndView irIncidencias() {
		return new ModelAndView("interfazCrearIncidencia");
	}
	
	@RequestMapping(value = "/REResolverIncidencia", method = RequestMethod.GET)
	public ModelAndView REResolverIncidencia() {
		return new ModelAndView("resolverIncidencia");
	}
}
