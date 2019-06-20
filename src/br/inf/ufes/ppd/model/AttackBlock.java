package br.inf.ufes.ppd.model;

public class AttackBlock {
	private long initialwordindex;
	private long finalwordindex;
	
	public AttackBlock(long initialwordindex, long finalwordindex) {
		this.initialwordindex = initialwordindex;
		this.finalwordindex = finalwordindex;
	}
	
	public AttackBlock(AttackBlock attackBlock) {
		this.initialwordindex = attackBlock.getInitialwordindex();
		this.finalwordindex = attackBlock.getFinalwordindex();
	}
	
	public long getFinalwordindex() {
		return finalwordindex;
	}
	
	public void setFinalwordindex(long finalwordindex) {
		this.finalwordindex = finalwordindex;
	}
	
	public long getInitialwordindex() {
		return initialwordindex;
	}
	
	public void setInitialwordindex(long initialwordindex) {
		this.initialwordindex = initialwordindex;
	}
	
	public boolean isAttackDone(){
		return this.initialwordindex == this.finalwordindex;
	}
}
