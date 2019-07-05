package br.inf.ufes.ppd.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import br.inf.ufes.ppd.Decrypt;
import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.Master;

public class ClientExecute {
	public static void main(String[] args) {
		try {
			Guess[] guesses = null;
			byte[] ciphertext = null;
			if (args.length < 3) {
				int fileNumber = 1; // Número do arquivo, alterar quando iniciar novos escravos.
				PrintWriter writer = new PrintWriter("result" + args[2] + "Slave.csv", "UTF-8");
				for (int i = 1; i <= 10; i++) {
					byte[] knowntext = args[1].getBytes();
					Registry registry = LocateRegistry.getRegistry(args[0]);
					Master master = (Master) registry.lookup("mestre");
					ciphertext = Decrypt.readFile("../files/" + (i * 10000) + ".cipher");
					long initialTime = System.nanoTime();				
					guesses = master.attack(ciphertext, knowntext);
					long finalTime = System.nanoTime();
					long totalTime = finalTime - initialTime;
					double secondsTime = (totalTime / 1_000_000_000.0);
					System.out.println("File no." + i + "; Total time: " + secondsTime + " seconds.");
					writer.println("Time;" + secondsTime + ";Nano;" + totalTime + ";");
				}
				writer.close();
				for (Guess guess : guesses) {
					if (guess == null)
						break;
					Decrypt.saveFile(guess.getKey() + ".msg", guess.getMessage());
				}
				System.exit(0);
			} else {
				try {
					ciphertext = Decrypt.readFile(args[2]);
				} catch (FileNotFoundException e) {
					Random rd = new Random();
					int fileSize;
					if (args.length < 4)
						fileSize = rd.nextInt(99000) + 1000;
					else
						fileSize = Integer.parseInt(args[3]);
					fileSize -= fileSize % 8;
					byte[] file = new byte[fileSize];
					rd.nextBytes(file);
					ciphertext = file;
				}
			}
			Registry registry = LocateRegistry.getRegistry(args[0]);
			Master master = (Master) registry.lookup("mestre");
			byte[] knowntext = args[1].getBytes();
			
			// Initial Time
			long initialTime = System.nanoTime();
			
			// Attack
			guesses = master.attack(ciphertext, knowntext);
			
			// Final Time
			long finalTime = System.nanoTime();
			
			// Total time
			long totalTime = finalTime - initialTime;
			double secondsTime = (double) (totalTime / 1_000_000_000.0);
			System.out.println("Total time: " + secondsTime + " seconds.");
			
			
			for (Guess guess : guesses) {
				if (guess == null)
					break;
				System.out.println("[Candidate Key] Key: [" + guess.getKey() + "]");
				Decrypt.saveFile(guess.getKey() + ".msg", guess.getMessage());
			}
		} catch (RemoteException e) {
			System.out.println("[Error] RemoteException in ClientExecute.");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("[Error] NotBoundException in ClientExecute.");
		} catch (IOException e) {
			System.out.println("[Error] IOException in ClientExecute.");
			e.printStackTrace();
		}
	}
}
