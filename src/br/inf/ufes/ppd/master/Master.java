package br.inf.ufes.ppd.master;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import br.inf.ufes.ppd.Attacker;
import br.inf.ufes.ppd.Guess;


public class Master implements Attacker {

	private Map<Integer, List<Guess>> guessesMap = new HashMap<>();
	private int attackNumber;
	private int lineNumber;
	// Thread para verificar os checkpoints dos slaves.
	private byte[] ciphertext;
	private byte[] knowntext;

	public Master(String fileName) {
		this.lineNumber = readFile(fileName);
		this.attackNumber = 0;
	}
	
	public static void main(String[] args) {
		try {
			Master master = new Master(args[0]);
			Master masterRef = (Master) UnicastRemoteObject.exportObject(master, 0);
			Registry registry = LocateRegistry.getRegistry("localhost");
			registry.rebind("mestre", masterRef);
			System.out.println("[System Initiation] Master ready!");
		} catch (RemoteException e) {
			System.err.println("[Error] Error to connect master to server.");
			e.printStackTrace();
		}
	}

	@Override
	public Guess[] attack(byte[] ciphertext, byte[] knowntext) throws RemoteException {
		this.setCiphertext(ciphertext);
		this.setKnowntext(knowntext);
		int attackNumberLocal;
//		long initialwordindex, finalwordindex, modwordindex;

		// Atribui os valores para as variáveis que serão utilizadas no ataque.
//		initialwordindex = 0;
//		finalwordindex = this.getLineNumber() / getMapSlaveSize();

		// Armazena o resto da divisão para acrescentar na última partição.
//		modwordindex = this.getLineNumber() % getMapSlaveSize();
		attackNumberLocal = this.getAttackNumber();

		System.out.println("[System Attack] Attack no." + attackNumberLocal + " has begun!");

		this.getGuessesMap().put(attackNumberLocal, new ArrayList<Guess>());

//		long keyNumbers = finalwordindex;

		this.incrementAttackNumber();

		System.out.println("[System Attack] Attack no." + attackNumberLocal + " is done!");

		Guess[] guesses = new Guess[this.getGuessesMap().get(attackNumberLocal).size()];
		int guessCount = 0;
		for (Guess g : this.getGuessesMap().get(attackNumberLocal)) {
			guesses[guessCount++] = g;
		}

		return guesses;
	}

	private int readFile(String fileName) {
		int i = 0;
		try {
			Scanner scanner = new Scanner(new File(fileName));
			while (scanner.hasNextLine()) {
				scanner.nextLine();
				i++;
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("[Error] FileNotFound error in MasterImpl.");
			e.printStackTrace();
		}
		return i - 1;
	}

	public Map<Integer, List<Guess>> getGuessesMap() {
		return guessesMap;
	}

	public int getAttackNumber() {
		return this.attackNumber;
	}

	public void incrementAttackNumber() {
		this.attackNumber++;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}

	public byte[] getCiphertext() {
		return this.ciphertext;
	}

	public void setCiphertext(byte[] ciphertext) {
		this.ciphertext = ciphertext;
	}

	public byte[] getKnowntext() {
		return this.knowntext;
	}

	public void setKnowntext(byte[] knowntext) {
		this.knowntext = knowntext;
	}
}