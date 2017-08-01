package nz.ac.auckland.concert.server;

import java.io.Console;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import nz.ac.auckland.concert.common.Config;

public class Server {
	
	public static void main(String[] args) {
		try {		
			final int MAX_CONCERTS = 100;
			
			Registry lookupService = LocateRegistry.createRegistry(Config.REGISTRY_PORT);
					
			
			ConcertServiceServant service = new ConcertServiceServant(MAX_CONCERTS);
					
			lookupService.rebind(Config.SERVICE_NAME, service);
			
			Console c = System.console();
			c.readLine("Press Enter to shutdown the server ");
			lookupService.unbind(Config.SERVICE_NAME);
			
			System.out.println(
					"The Concert service is no longer bound in the RMI registry. " 
					+ "Waiting for lease to expire.");
			
		} catch(RemoteException e) {
			System.out.println("Unable to start or register proxy with the RMI Registry");
			e.printStackTrace();
		} catch(NotBoundException e) {
			System.out.println("Unable to remove proxy from the RMI Registry");
		}
	}
}