package com.uclm.equipo02.modelo;

import org.bson.types.ObjectId;

import com.uclm.equipo02.persistencia.Persistencia;

public class Incidencia {
	@Override
	public String toString() {
		return "Incidencia [id= "+ _id.toString() +", nombreUsuario=" + nombreUsuario + ", dniUsuario=" + dniUsuario + ", categoria=" + categoria
				+ ", fechaCreacion=" + fechaCreacion + ", descripcion=" + descripcion + ", estado=" + estado
				+ ", comentarioGestor=" + comentarioGestor +  "]";
	}

	private String nombreUsuario,dniUsuario,categoria,fechaCreacion,descripcion,estado,comentarioGestor;
	private ObjectId _id;
	

	private Persistencia persis = new Persistencia();
	
	public Incidencia() {
		
	}

	public Incidencia(String nombreUsuario, String dniUsuario, String categoria, String descripcion, String estado, 
			String fechaCreacion, String comentarioGestor) {
		this.nombreUsuario = nombreUsuario;
		this.dniUsuario = dniUsuario;
		this.categoria = categoria;
		this.fechaCreacion = fechaCreacion;
		this.descripcion = descripcion;
		this.estado = estado;
		this.comentarioGestor = comentarioGestor;
	}
	
	public Incidencia(ObjectId _id,String nombreUsuario, String dniUsuario, String categoria, String descripcion, String estado, 
			String fechaCreacion, String comentarioGestor) {
		this._id=_id;
		this.nombreUsuario = nombreUsuario;
		this.dniUsuario = dniUsuario;
		this.categoria = categoria;
		this.fechaCreacion = fechaCreacion;
		this.descripcion = descripcion;
		this.estado = estado;
		this.comentarioGestor = comentarioGestor;
	}
	
	public Incidencia(String dniUsuario, String categoria, String fechaCreacion, String descripcion) {
		this.dniUsuario=dniUsuario;
		this.categoria=categoria;
		this.fechaCreacion=fechaCreacion;
		this.descripcion=descripcion;
		
	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
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

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	
	

}
