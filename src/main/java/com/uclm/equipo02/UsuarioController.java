package com.uclm.equipo02;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


import com.uclm.equipo02.Auxiliar.Utilidades;
import com.uclm.equipo02.modelo.Modelo;
import com.uclm.equipo02.persistencia.UsuarioDaoImplement;

@Controller
public class UsuarioController {
	private final String alert = "alerta";
	private final String gestionPwd = "gestionPwd";
	private final String usuario_conect = "usuarioConectado";

	UsuarioDaoImplement userDao = new UsuarioDaoImplement();

	@RequestMapping(value = "/modificarPwd", method = RequestMethod.POST)
	public String modificarPwd(HttpServletRequest request, Model model) throws Exception {
		Modelo usuarioLigero = (Modelo) request.getSession().getAttribute(usuario_conect);
		String emailActual = usuarioLigero.getEmail();
		
		String pwdActual = request.getParameter("contrasenaActual");
		String pwdNueva = request.getParameter("contrasenaNueva");
		String pwdNueva2 = request.getParameter("contrasenaNueva2");
		String nombre = userDao.devolverUser(usuarioLigero);
	
		Modelo usuario = userDao.selectNombre(nombre);
		usuario.setEmail(emailActual);
		usuario.setPassword(pwdActual);
		
		
		
		if(!userDao.login(usuario)) {
			request.setAttribute("nombreUser", usuario.getNombre());
			request.setAttribute("mailUser", usuario.getEmail());
			model.addAttribute(alert, "Password actual incorrecta");
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
			model.addAttribute(alert, "Datos incorrectos");
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
		Modelo usuario = (Modelo) request.getSession().getAttribute(usuario_conect);
		if(usuario.getRol().equalsIgnoreCase("Empleado")) {
			returned="fichajes";
		}else if(usuario.getRol().equalsIgnoreCase("administrador")){
			returned="interfazAdministrador";

		}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
			returned="interfazGestor";
		}
		
		return new ModelAndView(returned);
	}
	
	
	

}
