package br.inf.ufes.ppd.master;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.*;

import com.sun.messaging.ConnectionConfiguration;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.Master;


public class MasterExecute implements Master, MessageListener {

	private Map<Integer, List<Guess>> guessesMap = new HashMap<>();
	private Map<Integer, AttackFinishThread> attackFinishControlMap = new HashMap<>();
	private int attackNumber;
	private int lineNumber;
	// Thread para verificar os checkpoints dos slaves.
	private byte[] ciphertext;
	private byte[] knowntext;
	private int m;
	

	public MasterExecute(String fileName, int m) {
		this.lineNumber = this.linesNumberFile(fileName);
		this.attackNumber = 0;
		this.m = m;
	}
	
	public static void main(String[] args) {
		try {
			MasterExecute master = new MasterExecute(args[0], Integer.parseInt(args[1]));
			Remote masterRef = UnicastRemoteObject.exportObject(master, 0);
			Registry registry = LocateRegistry.getRegistry("localhost");
			registry.rebind("mestre", masterRef);
			
			Logger.getLogger("").setLevel(Level.SEVERE);
			System.out.println("obtaining connection factory...");
			com.sun.messaging.ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
			connectionFactory.setProperty(ConnectionConfiguration.imqAddressList,"localhost:7676");	
			System.out.println("obtained connection factory.");
			
			System.out.println("obtaining queues...");
			com.sun.messaging.Queue guessesQueue = new com.sun.messaging.Queue("GuessesQueue");
			System.out.println("obtained queues.");

			JMSContext context = connectionFactory.createContext();
			JMSConsumer consumer = context.createConsumer(guessesQueue);
			
			MessageListener listener = master;
			consumer.setMessageListener(listener);
			
			System.out.println("[System Initiation] Master ready!");
		} catch (RemoteException e) {
			System.err.println("[Error] Error to connect master to server.");
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Guess[] attack(byte[] ciphertext, byte[] knowntext) throws RemoteException {
		int attackNumberLocal = this.getAttackNumber();
		this.incrementAttackNumber();
		
		try (Scanner s = new Scanner(System.in)) {
			this.setCiphertext(ciphertext);
			this.setKnowntext(knowntext);
			long partitionSize = this.getLineNumber() / this.getM();
			long initialwordindex, finalwordindex, modwordindex;
			initialwordindex = 0;
			finalwordindex = this.getLineNumber() / partitionSize;
			modwordindex = this.getLineNumber() % partitionSize;
			
			long keyNumbers = finalwordindex;
			
			Logger.getLogger("").setLevel(Level.SEVERE);
			System.out.println("obtaining connection factory...");
			com.sun.messaging.ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
			connectionFactory.setProperty(ConnectionConfiguration.imqAddressList,"localhost:7676");	
			System.out.println("obtained connection factory.");
			
			System.out.println("obtaining queues...");
			com.sun.messaging.Queue subAttacksQueue = new com.sun.messaging.Queue("SubAttacksQueue");
			System.out.println("obtained queues.");

			JMSContext context = connectionFactory.createContext();
			JMSProducer producer = context.createProducer(); 
			
			AttackFinishThread attackFinishControl = new AttackFinishThread();
			Thread attackFinishControlThread = new Thread(attackFinishControl);
			
			this.putOnAttackFinishControlMap(attackNumberLocal, attackFinishControl);
			
			for (int i = 0; i < partitionSize; i++) {
				MapMessage message = context.createMapMessage(); 

				message.setBytes("cipherText", ciphertext);
				message.setBytes("knownText", knowntext);
				message.setLong("initialWordIndex", initialwordindex);
				if (i == partitionSize - 1)
					finalwordindex += modwordindex;
				message.setLong("finalWordIndex", finalwordindex);
				message.setInt("attackNumber", attackNumberLocal);
				
				synchronized(attackFinishControl) {attackFinishControl.incrementAttack();}
				
				producer.send(subAttacksQueue, message);
				System.out.println("Partition [" + (i + 1) + "] - Sent a message: [" + initialwordindex + ":" + finalwordindex + "]");
				initialwordindex = finalwordindex + 1;
				finalwordindex += keyNumbers;
			}
			
			attackFinishControlThread.start();
			
			attackFinishControlThread.join();

		} catch (JMSException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Guess[] guesses = this.getAttackGuesses(attackNumberLocal);
		System.out.println("[Guess] Total of " + this.getAttackGuesses(attackNumberLocal).length + " guesses found!");
		this.cleanGuessMap(attackNumberLocal);
		return guesses;
	}
	
	@Override
	public void onMessage(Message m) {
		try {
			if (m instanceof MapMessage) {
				int attackNumber = ((MapMessage) m).getIntProperty("attackNumber");
				int numOfGuesses = ((MapMessage) m).getInt("numOfGuesses");
				
				int i;
				
				List<Guess> guesses = new ArrayList<Guess>();
				
				for(i = 0; i < numOfGuesses; i++) {
					long currentindex = ((MapMessage) m).getLong("currentIndex_" + i);
					Guess guess = new Guess();
					guess.setKey( ((MapMessage) m).getString("key_" + i) );
//					guess.setMessage( ((MapMessage) m).getBytes("guess_" + i) );
					String decryptedText = ((MapMessage) m).getString("decryptedText_" + i);
					guess.setMessage(decryptedText.getBytes());
					guesses.add(guess);
					System.out.println("[Candidate Key] AttackNumber: [" + attackNumber + "] Index: [" + currentindex + "]; Key: [" + guess.getKey() + "]");
				}

				this.putOnGuessesMap(attackNumber, guesses);
				this.decrementAttackCounter(attackNumber);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private int linesNumberFile(String fileName) {
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
		return this.guessesMap;
	}
	
	public void putOnGuessesMap(int attackNumber, List<Guess> guesses) {
		if(!this.getGuessesMap().containsKey(attackNumber))
			this.getGuessesMap().put(attackNumber, guesses);
		else
			this.getGuessesMap().get(attackNumber).addAll(guesses);
	}
	
	public Guess[] getAttackGuesses(int attackNumber) {
		Guess[] guesses = new Guess[this.getGuessesMap().get(attackNumber).size()];
		int guessCount = 0;
		for (Guess g : this.getGuessesMap().get(attackNumber)) {
			guesses[guessCount++] = g;
		}
		return guesses;
	}
	
	public void cleanGuessMap(int attackNumber) {
		this.getGuessesMap().get(attackNumber).clear();
		this.getGuessesMap().remove(attackNumber);
	}
	
	public void putOnAttackFinishControlMap(int attackNumber, AttackFinishThread control) {
		this.attackFinishControlMap.put(attackNumber, control);
	}
	
	public AttackFinishThread getAttackFinishThreadFromMap(int attackNumber) {
		return this.attackFinishControlMap.get(attackNumber);
	}
	
	public void decrementAttackCounter(int attackNumber) {
		AttackFinishThread aux = this.attackFinishControlMap.get(attackNumber);
		
		synchronized(aux) {
			aux.decrementAttack();
			aux.notify();
		}
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
	
	public int getM() {
		return this.m;
	}
	
	public void setM(int m) {
		this.m = m;
	}
}
