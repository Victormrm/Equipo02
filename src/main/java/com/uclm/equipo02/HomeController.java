package com.uclm.equipo02;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.uclm.equipo02.modelo.Usuario;
import com.uclm.equipo02.persistencia.Persistencia;

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
	
	

	Persistencia persis = new Persistencia();


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
		String nombre =  persis.devolverUser(usuario);
		usuario.setNombre(nombre);
		String dni = persis.devolverDni(usuario);
		usuario.setDni(dni);
		}catch(Exception e) {
			
		}
		if (persis.login(usuario) && request.getSession().getAttribute(usuario_conect) == null){
			usuario.setRol(persis.devolverRol(usuario));

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
}
