package com.uclm.equipo02;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.security.auth.message.callback.PrivateKeyCallback.Request;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.uclm.equipo02.modelo.Fichaje;
import com.uclm.equipo02.modelo.Usuario;
import com.uclm.equipo02.persistencia.MongoBroker;

import org.bson.Document;
import org.bson.types.ObjectId;

@Controller
public class FichajeController {



	private final String usuario_conect = "usuarioConectado";
	private final String errorMessageAbrir = "errorMessageAbrir";
	private final String errorMessageCerrar = "errorMessageCerrar";
	private final String fichajes = "fichajes";
	private final String interfazAdministrador="interfazAdministrador";
	private final String alertaFichaje="alertaFichaje";
	private final String interfazGestor="interfazGestor";


	@RequestMapping(value = "abrirFichajeGeneral", method = RequestMethod.POST)
	public String abrirFichajeGeneral(HttpServletRequest request, Model model) throws Exception {
		String returned="";
		String hora,fecha;
		ObjectId id;

		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);

		hora=getCurrentTimeUsingCalendar();
		fecha=(java.time.LocalDate.now()).toString();



		Fichaje fichaje = new Fichaje(usuario.getDni(), fecha, hora,null,true);

		if(!validezAbierto(fichaje)) {
			model.addAttribute(errorMessageAbrir, "No puedes abrir otro fichaje, necesitas cerrar tu fichaje actual");
			if(usuario.getRol().equalsIgnoreCase("Empleado")) {
				returned=fichajes;
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned=interfazGestor;
			}

		}else {
			id=abrirFichaje(fichaje);

			Fichaje fichajeabierto = new Fichaje(id,usuario.getDni(), fecha, hora,null,true);

			model.addAttribute("seleccionadoFichaje",fichajeabierto);

			model.addAttribute(alertaFichaje,"Ha abierto un fichaje correctamente");

			if(usuario.getRol().equalsIgnoreCase("Empleado")) {
				returned=fichajes;
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned=interfazGestor;
			}
		}
		return returned;
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

	private boolean validezAbierto(Fichaje fichaje) {
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
	public static MongoCollection<Document> getFichajes() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> fichajes = broker.getCollection("Fichajes");
		return fichajes;
	}

	private String getCurrentTimeUsingCalendar() {
		Calendar cal = Calendar.getInstance();
		Date date=cal.getTime();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		String formattedTime=dateFormat.format(date);
		return formattedTime;

	}

	@RequestMapping(value = "cerrarFichajeGeneral", method = RequestMethod.POST)
	public String cerrarFichajeGeneral(HttpServletRequest request, Model model) throws Exception {
		Usuario usuario;
		String returned="";
		String fecha,horaentrada, idAbiertoString;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);
		fecha=(java.time.LocalDate.now()).toString();
		horaentrada=getHoraEntrada(usuario.getDni(),fecha);


		idAbiertoString = request.getParameter("idFichajeAbierto");
		ObjectId idAbierto=new ObjectId(idAbiertoString);

		String horaactual;
		horaactual=getCurrentTimeUsingCalendar();
		fecha=(java.time.LocalDate.now()).toString();

		Fichaje fichaje = new Fichaje(idAbierto,usuario.getDni(), fecha,horaentrada,horaactual,false);

		if(validezCerrado(fichaje)) {
			cerrarFichaje(usuario, fichaje);
			model.addAttribute(alertaFichaje,"Ha cerrado un fichaje correctamente");
			if(usuario.getRol().equalsIgnoreCase("Empleado")) {
				returned=fichajes;
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned=interfazGestor;
			}

		}else {
			model.addAttribute(errorMessageCerrar, "No puedes cerrar ning&uacuten fichaje, necesitas fichar para cerrar un fichaje");
			if(usuario.getRol().equalsIgnoreCase("Empleado")) {
				returned=fichajes;
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned=interfazGestor;
			}
		}
		return returned;

	}
	public void cerrarFichaje(Usuario usuario, Fichaje fichaje) {
		MongoCollection<Document> fichajes = getFichajes();
		MongoBroker broker = MongoBroker.get();

		Document criteria=new Document();
		criteria.put("_id", fichaje.get_id());
		criteria.put("dniEmpleado", usuario.getDni());
		criteria.put("fechaFichaje", fichaje.getFechaFichaje());



		Document changes=new Document();

		changes.put("estado", fichaje.getEstado());
		changes.put("horaSalida", fichaje.getHoraSalida());
		Document doc = new Document();
		doc.put("$set", changes);

		broker.updateDoc(fichajes, criteria, doc);


	}
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

	@RequestMapping(value = "consultaFichajesFechaGeneral", method = RequestMethod.GET)
	public String consultaFichajesFechaGeneral(HttpServletRequest request, Model model) {
		Usuario usuario;
		String returned="";
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect);

		String dni = usuario.getDni();
		String fecha1= request.getParameter("fecha1");
		String fecha2= request.getParameter("fecha2");

		if(!existeFichajesPeriodo(dni, fecha1,fecha2)) {
			model.addAttribute("nullFecha","No existe ning&uacuten fichaje en ese periodo de fechas");

			if(usuario.getRol().equalsIgnoreCase("Empleado")) {
				returned=fichajes;
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned=interfazGestor;
			}
		}else {
			List<Document> listaFichajesFecha =listarFichajesPeriodo(dni, fecha1,fecha2);
			model.addAttribute("listafichajes", listaFichajesFecha);

			if(usuario.getRol().equalsIgnoreCase("Empleado")) {
				returned=fichajes;
			}else if(usuario.getRol().equalsIgnoreCase("administrador")){
				returned=interfazAdministrador;

			}else if(usuario.getRol().equalsIgnoreCase("Gestor de incidencias")){
				returned=interfazGestor;
			}
		}
		return returned;
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
		 
		Date fechafichaje=parserFecha(fechaFichaje);
		
		if(periodo.contains(fechafichaje)) {
			bool= true;
		}
		
		return bool;
		
	}







	/***Redireccion a gestionPwd***/


	@RequestMapping(value = "/gestionPwd", method = RequestMethod.GET)
	public ModelAndView gestionPwd(HttpServletRequest request) {
		Usuario usuario;
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect); 
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
		usuario = (Usuario) request.getSession().getAttribute(usuario_conect); 
		request.setAttribute("nombreUser", usuario.getNombre());
		request.setAttribute("dniUser", usuario.getDni());
		return new ModelAndView("modificarIncidencia");
	}
public static List<Date> calculoPeriodoFechas(String fecha1, String fecha2) {
		
		Date startdate = parserFecha(fecha1);
		Date enddate = parserFecha(fecha2);
		
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
public static Date parserFecha(String fecha) {
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


}
