package com.uclm.equipo02;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bson.BsonString;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.uclm.equipo02.Auxiliar.Utilidades;
import com.uclm.equipo02.modelo.Usuario;
import com.uclm.equipo02.persistencia.MongoBroker;

@Controller

public class HomeController {

	
	private final String usuario_login = "login";
	private final String usuario_conect = "usuarioConectado";
	private final String alert = "alerta";
	private final String usuarioServ = "usuario";
	private final String name = "nombre";
	private final String password = "pwd";
	private final String email = "email";
	private final String rol = "rol";
	private final String dni = "dni";
	
	private final String welcome = "welcome";


	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);

		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);

		String formattedDate = dateFormat.format(date);

		model.addAttribute("serverTime", formattedDate );

		return "home";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String iniciarSesion(HttpServletRequest request, Model model) throws Exception {
		//String cadenaUrl = usuarioServ;
		String email = request.getParameter("txtUsuarioEmail");
		String password = request.getParameter("txtUsuarioPassword");
		if (email.equals("") || password.equals("")) {
			model.addAttribute(alert, "Por favor rellene los campos");
			return usuario_login;
		}
		Usuario usuario = new Usuario();
		usuario.setEmail(email);
		usuario.setPassword(password);
		try {
		String nombre =  devolverUser(usuario);
		usuario.setNombre(nombre);
		String dni = devolverDni(usuario);
		usuario.setDni(dni);
		}catch(Exception e) {
			
		}
		if (login(usuario) && request.getSession().getAttribute(usuario_conect) == null){
			usuario.setRol(devolverRol(usuario));

			if(usuario.getRol().equalsIgnoreCase("empleado")) {
				request.getSession().setAttribute(usuario_conect, usuario);
				request.setAttribute("nombreUser", usuario);
				request.setAttribute("mailUser", email);
				request.setAttribute("dniUser", dni);
				return "fichajes";
			}else if (usuario.getRol().equalsIgnoreCase("administrador")){
				request.getSession().setAttribute(usuario_conect, usuario);
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", email);
				request.setAttribute("dniUser", dni);
				return "interfazAdministrador";
			}else if (usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				request.getSession().setAttribute(usuario_conect, usuario);
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", email);
				request.setAttribute("dniUser", dni);
				return "interfazGestor";
			}

		}else{
			model.addAttribute(alert, "Usuario y/o clave incorrectos");
			return usuario_login;
		}
		return usuario_login;
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

	public ModelAndView cambiarVista(String nombreVista) {
		ModelAndView vista = new ModelAndView(nombreVista);
		return vista;
	}
	private MongoCollection<Document> obtenerUsuarios() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> usuarios = broker.getCollection("Usuarios");
		return usuarios;
	}


	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ModelAndView cerrarSesion(HttpServletRequest request) throws Exception {
		HttpSession sesion = request.getSession();

		System.out.println("Sesion antes de invalidar: " + sesion);
		sesion.invalidate();
		System.out.println("Invalidamos la sesion: " + sesion);

		return cambiarVista(usuario_login);
	}
}
