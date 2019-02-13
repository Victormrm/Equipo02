package com.uclm.equipo02.modelo;

import org.bson.types.ObjectId;

public class Fichaje {
	
	private String fechaFichaje, horaEntrada, horaSalida, dniEmpleado;
	private ObjectId _id;

	private boolean estado; //true=abierto false=cerrado

	


	////Fichaj con una sola hora de fichaje  y el metodo cerraFIchaje --> horaEntrada=horaSalida
	///AnADIR HORA SALIDA Y ACTUALIZAR 


	public Fichaje(String dniEmpleado, String fechaFichaje, String horaEntrada,String horaSalida,boolean estado ) {
		this.dniEmpleado=dniEmpleado;
		this.fechaFichaje = fechaFichaje;
		this.horaEntrada = horaEntrada;
		this.horaSalida=horaSalida;
		this.estado = estado;	//Tru--> Fichaje Abierto False--> Fichaje Cerrado
	}
	
	public Fichaje(ObjectId _id,String dniEmpleado, String fechaFichaje, String horaEntrada,String horaSalida,boolean estado ) {
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

	@Override
	public String toString() {
		return "Fichaje [id= "+ _id.toString() +", fechaFichaje=" + fechaFichaje + ", horaEntrada=" + horaEntrada + ", horaSalida=" + horaSalida
				+ ", dniEmpleado=" + dniEmpleado + ", estado=" + estado + "]";
	}

	


}