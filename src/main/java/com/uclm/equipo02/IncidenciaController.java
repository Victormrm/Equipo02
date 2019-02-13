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
import com.uclm.equipo02.modelo.Modelo;
import com.uclm.equipo02.persistencia.DAOFichaje;
import com.uclm.equipo02.persistencia.DAOIncidencia;
import com.uclm.equipo02.persistencia.UsuarioDaoImplement;


@Controller
public class IncidenciaController {
	
	private final String fichajes = "fichajes";
	private final String interfazAdministrador="interfazAdministrador";
	private final String interfazGestor="interfazGestor";
	private final String usuario_conect = "usuarioConectado";
	DAOIncidencia incidenciaDao = new DAOIncidencia();
	UsuarioDaoImplement userDao = new UsuarioDaoImplement();
	
	
	@RequestMapping(value = "/crearIncidenciaGeneral", method = RequestMethod.POST)
	public String crearIncidenciaGeneral(HttpServletRequest request, Model model) throws Exception {
		String returned="";
		Modelo usuario;
	    usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
	   
		String nombreUsuario = usuario.getNombre();
		String dniUsuario = usuario.getDni();
		String categoria = request.getParameter("listaTiposIncidencia");
		String fechaCreacion =(java.time.LocalDate.now()).toString();
		String descripcion = request.getParameter("textoIncidencia");
		String estado = "En espera";
		String comentarioGestor = "";
		
		Modelo incidencia = new Modelo(nombreUsuario, dniUsuario, categoria, descripcion, estado, 
				fechaCreacion, comentarioGestor);
		
		try {
			incidenciaDao.insert(incidencia);
		} catch (Exception e) {

		}
		
		String  asunto = "Nueva incidencia";
		String cuerpo = "Tiene una nueva incidencia por resolver\n"
				+ "	   Usuario: "+ nombreUsuario+"\n"
				+ "    Tipo: " + categoria+"\n"
				+ "    Fecha: " + fechaCreacion+"\n\n\n"
				+ "                 InTime Corporation";
		MailSender mailSender = new MailSender();

		List<String> gestores = userDao.obtenerGestores();
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
		
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		
		
		Modelo inci = incidenciaDao.buscarIncidenciaID(id);
		model.addAttribute("seleccionadaInci", inci); 
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =incidenciaDao.getIncidenciasGestor();
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
		
		return "resolverIncidencia";

	
	}
	
	@RequestMapping(value = "resolverIncidencia", method = RequestMethod.GET)
	public String resolverIncidencia(HttpServletRequest request, Model model) throws Exception {
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		String texto=request.getParameter("textoGestor");
		String modo="resolver";
		
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		Modelo resuelta=incidenciaDao.resolverIncidencia(id,texto);
		
		incidenciaDao.updateIncidencia(resuelta,modo);
		
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =incidenciaDao.getIncidenciasGestor();
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
	
		return "resolverIncidencia";
	}
	
	@RequestMapping(value = "denegarIncidencia", method = RequestMethod.GET)
	public String denegarIncidencia(HttpServletRequest request, Model model) throws Exception {
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		String texto=request.getParameter("textoGestor");
		String modo="denegar";
		
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		Modelo denegada=incidenciaDao.denegarIncidencia(id,texto);
		incidenciaDao.updateIncidencia(denegada,modo);

		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =incidenciaDao.getIncidenciasGestor();
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);

		return "resolverIncidencia";
	}
	
	@RequestMapping(value = "listarIncidenciasGestor", method = RequestMethod.GET)
	public String listarIncidenciasGestor(HttpServletRequest request, Model model) {
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);

		String dni = usuario.getDni();
		String fecha1= request.getParameter("fecha1");
		String fecha2= request.getParameter("fecha2");

		if(!incidenciaDao.existeIncidenciasEspera()) {
			model.addAttribute("nullIncidencia","No existe ning&uacutena incidencia en estado de espera");
			return "resolverIncidencia";
		}else {
			List<Document> listaIncidenciasGestor =incidenciaDao.getIncidenciasGestor();
			model.addAttribute("listaIncidencias", listaIncidenciasGestor);
			return "resolverIncidencia";
		}
	}
	@RequestMapping(value = "/listarIncidencias", method = RequestMethod.GET)
	public String listarIncidencia(HttpServletRequest request, Model model) {
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		request.setAttribute("nombreUser", usuario.getNombre());
		request.setAttribute("dniUser", usuario.getDni());
		String dni = usuario.getDni();
		
		if(!incidenciaDao.existeIncidencias(dni)) {
			model.addAttribute("nullIncidencia","No existe ning&uacutena incidencia en estado de espera");
			return "modificarIncidencia";
		}else {
			List<Document> listaIncidencias =incidenciaDao.getIncidencias(dni);
			model.addAttribute("listaIncidencias", listaIncidencias);
			System.out.println("LISTA INCIDENCIAS"+listaIncidencias.toString());
			return "modificarIncidencia";
		}
	}
	@RequestMapping(value = "/listarIncidenciasEliminar", method = RequestMethod.GET)
	public String listarIncidenciaEliminar(HttpServletRequest request, Model model) {
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		request.setAttribute("nombreUser", usuario.getNombre());
		request.setAttribute("dniUser", usuario.getDni());
		String dni = usuario.getDni();
		
		if(!incidenciaDao.existeIncidencias(dni)) {
			model.addAttribute("nullIncidencia","No existe ning&uacutena incidencia en estado de espera");
			List<Document> listaIncidencias =incidenciaDao.getIncidencias(dni);
			model.addAttribute("listaIncidencias", listaIncidencias);
			return "eliminarIncidencia";
		}else {
			List<Document> listaIncidencias =incidenciaDao.getIncidencias(dni);
			model.addAttribute("listaIncidencias", listaIncidencias);
			System.out.println("LISTA INCIDENCIAS"+listaIncidencias.toString());
			
			return "eliminarIncidencia";
		}
	}
	@RequestMapping(value = "seleccionarIncidenciaUsuario", method = RequestMethod.GET)
	public String seleccionarIncidenciaUsuario(HttpServletRequest request, Model model) {
		
		String idIncidencia=request.getParameter("idI");
		ObjectId id=new ObjectId(idIncidencia);
		
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		
		
		Modelo inci = incidenciaDao.buscarIncidenciaID(id);
		model.addAttribute("seleccionadaInci", inci); 
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =incidenciaDao.getIncidencias(usuario.getDni());
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
		
		return "modificarIncidencia";	
	}
	@RequestMapping(value = "seleccionarIncidenciaEliminar", method = RequestMethod.GET)
	public String seleccionarIncidenciaEliminar(HttpServletRequest request, Model model) {
		
		String idIncidencia=request.getParameter("idI");
		ObjectId id=new ObjectId(idIncidencia);
		
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		
		
		Modelo inci = incidenciaDao.buscarIncidenciaID(id);
		model.addAttribute("seleccionadaInci", inci); 
		//Creacion de lista de incidencias de nuevo
		List<Document> listaIncidenciasGestor =incidenciaDao.getIncidencias(usuario.getDni());
		model.addAttribute("listaIncidencias", listaIncidenciasGestor);
		
		return "eliminarIncidencia";	
	}
	@RequestMapping(value = "modificarIncidenciaUser", method = RequestMethod.GET)
	public String modificarIncidencia(HttpServletRequest request, Model model) {
		String modo="modificar";
		String returned="";
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		String categoria = request.getParameter("listaTiposIncidencia");
		String fecha = request.getParameter("txtFecha");
		String descripcion = request.getParameter("textoIncidencia");
		
		if(categoria.equalsIgnoreCase("")||fecha.equalsIgnoreCase("")||descripcion.equalsIgnoreCase("")) {
			model.addAttribute("alerta", "Por favor rellene los campos");
			return "modificarIncidencia";
		}
		
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		Modelo incidencia= incidenciaDao.devolverIncidencia(id, categoria,fecha,descripcion);
		try {
			incidenciaDao.updateIncidencia(incidencia,modo);
		}catch(Exception e) {
			
		}
		List<Document> listaIncidencias =incidenciaDao.getIncidencias(usuario.getDni());
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
		Modelo usuario;
		usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		String idIncidencia=request.getParameter("idSeleccionada");
		ObjectId id=new ObjectId(idIncidencia);
		
		System.out.println("id object id " + id);
		Modelo incidencia= incidenciaDao.devolverIncidencia(id);
		try {
			incidenciaDao.delete(incidencia);
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
