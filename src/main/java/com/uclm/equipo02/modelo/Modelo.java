package com.uclm.equipo02.modelo;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.uclm.equipo02.persistencia.DAOFichaje;
import com.uclm.equipo02.persistencia.DAOIncidencia;

public class Modelo {
	
	
	private String fechaFichaje, horaEntrada, horaSalida, dniEmpleado;
	private ObjectId _id;

	private boolean estado; //true=abierto false=cerrado

	DAOFichaje daofichaje=new DAOFichaje();
	


	////Fichaj con una sola hora de fichaje  y el metodo cerraFIchaje --> horaEntrada=horaSalida
	///AnADIR HORA SALIDA Y ACTUALIZAR 


	public Modelo(String dniEmpleado, String fechaFichaje, String horaEntrada,String horaSalida,boolean estado ) {
		this.dniEmpleado=dniEmpleado;
		this.fechaFichaje = fechaFichaje;
		this.horaEntrada = horaEntrada;
		this.horaSalida=horaSalida;
		this.estado = estado;	//Tru--> Fichaje Abierto False--> Fichaje Cerrado
	}
	
	public Modelo(ObjectId _id,String dniEmpleado, String fechaFichaje, String horaEntrada,String horaSalida,boolean estado ) {
		this._id=_id;
		this.dniEmpleado=dniEmpleado;
		this.fechaFichaje = fechaFichaje;
		this.horaEntrada = horaEntrada;
		this.horaSalida=horaSalida;
		this.estado = estado;	//Tru--> Fichaje Abierto False--> Fichaje Cerrado
	}



	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public ObjectId get_id() {
		return _id;
	}
	public String getDNIFichaje() {
		return dniEmpleado;
	}

	public void setDNIFichaje(String dniEmpleado) {
		this.dniEmpleado = dniEmpleado;
	}


	public String getFechaFichaje() {
		return fechaFichaje;
	}
	public void setFechaFichaje(String fechaFichaje) {
		this.fechaFichaje = fechaFichaje;
	}
	public String getHoraEntrada() {
		return horaEntrada;
	}
	public void sethoraEntrada(String horaEntrada) {
		this.horaEntrada = horaEntrada;
	}
	public String getHoraSalida() {
		return horaSalida;
	}
	public void sethoraSalida(String horaSalida) {
		this.horaSalida = horaSalida;
	}

	public boolean getEstado() {
		return estado;
	}

	public void setEstado(boolean estado) {
		this.estado = estado;
	}
/*
	@Override
	public String toString() {
		return "Fichaje [id= "+ _id.toString() +", fechaFichaje=" + fechaFichaje + ", horaEntrada=" + horaEntrada + ", horaSalida=" + horaSalida
				+ ", dniEmpleado=" + dniEmpleado + ", estado=" + estado + "]";
	}

	@Override
	public String toString() {
		return "Incidencia [id= "+ _id.toString() +", nombreUsuario=" + nombreUsuario + ", dniUsuario=" + dniUsuario + ", categoria=" + categoria
				+ ", fechaCreacion=" + fechaCreacion + ", descripcion=" + descripcion + ", estado=" + estado
				+ ", comentarioGestor=" + comentarioGestor + ", dao=" + dao + "]";
	}
*/
	//INCIDENCIAS
	private String nombreUsuario,dniUsuario,categoria,fechaCreacion,descripcion,estadoIncidencia,comentarioGestor;
	private ObjectId _idIncidencia;
	

	private DAOIncidencia dao = new DAOIncidencia();
	
	

	public Modelo(String nombreUsuario, String dniUsuario, String categoria, String descripcion, String estado, 
			String fechaCreacion, String comentarioGestor) {
		this.nombreUsuario = nombreUsuario;
		this.dniUsuario = dniUsuario;
		this.categoria = categoria;
		this.fechaCreacion = fechaCreacion;
		this.descripcion = descripcion;
		this.estadoIncidencia = estado;
		this.comentarioGestor = comentarioGestor;
	}
	
	public Modelo(ObjectId _id,String nombreUsuario, String dniUsuario, String categoria, String descripcion, String estado, 
			String fechaCreacion, String comentarioGestor) {
		this._id=_id;
		this.nombreUsuario = nombreUsuario;
		this.dniUsuario = dniUsuario;
		this.categoria = categoria;
		this.fechaCreacion = fechaCreacion;
		this.descripcion = descripcion;
		this.estadoIncidencia = estado;
		this.comentarioGestor = comentarioGestor;
	}
	
	public Modelo(String dniUsuario, String categoria, String fechaCreacion, String descripcion) {
		this.dniUsuario=dniUsuario;
		this.categoria=categoria;
		this.fechaCreacion=fechaCreacion;
		this.descripcion=descripcion;
		
	}

	public ObjectId get_idIncidencia() {
		return _idIncidencia;
	}

	public void set_idIncidencia(ObjectId _id) {
		this._idIncidencia = _id;
	}
	
	public String getDniUsuario() {
		return dniUsuario;
	}

	public void setDniUsuario(String dniUsuario) {
		this.dniUsuario = dniUsuario;
	}

	public String getComentarioGestor() {
		return comentarioGestor;
	}

	public void setComentarioGestor(String comentarioGestor) {
		this.comentarioGestor = comentarioGestor;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getEstadoIncidencia() {
		return estadoIncidencia;
	}

	public void setEstadoIncidencia(String estado) {
		this.estadoIncidencia = estado;
	}

	public String getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public DAOIncidencia getDao() {
		return dao;
	}

	public void setDao(DAOIncidencia dao) {
		this.dao = dao;
	}
	
	//USUARIOS
	private String nombre;
	private String password;
	private String email;
	private String rol;
	private String dni;

	private DAOFichaje daoFichaje = new DAOFichaje();
	public Modelo(String nombre, String password, String email, String rol,String dni) {
		super();
		this.nombre = nombre;
		this.password = password;
		this.email = email;
		this.rol = rol;
		this.dni = dni;
	}
	
	public List<Document> getFichajesEmpleado(String nombreEmpleado){
		return daoFichaje.fichajesEmpleado(nombreEmpleado);
	}
	
	public Modelo(String nombre) {
		this.nombre=nombre;
	}
	public Modelo() {

	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRol() {
		return rol;
	}
	public void setRol(String rol) {
		this.rol = rol;
	}
	public String getDni() {
		return dni;
	}
	@Override
	public String toString() {
		return "Usuario [nombre=" + nombre + ", password=" + password +"dni = "+dni +", email=" + email + ", rol=" + rol + ", dao="
				+ dao + "]";
	}

	public void setDni(String dni) {
		this.dni=dni;
		
	}
}
