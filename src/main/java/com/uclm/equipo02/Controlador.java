package com.uclm.equipo02;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.uclm.equipo02.Auxiliar.Utilidades;
import com.uclm.equipo02.mail.MailSender;
import com.uclm.equipo02.modelo.Fichaje;
import com.uclm.equipo02.modelo.Incidencia;
import com.uclm.equipo02.modelo.Usuario;
import com.uclm.equipo02.persistencia.DAOAdmin;
import com.uclm.equipo02.persistencia.DAOFichaje;
import com.uclm.equipo02.persistencia.DAOIncidencia;
import com.uclm.equipo02.persistencia.UsuarioDaoImplement;

@Controller
public class Controlador {

	
	//ADMIN
	//private final String usuario_login = "login";
		UsuarioDaoImplement userDao = new UsuarioDaoImplement();
		Usuario user = new Usuario();
		private final String alert = "alerta";
		private final String usuario_conect = "usuarioConectado";
		private final String adminUpdatePwd = "adminUpdatePwd";
		private DAOAdmin daoadmin=new DAOAdmin();

		//private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

		@RequestMapping(value = "/crearUsuario", method = RequestMethod.POST)
		public String crearUsuario(HttpServletRequest request, Model model) throws Exception {

			String mail = request.getParameter("txtUsuarioEmail");
			String nombre = request.getParameter("txtUsuarioNombre");
			String rol = request.getParameter("listaRoles");
			String dni = request.getParameter("txtDni");
			String pass = Utilidades.passRandom();
			if (mail.equals("") || nombre.equals("") || rol.equals("")||dni.equals("")) {
				model.addAttribute("alerta", "Por favor rellene los campos");
				return "interfazCrearUsuario";
			}
			//UsuarioDaoImplement userDao = new UsuarioDaoImplement();
			Usuario user = new Usuario();
			user.setNombre(nombre);
			user.setPassword(Utilidades.encrypt(pass));
			user.setEmail(mail);
			user.setRol(rol);
			user.setDni(dni);

			
			try {
				userDao.insert(user);
			} catch (Exception e) {
				
			}

			String asunto = "Password por defecto";
			String cuerpo = "Hola " + nombre + "! \nLa password por defecto es la siguiente:\n" + pass
					+"\n\nUn Saludo\nInTime Corporation";

			MailSender mailSender = new MailSender();
			mailSender.enviarConGMail(mail, asunto, cuerpo);
			
			model.addAttribute("alerta1", "Se ha creado un usuario satisfactoriamente");
			return "interfazCrearUsuario";
		}

		@RequestMapping(value = "/eliminarUsuario", method = RequestMethod.POST)
		public String eliminarUsuario(HttpServletRequest request, Model model) throws Exception {

			String dni = request.getParameter("txtDni");

			if (dni.equals("")){
				model.addAttribute(alert, "Por favor rellene los campos");
				return "interfazEliminarUsuario";
			}else {
				Usuario user = new Usuario();
				user.setDni(dni);
				try {
					user.setEmail(userDao.devolverMail(user));
					user.setNombre(userDao.devolverUser(user));
					userDao.delete(user);
				}catch(Exception e) {

				}

				return "interfazAdministrador";
			}
		}
		
		@RequestMapping(value = "/buscarUsuarioPorDni", method = RequestMethod.GET)
		public String buscarUsuario(HttpServletRequest request, Model model) throws Exception {

			String dni = request.getParameter("txtDniBusqueda");
			
			if(!daoadmin.existeUser(dni)) {
				model.addAttribute("alertaUsuarioNull","El usuario buscado no existe");
				return "modificarUsuario";
			}else {

			user.setDni(dni);
			
			user.setEmail(userDao.devolverMail(user));
			user.setNombre(userDao.devolverUser(user));
			user.setRol(userDao.devolverRol(user));
			
			
			HttpSession session = request.getSession();
			request.setAttribute("nombreUser", user.getNombre());
			request.setAttribute("dniUser", user.getDni());

			model.addAttribute("RolUsuario", user.getRol());
			model.addAttribute("EmailUsuario", user.getEmail());
			
			return "modificarUsuario";
			}

			

		}
		
		
		@RequestMapping(value = "/modificarUser", method = RequestMethod.GET)
		public String modificarUser(HttpServletRequest request, Model model) throws Exception {

			String rol = request.getParameter("listaRoles");
			String email = request.getParameter("txtUsuarioEmail");
			try {
				
				userDao.updateEmail(user, email);
				userDao.updateRol(user, rol);
			}catch(Exception e) {

			}

			return "interfazAdministrador";

		}
		
		
		@RequestMapping(value = "/adminModificarPwd", method = RequestMethod.POST)
		public String adminModificarPwd(HttpServletRequest request, Model model) throws Exception {
			Usuario usuarioLigero = (Usuario) request.getSession().getAttribute(usuario_conect);
			
			String dniUsuario = request.getParameter("dniUsuario");
			
			
			String pwdNueva = request.getParameter("contrasenaNueva");
			String pwdNueva2 = request.getParameter("contrasenaNueva2");
			
			
			Usuario usuarioBusqueda= new Usuario();
			
			
			
			if(!daoadmin.existeUser(dniUsuario)) {
				model.addAttribute("alertaUsuarioNull","El usuario buscado no existe");
				return adminUpdatePwd;
				
			}else {
			
			usuarioBusqueda = daoadmin.buscarUsuarioEmail(dniUsuario);
			
			
			String nombre = userDao.devolverUser(usuarioBusqueda);

			Usuario usuario = userDao.selectNombre(nombre);
			String actualPwd = usuario.getPassword();
			String dni = usuario.getDni();
			usuario.setEmail(usuarioBusqueda.getEmail());
			usuario.setPassword(pwdNueva);
			
			
			if (usuario == null || !(pwdNueva.equals(pwdNueva2))) {
				request.setAttribute("nombreUserBusqueda", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				model.addAttribute(alert, "Datos incorrectos");
				return adminUpdatePwd;
			}
			try {
		
			} catch (Exception e) {
				model.addAttribute(alert, e.getMessage());
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				return adminUpdatePwd;
			}
			
			if(Utilidades.comprobarPwd(dni, actualPwd, pwdNueva)==false){
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				model.addAttribute("alertaPWDRepe","Password anteriormente utilizada");
				return adminUpdatePwd;
			}else if(!Utilidades.seguridadPassword(pwdNueva)) {
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				model.addAttribute("alertaPWDinsegura","Password poco segura (minimo 8 caracteres, con numeros y letras)");
				return adminUpdatePwd;
			}else {
				userDao.updatePwd(usuario);
				HttpSession session = request.getSession();
				request.setAttribute("usuarioNombre", usuario.getNombre());
				request.setAttribute("usuarioEmail", usuario.getEmail());
				session.setAttribute("alertaCambio", "La contrase&ntilde;a ha sido cambiada satisfactoriamente");
				return adminUpdatePwd;
			}
			
			}
			
		}
		
		@RequestMapping(value = "/adminUpdatePwd", method = RequestMethod.GET)
		public ModelAndView interfazFichajesAdmin() {
			return new ModelAndView("adminUpdatePwd");
			
		}
		@RequestMapping(value = "/REfichajesAdminNav", method = RequestMethod.GET)
		public ModelAndView fichajesAdminNav() {
			return new ModelAndView("interfazAdministrador");
			
		}

		@RequestMapping(value = "/interfazCrearUsuario", method = RequestMethod.GET)
		public ModelAndView interfazCrearUsuario() {
			return new ModelAndView("interfazCrearUsuario");
		}
		@RequestMapping(value = "/interfazEliminarUsuario", method = RequestMethod.GET)
		public ModelAndView interfazEliminarUsuario() {
			return new ModelAndView("interfazEliminarUsuario");
		}
		@RequestMapping(value = "/modificarUsuario", method = RequestMethod.GET)
		public ModelAndView modificarUsuario() {
			return new ModelAndView("modificarUsuario");
		}

		//FICHAJE
		
		DAOFichaje fichajedao = new DAOFichaje();


		private final String usuario_conectFichajes = "usuarioConectado";
		private final String errorMessageAbrir = "errorMessageAbrir";
		private final String errorMessageCerrar = "errorMessageCerrar";
		private final String fichajes = "fichajes";
		private final String interfazAdministradorFichaje="interfazAdministrador";
		private final String alertaFichaje="alertaFichaje";
		private final String interfazGestor="interfazGestor";


		@RequestMapping(value = "abrirFichajeGeneral", method = RequestMethod.POST)
		public String abrirFichajeGeneral(HttpServletRequest request, Model model) throws Exception {
			String returned="";
			String hora,fecha;
			ObjectId id;

			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectFichajes);

			hora=fichajedao.getCurrentTimeUsingCalendar();
			fecha=(java.time.LocalDate.now()).toString();



			Fichaje fichaje = new Fichaje(usuario.getDni(), fecha, hora,null,true);

			if(!fichajedao.validezAbierto(fichaje)) {
				model.addAttribute(errorMessageAbrir, "No puedes abrir otro fichaje, necesitas cerrar tu fichaje actual");
				if(usuario.getRol().equalsIgnoreCase("Empleado")) {
					returned=fichajes;
				}else if(usuario.getRol().equalsIgnoreCase("administrador")){
					returned=interfazAdministradorFichaje;

				}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
					returned=interfazGestor;
				}

			}else {
				id=fichajedao.abrirFichaje(fichaje);

				Fichaje fichajeabierto = new Fichaje(id,usuario.getDni(), fecha, hora,null,true);

				model.addAttribute("seleccionadoFichaje",fichajeabierto);

				model.addAttribute(alertaFichaje,"Ha abierto un fichaje correctamente");

				if(usuario.getRol().equalsIgnoreCase("Empleado")) {
					returned=fichajes;
				}else if(usuario.getRol().equalsIgnoreCase("administrador")){
					returned=interfazAdministradorFichaje;

				}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
					returned=interfazGestor;
				}
			}
			return returned;
		} 

		@RequestMapping(value = "cerrarFichajeGeneral", method = RequestMethod.POST)
		public String cerrarFichajeGeneral(HttpServletRequest request, Model model) throws Exception {
			Usuario usuario;
			String returned="";
			String fecha,horaentrada, idAbiertoString;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectFichajes);
			fecha=(java.time.LocalDate.now()).toString();
			horaentrada=fichajedao.getHoraEntrada(usuario.getDni(),fecha);


			idAbiertoString = request.getParameter("idFichajeAbierto");
			ObjectId idAbierto=new ObjectId(idAbiertoString);

			String horaactual;
			horaactual=fichajedao.getCurrentTimeUsingCalendar();
			fecha=(java.time.LocalDate.now()).toString();

			Fichaje fichaje = new Fichaje(idAbierto,usuario.getDni(), fecha,horaentrada,horaactual,false);

			if(fichajedao.validezCerrado(fichaje)) {
				fichajedao.cerrarFichaje(usuario, fichaje);
				model.addAttribute(alertaFichaje,"Ha cerrado un fichaje correctamente");
				if(usuario.getRol().equalsIgnoreCase("Empleado")) {
					returned=fichajes;
				}else if(usuario.getRol().equalsIgnoreCase("administrador")){
					returned=interfazAdministradorFichaje;

				}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
					returned=interfazGestor;
				}

			}else {
				model.addAttribute(errorMessageCerrar, "No puedes cerrar ning&uacuten fichaje, necesitas fichar para cerrar un fichaje");
				if(usuario.getRol().equalsIgnoreCase("Empleado")) {
					returned=fichajes;
				}else if(usuario.getRol().equalsIgnoreCase("administrador")){
					returned=interfazAdministradorFichaje;

				}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
					returned=interfazGestor;
				}
			}
			return returned;

		} 

		@RequestMapping(value = "consultaFichajesFechaGeneral", method = RequestMethod.GET)
		public String consultaFichajesFechaGeneral(HttpServletRequest request, Model model) {
			Usuario usuario;
			String returned="";
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectFichajes);

			String dni = usuario.getDni();
			String fecha1= request.getParameter("fecha1");
			String fecha2= request.getParameter("fecha2");

			if(!DAOFichaje.existeFichajesPeriodo(dni, fecha1,fecha2)) {
				model.addAttribute("nullFecha","No existe ning&uacuten fichaje en ese periodo de fechas");

				if(usuario.getRol().equalsIgnoreCase("Empleado")) {
					returned=fichajes;
				}else if(usuario.getRol().equalsIgnoreCase("administrador")){
					returned=interfazAdministradorFichaje;

				}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
					returned=interfazGestor;
				}
			}else {
				List<Document> listaFichajesFecha =DAOFichaje.listarFichajesPeriodo(dni, fecha1,fecha2);
				model.addAttribute("listafichajes", listaFichajesFecha);

				if(usuario.getRol().equalsIgnoreCase("Empleado")) {
					returned=fichajes;
				}else if(usuario.getRol().equalsIgnoreCase("administrador")){
					returned=interfazAdministradorFichaje;

				}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
					returned=interfazGestor;
				}
			}
			return returned;
		}






		/***Redireccion a gestionPwd***/


		@RequestMapping(value = "/gestionPwd", method = RequestMethod.GET)
		public ModelAndView gestionPwd(HttpServletRequest request) {
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectFichajes); 
			request.setAttribute("nombreUser", usuario.getNombre());
			request.setAttribute("mailUser", usuario.getEmail());
			return new ModelAndView("gestionPwd");
		}

		@RequestMapping(value = "REfichajes", method = RequestMethod.GET)
		public ModelAndView REfichajes() {
			return new ModelAndView("fichajes");
		}

		@RequestMapping(value = "/modificarIncidencia", method = RequestMethod.GET)
		public ModelAndView modificarIncidencia(HttpServletRequest request) {
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectFichajes); 
			request.setAttribute("nombreUser", usuario.getNombre());
			request.setAttribute("dniUser", usuario.getDni());
			return new ModelAndView("modificarIncidencia");
		}

//Home Controller

		
		private final String usuario_login = "login";
		private final String usuario_conectHome = "usuarioConectado";
		private final String alertHome = "alerta";
		private final String usuarioServ = "usuario";
		private final String name = "nombre";
		private final String password = "pwd";
		private final String email = "email";
		private final String rol = "rol";
		private final String dni = "dni";
		
		private final String welcome = "welcome";
		

		private static final Logger logger = LoggerFactory.getLogger(Controlador.class);

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
				model.addAttribute(alertHome, "Por favor rellene los campos");
				return usuario_login;
			}
			Usuario usuario = new Usuario();
			usuario.setEmail(email);
			usuario.setPassword(password);
			try {
			String nombre =  userDao.devolverUser(usuario);
			usuario.setNombre(nombre);
			String dni = userDao.devolverDni(usuario);
			usuario.setDni(dni);
			}catch(Exception e) {
				
			}
			if (userDao.login(usuario) && request.getSession().getAttribute(usuario_conectHome) == null){
				usuario.setRol(userDao.devolverRol(usuario));

				if(usuario.getRol().equalsIgnoreCase("empleado")) {
					request.getSession().setAttribute(usuario_conectHome, usuario);
					request.setAttribute("nombreUser", usuario);
					request.setAttribute("mailUser", email);
					request.setAttribute("dniUser", dni);
					return "fichajes";
				}else if (usuario.getRol().equalsIgnoreCase("administrador")){
					request.getSession().setAttribute(usuario_conectHome, usuario);
					request.setAttribute("nombreUser", usuario.getNombre());
					request.setAttribute("mailUser", email);
					request.setAttribute("dniUser", dni);
					return interfazAdministrador;
				}else if (usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
					request.getSession().setAttribute(usuario_conectHome, usuario);
					request.setAttribute("nombreUser", usuario.getNombre());
					request.setAttribute("mailUser", email);
					request.setAttribute("dniUser", dni);
					return "interfazGestor";
				}

			}else{
				model.addAttribute(alertHome, "Usuario y/o clave incorrectos");
				return usuario_login;
			}
			return usuario_login;
	}

		public ModelAndView cambiarVista(String nombreVista) {
			ModelAndView vista = new ModelAndView(nombreVista);
			return vista;
		}

		@RequestMapping(value = "/logout", method = RequestMethod.GET)
		public ModelAndView cerrarSesion(HttpServletRequest request) throws Exception {
			HttpSession sesion = request.getSession();

			System.out.println("Sesion antes de invalidar: " + sesion);
			sesion.invalidate();
			System.out.println("Invalidamos la sesion: " + sesion);

			return cambiarVista(usuario_login);
		}
		//Incidencia

		private final String fichajesIncidencia = "fichajes";
		private final String interfazAdministrador="interfazAdministrador";
		private final String interfazGestorIncidencia="interfazGestor";
		private final String usuario_conectIncidencia = "usuarioConectado";
		DAOIncidencia incidenciaDao = new DAOIncidencia();
		
		
		@RequestMapping(value = "/crearIncidenciaGeneral", method = RequestMethod.POST)
		public String crearIncidenciaGeneral(HttpServletRequest request, Model model) throws Exception {
			String returned="";
			Usuario usuario;
		    usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
		   
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
				returned=fichajesIncidencia;
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned=interfazGestorIncidencia;
			}
			return returned;
		}


		@RequestMapping(value = "seleccionarIncidencia", method = RequestMethod.GET)
		public String seleccionarIncidencia(HttpServletRequest request, Model model) {
			
			String idIncidencia=request.getParameter("idI");
			ObjectId id=new ObjectId(idIncidencia);
			
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
			
			
			Incidencia inci = incidenciaDao.buscarIncidenciaID(id);
			model.addAttribute("seleccionadaInci", inci); 
			//Creacion de lista de incidencias de nuevo
			List<Document> listaIncidenciasGestor =incidenciaDao.getIncidenciasGestor();
			model.addAttribute("listaIncidencias", listaIncidenciasGestor);
			
			return "resolverIncidencia";

		
		}
		
		@RequestMapping(value = "resolverIncidencia", method = RequestMethod.GET)
		public String resolverIncidencia(HttpServletRequest request, Model model) throws Exception {
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
			String texto=request.getParameter("textoGestor");
			String modo="resolver";
			
			String idIncidencia=request.getParameter("idSeleccionada");
			ObjectId id=new ObjectId(idIncidencia);
			
			Incidencia resuelta=incidenciaDao.resolverIncidencia(id,texto);
			
			incidenciaDao.updateIncidencia(resuelta,modo);
			
			//Creacion de lista de incidencias de nuevo
			List<Document> listaIncidenciasGestor =incidenciaDao.getIncidenciasGestor();
			model.addAttribute("listaIncidencias", listaIncidenciasGestor);
		
			return "resolverIncidencia";
		}
		
		@RequestMapping(value = "denegarIncidencia", method = RequestMethod.GET)
		public String denegarIncidencia(HttpServletRequest request, Model model) throws Exception {
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
			String texto=request.getParameter("textoGestor");
			String modo="denegar";
			
			String idIncidencia=request.getParameter("idSeleccionada");
			ObjectId id=new ObjectId(idIncidencia);
			
			Incidencia denegada=incidenciaDao.denegarIncidencia(id,texto);
			incidenciaDao.updateIncidencia(denegada,modo);

			//Creacion de lista de incidencias de nuevo
			List<Document> listaIncidenciasGestor =incidenciaDao.getIncidenciasGestor();
			model.addAttribute("listaIncidencias", listaIncidenciasGestor);

			return "resolverIncidencia";
		}
		
		@RequestMapping(value = "listarIncidenciasGestor", method = RequestMethod.GET)
		public String listarIncidenciasGestor(HttpServletRequest request, Model model) {
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);

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
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
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
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
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
			
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
			
			
			Incidencia inci = incidenciaDao.buscarIncidenciaID(id);
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
			
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
			
			
			Incidencia inci = incidenciaDao.buscarIncidenciaID(id);
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
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
			String categoria = request.getParameter("listaTiposIncidencia");
			String fecha = request.getParameter("txtFecha");
			String descripcion = request.getParameter("textoIncidencia");
			
			if(categoria.equalsIgnoreCase("")||fecha.equalsIgnoreCase("")||descripcion.equalsIgnoreCase("")) {
				model.addAttribute("alerta", "Por favor rellene los campos");
				return "modificarIncidencia";
			}
			
			String idIncidencia=request.getParameter("idSeleccionada");
			ObjectId id=new ObjectId(idIncidencia);
			
			Incidencia incidencia= incidenciaDao.devolverIncidencia(id, categoria,fecha,descripcion);
			try {
				incidenciaDao.updateIncidencia(incidencia,modo);
			}catch(Exception e) {
				
			}
			List<Document> listaIncidencias =incidenciaDao.getIncidencias(usuario.getDni());
			model.addAttribute("listaIncidencias", listaIncidencias);
			
			if(usuario.getRol().equalsIgnoreCase("Empleado")) {
				returned=fichajesIncidencia;
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned=interfazGestorIncidencia;
			}
			
			return returned;	
		}
		@RequestMapping(value = "eliminarIncidenciaUser", method = RequestMethod.GET)
		public String eliminarIncidencia(HttpServletRequest request, Model model) {
			String returned="";
			Usuario usuario;
			usuario = (Usuario) request.getSession().getAttribute(usuario_conectIncidencia);
			String idIncidencia=request.getParameter("idSeleccionada");
			ObjectId id=new ObjectId(idIncidencia);
			
			System.out.println("id object id " + id);
			Incidencia incidencia= incidenciaDao.devolverIncidencia(id);
			try {
				incidenciaDao.delete(incidencia);
			}catch(Exception e) {
				
			}
			if(usuario.getRol().equalsIgnoreCase("Empleado")) {
				returned=fichajesIncidencia;
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned=interfazGestorIncidencia;
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
		
		//Usuario
		private final String alertUsuario = "alerta";
		private final String gestionPwd = "gestionPwd";
		private final String usuario_conectUsuario = "usuarioConectado";


		@RequestMapping(value = "/modificarPwd", method = RequestMethod.POST)
		public String modificarPwd(HttpServletRequest request, Model model) throws Exception {
			Usuario usuarioLigero = (Usuario) request.getSession().getAttribute(usuario_conectUsuario);
			String emailActual = usuarioLigero.getEmail();
			
			String pwdActual = request.getParameter("contrasenaActual");
			String pwdNueva = request.getParameter("contrasenaNueva");
			String pwdNueva2 = request.getParameter("contrasenaNueva2");
			String nombre = userDao.devolverUser(usuarioLigero);
		
			Usuario usuario = userDao.selectNombre(nombre);
			usuario.setEmail(emailActual);
			usuario.setPassword(pwdActual);
			
			
			
			if(!userDao.login(usuario)) {
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				model.addAttribute(alertUsuario, "Password actual incorrecta");
				return gestionPwd;
			}
			if(Utilidades.comprobarPwd(usuario.getDni(), pwdActual, pwdNueva)==false){
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				model.addAttribute("alertaPWDRepe","Password anteriormente utilizada");
				return gestionPwd;
			}
			if (usuario == null || !(pwdNueva.equals(pwdNueva2))) {
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				model.addAttribute(alertUsuario, "Datos incorrectos");
				return gestionPwd;
			}
			
			try {
		
			} catch (Exception e) {
				model.addAttribute(alert, e.getMessage());
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				return gestionPwd;
			}
			
			if(!Utilidades.seguridadPassword(pwdNueva)) {
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				model.addAttribute("alertaPWDinsegura","Password poco segura (minimo 8 caracteres, con numeros y letras)");
				return gestionPwd;
			}else {
				usuario.setPassword(pwdNueva);
				userDao.updatePwd(usuario);
				HttpSession session = request.getSession();
				request.setAttribute("nombreUser", usuario.getNombre());
				request.setAttribute("mailUser", usuario.getEmail());
				session.setAttribute("alertaCambio", "La contrase&ntilde;a ha sido cambiada satisfactoriamente");
				return gestionPwd;
			}
		}
		
		
		
		@RequestMapping(value = "/REfichajesUser", method = RequestMethod.GET)
		public ModelAndView REfichajesUser(HttpServletRequest request,Model model) {
			String returned="";
			Usuario usuario = (Usuario) request.getSession().getAttribute(usuario_conectUsuario);
			if(usuario.getRol().equalsIgnoreCase("Empleado")) {
				returned="fichajes";
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned="interfazGestor";
			}
			
			return new ModelAndView(returned);
		}
		
		

}
