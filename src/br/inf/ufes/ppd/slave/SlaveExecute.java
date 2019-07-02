package br.inf.ufes.ppd.slave;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.*;

import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.ConnectionFactory;
import com.sun.messaging.Queue;

import br.inf.ufes.ppd.Decrypt;
import br.inf.ufes.ppd.Guess;

public class SlaveExecute {

	protected String fileName;
	protected String slaveName;
	protected Queue guessesQueue;
	protected JMSContext context;

	public SlaveExecute(String fileName, String slaveName, Queue guessesQueue, JMSContext context) {
		this.fileName = fileName;
		this.slaveName = slaveName;
		this.guessesQueue = guessesQueue;
		this.context = context;
	}

	public static void main(String[] args) {
		Logger.getLogger("").setLevel(Level.INFO);

		System.out.println("[System Factory] Obtaining connection factory...");
		ConnectionFactory connectionFactory = new ConnectionFactory();
		try {
			connectionFactory.setProperty(ConnectionConfiguration.imqAddressList, args[2] + ":7676");
			System.out.println("[System Factory] Obtained connection factory.");

			System.out.println("[System Queue] Obtaining queues...");
			Queue subAttacksQueue = new Queue("SubAttacksQueue");
			Queue guessesQueue = new Queue("GuessesQueue");
			System.out.println("[System Queue] Obtained queues.");

			JMSContext context = connectionFactory.createContext();
			JMSConsumer consumer = context.createConsumer(subAttacksQueue);
			SlaveExecute slave = new SlaveExecute(args[0], args[1], guessesQueue, context);
			while (true) {
				Message m = consumer.receive();
				if (m instanceof MapMessage) {
					slave.sentMessage(m);
				}
				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sentMessage(Message m) {

		try {
			long initialwordindex = ((MapMessage) m).getLong("initialWordIndex");
			long finalwordindex = ((MapMessage) m).getLong("finalWordIndex");
			int attacknumber = ((MapMessage) m).getInt("attackNumber");
			byte[] ciphertext = ((MapMessage) m).getBytes("cipherText");
			byte[] knowntext = ((MapMessage) m).getBytes("knownText");
			int partition = ((MapMessage) m).getInt("partition");

			Scanner scanner = new Scanner(new File(getFileName()));

			for (int i = 0; i < initialwordindex && scanner.hasNextLine(); i++) {
				scanner.nextLine();
			}

			this.startSubAttack(ciphertext, knowntext, initialwordindex, finalwordindex, attacknumber, partition,
					scanner);
		} catch (FileNotFoundException e) {
			System.out.println("File not found in SlaveExecute");
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	public void startSubAttack(byte[] ciphertext, byte[] knowntextbyte, long initialwordindex, long finalwordindex,
			int attacknumber, int partition, Scanner scanner) {
		String knowntext = new String(knowntextbyte);

		try {
			JMSProducer producer = this.getContext().createProducer();

			System.out.println("[Slave Attack] Attack: [" + attacknumber + ":" + partition + "] Index: ["
					+ initialwordindex + ";" + finalwordindex + "] has begun...");

			long i = 0;
			int guessCount = 0;
			long j = finalwordindex - initialwordindex;

			MapMessage message = this.getContext().createMapMessage();

			message.setIntProperty("attackNumber", attacknumber);
			message.setInt("partition", partition);
			message.setString("slaveName", this.getSlaveName());

			while (scanner.hasNextLine()) {
				if (i > j)
					break;

				String key = scanner.nextLine();
				Guess guess = new Guess();
				guess.setMessage(Decrypt.decryptFile(ciphertext, key));
				if (guess.getMessage() != null) {
					String decryptedText = new String(guess.getMessage());
					if (decryptedText.indexOf(knowntext) != -1) {
						guess.setKey(key);
						System.out.println("[Candidate Key] Attack: [" + attacknumber + ":" + partition + "] Index: ["
								+ (initialwordindex + i) + "]; Key: [" + key + "] - Sent a message in [GuessesQueue]");
						message.setLong("currentIndex_" + Integer.toString(guessCount), i);
						message.setBytes("guess_" + Integer.toString(guessCount), guess.getMessage());
						message.setString("key_" + Integer.toString(guessCount), key);
						guessCount++;
					}
				}
				i++;
			}
			message.setInt("numOfGuesses", guessCount);

			producer.send(this.getGuessesQueue(), message);
		} catch (JMSException e) {
			e.printStackTrace();
		}

		scanner.close();

		System.out.println("[Slave Attack] Attack: [" + attacknumber + ":" + partition + "] Index: [" + initialwordindex
				+ ";" + finalwordindex + "] finished!");
	}

	public String getFileName() {
		return fileName;
	}

	public String getSlaveName() {
		return slaveName;
	}

	public Queue getGuessesQueue() {
		return guessesQueue;
	}

	public JMSContext getContext() {
		return context;
	}

}
