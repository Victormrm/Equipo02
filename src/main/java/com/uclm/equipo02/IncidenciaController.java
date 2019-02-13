package com.uclm.equipo02;

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
import com.uclm.equipo02.persistencia.Persistencia;


@Controller
public class IncidenciaController {
	
	private final String fichajes = "fichajes";
	private final String interfazAdministrador="interfazAdministrador";
	private final String interfazGestor="interfazGestor";
	private final String usuario_conect = "usuarioConectado";
	Persistencia persis = new Persistencia();
	
	
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
			persis.insert(incidencia);
		} catch (Exception e) {

		}
		
		String  asunto = "Nueva incidencia";
		String cuerpo = "Tiene una nueva incidencia por resolver\n"
				+ "	   Usuario: "+ nombreUsuario+"\n"
				+ "    Tipo: " + categoria+"\n"
				+ "    Fecha: " + fechaCreacion+"\n\n\n"
				+ "                 InTime Corporation";
		MailSender mailSender = new MailSender();

		List<String> gestores = persis.obtenerGestores();
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


	@RequestMapping(value = "seleccionarIncidencia", method = RequestMethod.GET)
	public String seleccionarIncidencia(HttpServletRequest request, Model model) {
		
		String idIncidencia=request.getParameter("idI");
		ObjectId id=new ObjectId(idIncidencia);
		
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		
		
		Incidencia inci = persis.buscarIncidenciaID(id);
		model.addAttribute("seleccionadaInci", inci); 
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =persis.getIncidenciasGestor();
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
		
		return "resolverIncidencia";

	
	}
	
	@RequestMapping(value = "resolverIncidencia", method = RequestMethod.GET)
	public String resolverIncidencia(HttpServletRequest request, Model model) throws Exception {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		String texto=request.getParameter("textoGestor");
		String modo="resolver";
		
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		Incidencia resuelta=persis.resolverIncidencia(id,texto);
		
		persis.updateIncidencia(resuelta,modo);
		
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor = persis.getIncidenciasGestor();
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
	
		return "resolverIncidencia";
	}
	
	@RequestMapping(value = "denegarIncidencia", method = RequestMethod.GET)
	public String denegarIncidencia(HttpServletRequest request, Model model) throws Exception {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		String texto=request.getParameter("textoGestor");
		String modo="denegar";
		
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		Incidencia denegada=persis.denegarIncidencia(id,texto);
		persis.updateIncidencia(denegada,modo);

		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =persis.getIncidenciasGestor();
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);

		return "resolverIncidencia";
	}
	
	@RequestMapping(value = "listarIncidenciasGestor", method = RequestMethod.GET)
	public String listarIncidenciasGestor(HttpServletRequest request, Model model) {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);

		String dni = usuario.getDni();
		String fecha1= request.getParameter("fecha1");
		String fecha2= request.getParameter("fecha2");

		if(!persis.existeIncidenciasEspera()) {
			model.addAttribute("nullIncidencia","No existe ning&uacutena incidencia en estado de espera");
			return "resolverIncidencia";
		}else {
			List<Document> listaIncidenciasGestor =persis.getIncidenciasGestor();
			model.addAttribute("listaIncidencias", listaIncidenciasGestor);
			return "resolverIncidencia";
		}
	}
	@RequestMapping(value = "/listarIncidencias", method = RequestMethod.GET)
	public String listarIncidencia(HttpServletRequest request, Model model) {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		request.setAttribute("nombreUser", usuario.getNombre());
		request.setAttribute("dniUser", usuario.getDni());
		String dni = usuario.getDni();
		
		if(!persis.existeIncidencias(dni)) {
			model.addAttribute("nullIncidencia","No existe ning&uacutena incidencia en estado de espera");
			return "modificarIncidencia";
		}else {
			List<Document> listaIncidencias =persis.getIncidencias(dni);
			model.addAttribute("listaIncidencias", listaIncidencias);
			System.out.println("LISTA INCIDENCIAS"+listaIncidencias.toString());
			return "modificarIncidencia";
		}
	}
	@RequestMapping(value = "/listarIncidenciasEliminar", method = RequestMethod.GET)
	public String listarIncidenciaEliminar(HttpServletRequest request, Model model) {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		request.setAttribute("nombreUser", usuario.getNombre());
		request.setAttribute("dniUser", usuario.getDni());
		String dni = usuario.getDni();
		
		if(!persis.existeIncidencias(dni)) {
			model.addAttribute("nullIncidencia","No existe ning&uacutena incidencia en estado de espera");
			List<Document> listaIncidencias =persis.getIncidencias(dni);
			model.addAttribute("listaIncidencias", listaIncidencias);
			return "eliminarIncidencia";
		}else {
			List<Document> listaIncidencias =persis.getIncidencias(dni);
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
		
		
		Incidencia inci = persis.buscarIncidenciaID(id);
		model.addAttribute("seleccionadaInci", inci); 
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =persis.getIncidencias(usuario.getDni());
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
		
		return "modificarIncidencia";	
	}
	@RequestMapping(value = "seleccionarIncidenciaEliminar", method = RequestMethod.GET)
	public String seleccionarIncidenciaEliminar(HttpServletRequest request, Model model) {
		
		String idIncidencia=request.getParameter("idI");
		ObjectId id=new ObjectId(idIncidencia);
		
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		
		
		Incidencia inci = persis.buscarIncidenciaID(id);
		model.addAttribute("seleccionadaInci", inci); 
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =persis.getIncidencias(usuario.getDni());
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
		
		Incidencia incidencia= persis.devolverIncidencia(id, categoria,fecha,descripcion);
		try {
			persis.updateIncidencia(incidencia,modo);
		}catch(Exception e) {
			
		}
		List<Document> listaIncidencias =persis.getIncidencias(usuario.getDni());
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
	@RequestMapping(value = "eliminarIncidenciaUser", method = RequestMethod.GET)
	public String eliminarIncidencia(HttpServletRequest request, Model model) {
		String returned="";
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		System.out.println("id object id " + id);
		Incidencia incidencia= persis.devolverIncidencia(id);
		try {
			persis.delete(incidencia);
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
