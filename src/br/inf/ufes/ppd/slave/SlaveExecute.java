package br.inf.ufes.ppd.slave;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.*;

import com.sun.messaging.ConnectionConfiguration;

import br.inf.ufes.ppd.Decrypt;
import br.inf.ufes.ppd.Guess;

public class SlaveExecute implements MessageListener {

	// protected UUID slaveKey;
	protected String fileName;
	// protected String slaveName;

	public SlaveExecute(String fileName) {
		// this.slaveKey = slaveKey;
		// this.slaveName = slaveName;
		this.fileName = fileName;
	}

	public static void main(String[] args) {
		Logger.getLogger("").setLevel(Level.INFO);

		System.out.println("obtaining connection factory...");
		com.sun.messaging.ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
		try {
			connectionFactory.setProperty(ConnectionConfiguration.imqAddressList, "localhost:7676");
			System.out.println("obtained connection factory.");

			System.out.println("obtaining queues...");
			com.sun.messaging.Queue subAttacksQueue = new com.sun.messaging.Queue("SubAttacksQueue");
			com.sun.messaging.Queue guessesQueue = new com.sun.messaging.Queue("GuessesQueue");
			System.out.println("obtained queues.");

			JMSContext context = connectionFactory.createContext();

			JMSConsumer consumer = context.createConsumer(subAttacksQueue);

			MessageListener listener = new SlaveExecute(args[0]);
			consumer.setMessageListener(listener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message m) {
		try {
			if (m instanceof MapMessage) {
				try {
					long initialwordindex = ((MapMessage) m).getLong("initialWordIndex");
					long finalwordindex = ((MapMessage) m).getLong("finalWordIndex");
					int attacknumber = ((MapMessage) m).getInt("attackNumber");
					byte[] ciphertext = ((MapMessage) m).getBytes("cipherText");
					byte[] knowntext = ((MapMessage) m).getBytes("knownText");
					Scanner scanner = new Scanner(new File(getFileName()));
					for (int i = 0; i < initialwordindex && scanner.hasNextLine(); i++) {
						scanner.nextLine();
					}
					System.out.println("[System Attack] Attack no." + attacknumber + " has begun!");
					System.out.println("[Slave Index] Attack: [" + attacknumber + "] Index: [" + initialwordindex + ";"
							+ finalwordindex + "]");
					this.startSubAttack(ciphertext, knowntext, initialwordindex, finalwordindex, attacknumber, scanner);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				System.out.print("\nreceived message: ");
				System.out.println(((MapMessage) m).getLong("finalWordIndex"));
				System.out.print("enter your message:");
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void startSubAttack(byte[] ciphertext, byte[] knowntextbyte, long initialwordindex, long finalwordindex,
			int attacknumber, Scanner scanner) {
		String knowntext = new String(knowntextbyte);

		long i = 0;
		long j = finalwordindex - initialwordindex;

		while (scanner.hasNextLine()) {
			if (i >= j)
				break;
			i++;
			String key = scanner.nextLine();
			Guess guess = new Guess();
			guess.setMessage(Decrypt.decryptFile(ciphertext, key));
			if (guess.getMessage() != null) {
				String decryptedText = new String(guess.getMessage());
				if (decryptedText.indexOf(knowntext) != -1) {
					guess.setKey(key);
					System.out.println("[Candidate Key] attackNumber: [" + attacknumber + "] Index: ["
							+ (initialwordindex + i - 1) + "]; Key: [" + key + "]");
				}
			}
		}

		scanner.close();

		System.out.println("[Attack] Attack no." + attacknumber + " finished!");

	}

	// public UUID getSlaveKey() {
	// return slaveKey;
	// }
	//
	public String getFileName() {
		return fileName;
	}
	//
	// public String getSlaveName() {
	// return slaveName;
	// }

}
