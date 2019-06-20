package br.inf.ufes.ppd;



/**
 * Guess.java
 */


import java.io.Serializable;

public class Guess implements Serializable {

	// Chave candidata
	private String key; 

	// Mensagem decriptografada com a chave candidata
	private byte[] message;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public byte[] getMessage() {
		return message;
	}
	public void setMessage(byte[] message) {
		this.message = message;
	}

}
