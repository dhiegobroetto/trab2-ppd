package br.inf.ufes.ppd;






/**
 * Master.java
 */


import java.rmi.Remote;

public interface Master extends Remote, Attacker {
	// o mestre é um SlaveManager e um Attacker
}
