package com.uclm.equipo02;

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
import com.mongodb.client.MongoCursor;
import com.uclm.equipo02.Auxiliar.Utilidades;
import com.uclm.equipo02.mail.MailSender;
import com.uclm.equipo02.modelo.Usuario;
import com.uclm.equipo02.persistencia.MongoBroker;


@Controller
public class AdminController {
	private final String name = "nombre";
	private final String password = "pwd";
	private final String email = "email";
	private final String rol = "rol";
	private final String dni = "dni";

	//private final String usuario_login = "login";
	Usuario user = new Usuario();
	private final String alert = "alerta";
	private final String usuario_conect = "usuarioConectado";
	private final String adminUpdatePwd = "adminUpdatePwd";

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
			insert(user);
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
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> incidencias = broker.getCollection("Contrasenas");
		return incidencias;
	}
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
	private MongoCollection<Document> obtenerUsuarios() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> usuarios = broker.getCollection("Usuarios");
		return usuarios;
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
				user.setEmail(devolverMail(user));
				user.setNombre(devolverUser(user));
				delete(user);
			}catch(Exception e) {

			}

			return "interfazAdministrador";
		}
	}
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
	
	@RequestMapping(value = "/buscarUsuarioPorDni", method = RequestMethod.GET)
	public String buscarUsuario(HttpServletRequest request, Model model) throws Exception {

		String dni = request.getParameter("txtDniBusqueda");
		
		if(!existeUser(dni)) {
			model.addAttribute("alertaUsuarioNull","El usuario buscado no existe");
			return "modificarUsuario";
		}else {

		user.setDni(dni);
		
		user.setEmail(devolverMail(user));
		user.setNombre(devolverUser(user));
		user.setRol(devolverRol(user));
		
		
		HttpSession session = request.getSession();
		request.setAttribute("nombreUser", user.getNombre());
		request.setAttribute("dniUser", user.getDni());

		model.addAttribute("RolUsuario", user.getRol());
		model.addAttribute("EmailUsuario", user.getEmail());
		
		return "modificarUsuario";
		}

		

	}
	
	
	private boolean existeUser(String dni) {
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

	private MongoCollection<Document> getUsuarios() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> usuarios = broker.getCollection("Usuarios");
		return usuarios;
	}

	@RequestMapping(value = "/modificarUser", method = RequestMethod.GET)
	public String modificarUser(HttpServletRequest request, Model model) throws Exception {

		String rol = request.getParameter("listaRoles");
		String email = request.getParameter("txtEmail");
		try {
			
			updateEmail(user, email);
			updateRol(user, rol);
		}catch(Exception e) {

		}

		return "interfazAdministrador";

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
	
	
	@RequestMapping(value = "/adminModificarPwd", method = RequestMethod.POST)
	public String adminModificarPwd(HttpServletRequest request, Model model) throws Exception {
		Usuario usuarioLigero = (Usuario) request.getSession().getAttribute(usuario_conect);
		
		String dniUsuario = request.getParameter("dniUsuario");
		
		
		String pwdNueva = request.getParameter("contrasenaNueva");
		String pwdNueva2 = request.getParameter("contrasenaNueva2");
		
		
		Usuario usuarioBusqueda= new Usuario();
		
		
		
		if(!existeUser(dniUsuario)) {
			model.addAttribute("alertaUsuarioNull","El usuario buscado no existe");
			return adminUpdatePwd;
			
		}else {
		
		usuarioBusqueda = buscarUsuarioEmail(dniUsuario);
		
		
		String nombre = devolverUser(usuarioBusqueda);

		Usuario usuario = selectNombre(nombre);
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
			updatePwd(usuario);
			HttpSession session = request.getSession();
			request.setAttribute("usuarioNombre", usuario.getNombre());
			request.setAttribute("usuarioEmail", usuario.getEmail());
			session.setAttribute("alertaCambio", "La contrase&ntilde;a ha sido cambiada satisfactoriamente");
			return adminUpdatePwd;
		}
		
		}
		
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
	
}
