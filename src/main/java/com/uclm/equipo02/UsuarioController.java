package com.uclm.equipo02;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bson.BsonString;
import org.bson.Document;
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
public class UsuarioController {
	private final String alert = "alerta";
	private final String gestionPwd = "gestionPwd";
	private final String usuario_conect = "usuarioConectado";

	private final String name = "nombre";
	private final String password = "pwd";
	private final String email = "email";
	private final String rol = "rol";
	private final String dni = "dni";

	@RequestMapping(value = "/modificarPwd", method = RequestMethod.POST)
	public String modificarPwd(HttpServletRequest request, Model model) throws Exception {
		Usuario usuarioLigero = (Usuario) request.getSession().getAttribute(usuario_conect);
		String emailActual = usuarioLigero.getEmail();
		
		String pwdActual = request.getParameter("contrasenaActual");
		String pwdNueva = request.getParameter("contrasenaNueva");
		String pwdNueva2 = request.getParameter("contrasenaNueva2");
		String nombre = devolverUser(usuarioLigero);
	
		Usuario usuario = selectNombre(nombre);
		usuario.setEmail(emailActual);
		usuario.setPassword(pwdActual);
		
		
		
		if(!login(usuario)) {
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
			updatePwd(usuario);
			HttpSession session = request.getSession();
			request.setAttribute("nombreUser", usuario.getNombre());
			request.setAttribute("mailUser", usuario.getEmail());
			session.setAttribute("alertaCambio", "La contrase&ntilde;a ha sido cambiada satisfactoriamente");
			return gestionPwd;
		}
	}
	public static MongoCollection<Document> getContrasenas() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> incidencias = broker.getCollection("Contrasenas");
		return incidencias;
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
	private MongoCollection<Document> obtenerUsuarios() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> usuarios = broker.getCollection("Usuarios");
		return usuarios;
	}

	public String devolverUser(Usuario usuario) {
		MongoCollection<Document> usuarios = obtenerUsuarios();
		Document criterio = new Document();
		criterio.append("email", new BsonString(usuario.getEmail()));
		FindIterable<Document> resultado=usuarios.find(criterio);
		Document usuariobso = resultado.first();
		if (usuario==null || usuariobso ==null){
			return null;
		}else {
			String nombreFinal= usuariobso.getString("nombre");
			usuario.setNombre(nombreFinal);
		}
		return usuario.getNombre();
	}
	
	
	
	@RequestMapping(value = "/REfichajesUser", method = RequestMethod.GET)
	public ModelAndView REfichajesUser(HttpServletRequest request,Model model) {
		String returned="";
		Usuario usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
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
