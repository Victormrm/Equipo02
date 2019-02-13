package com.uclm.equipo02.Auxiliar;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
//import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.bson.BsonString;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.uclm.equipo02.persistencia.MongoBroker;




public class Utilidades {


	private static String key = "InTimeQQ12345678";
	private static String initVector = "RandomInitVector";



	public static String encrypt(String value) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(value.getBytes());

			return Base64.encodeBase64String(encrypted);
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		return null;
	}


	public static String decrypt(String encrypted) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

			return new String(original);
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}

		return null;
	}

	/******Cambiar a return string y poner ese string  en una alerta en el updatePWD del controller y en el jsp de updatePWD******/
	public static boolean seguridadPassword(String pwd){
		
		boolean bool=true;
		if (pwd.length() < 8)
			return false;
		boolean caracter = false;
		boolean numero = false;
		char c;
		int size = pwd.length();
		for (int i = 0; i < size; i++) {
			c = pwd.charAt(i);
			if (Character.isDigit(c)) {
				numero = true;
			}
			if (Character.isLetter(c)) {
				caracter = true;
			}
		}
		if (!caracter || !numero) {
			bool=false;
		}
			return bool;
	
	}
	
	public static boolean comprobarPwd(String dni, String pwd, String nuevaPwd) {
		MongoCollection<Document> pwds = getContrasenas();
		Document criterio = new Document();
		criterio.append("pwd", new BsonString(encrypt(nuevaPwd)));
		criterio.append("dni", dni);
		FindIterable<Document> resultado=pwds.find(criterio);
		Document usuarioBson = resultado.first();
		//Document bso = new Document();
		if (usuarioBson==null) {
			return true;
		}else {
			return false;
		}
	
	}

	public static MongoCollection<Document> getContrasenas() {
		MongoBroker broker = MongoBroker.get();
		MongoCollection<Document> incidencias = broker.getCollection("Contrasenas");
		return incidencias;
	}

	public static String passRandom() {
		char[] elementos={'0','1','2','3','4','5','6','7','8','9' ,
				'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

		char[] conjunto = new char[10];
		String pass;

		for(int i=0;i<10;i++){
			int el = (int)(Math.random()*62);
			conjunto[i] = (char)elementos[el];
		}
		return pass = new String(conjunto);
	}

}
