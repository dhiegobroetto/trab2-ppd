package br.inf.ufes.ppd.master;

public class AttackFinishThread implements Runnable{
	
	private int onGoingAttacks; 
	
	public AttackFinishThread() {
		this.onGoingAttacks = 0;
	}

	public int getOnGoingAttacks() {
		return this.onGoingAttacks;
	}
	
	public boolean isFinished() {
		return (this.onGoingAttacks == 0);
	}

	public void incrementAttack() {
		this.onGoingAttacks++;
	}
	
	public void decrementAttack(){
		this.onGoingAttacks--;
	}

	@Override
	public void run() {
		try {
			while(true) {
				synchronized(this) { wait(); }
				if(this.isFinished()) break;
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted Exception no AttackFinishThread");
			e.printStackTrace();
		}
		
	}
}
