package nz.ac.auckland.concert.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import nz.ac.auckland.concert.common.Concert;
import nz.ac.auckland.concert.common.ConcertService;
import nz.ac.auckland.concert.common.Config;
import nz.ac.auckland.concert.common.FullException;

import org.joda.time.DateTime;


public class Client {
	
	private static ConcertService _proxy;
	
	@BeforeClass
	public static void getProxy() {
		try {
			// Instantiate a proxy object for the RMI Registry, expected to be
			// running on the local machine and on a specified port. 
			Registry lookupService = LocateRegistry.getRegistry("localhost", Config.REGISTRY_PORT);
			
			// Retrieve a proxy object representing the ShapeFactory.
			_proxy = (ConcertService)lookupService.lookup(Config.SERVICE_NAME);
		} catch (RemoteException e) {
			System.out.println("Unable to connect to the RMI Registry");
		} catch (NotBoundException e) {
			System.out.println("Unable to acquire a proxy for the Concert service");
		}
	}
	
	@Test
	public void testCreate() throws RemoteException {
		try {
			
			// Clear the proxy before testing, may cause error later on
			_proxy.clear();
			
			Concert ConcertA = _proxy.createConcert(new Concert("The Rolling Stones", new DateTime(2017, 7, 20, 8, 10)));
			Concert ConcertB = _proxy.createConcert(new Concert("Roger Waters", new DateTime(2017, 7, 25, 13, 30)));
			Concert ConcertC = _proxy.createConcert(new Concert("One Direction", new DateTime(2017, 9, 10, 18, 20)));

			System.out.println("ConcertA's Id is " + ConcertA .getId());
			System.out.println("ConcertB's Id is " + ConcertB .getId());
			System.out.println("ConcertC's Id is " + ConcertC .getId());
		
			// Query the remote factory.
			List<Concert> remoteConcerts = _proxy.getAllConcerts();
			
			assertTrue(remoteConcerts.contains(ConcertA));
			assertTrue(remoteConcerts.contains(ConcertB));
			assertTrue(remoteConcerts.contains(ConcertC));
			assertFalse(ConcertA.equals(ConcertB));
			assertTrue(ConcertB.equals(_proxy.getConcert(ConcertB.getId())));
			assertEquals(3, remoteConcerts.size());
			
			for(Concert c : remoteConcerts) {
				System.out.println(c.getId().toString() + " " + c.getTitle().toString() + " " + c.getDate().toString());
			}

			
		} catch(FullException e) {
			fail();
		}
	}
	
	
	
	@Test
	public void testGetConcert() throws RemoteException {
		
		try {
			
			Concert ConcertD = _proxy.createConcert(new Concert("Farewell Tour", new DateTime(2018, 7, 20, 8, 10)));
			Concert ConcertE = _proxy.createConcert(new Concert("Mayday Tour", new DateTime(2018, 5, 12, 12, 10)));
			
			Concert StoredConcertD = _proxy.getConcert(new Long(ConcertD.getId()));
			Concert StoredConcertE = _proxy.getConcert(new Long(ConcertE.getId()));
		
			assertTrue(StoredConcertD.getTitle().equals(ConcertD.getTitle()));
			assertTrue(StoredConcertD.getDate().equals(ConcertD.getDate()));
			assertFalse(StoredConcertE.getTitle().equals(ConcertD.getTitle()));
			assertFalse(StoredConcertE.getDate().equals(ConcertD.getDate()));
			
		} catch(FullException e) {
			fail();
		}
		
	}
	
	@Test
	public void testUpdateConcertTrue() throws RemoteException {
		
		try {
			
			Concert ConcertA = _proxy.createConcert(new Concert("Edge", new DateTime(2018, 1, 1, 13, 45)));
			Long id = new Long(ConcertA.getId());
			Concert ConcertB = new Concert(id,"Edge", new DateTime(2018, 4, 1, 13, 45));
			
			assertTrue(_proxy.updateConcert(ConcertB));
			Concert StoredConcertB = _proxy.getConcert(id);
			assertEquals(StoredConcertB.getTitle(), "Edge");
			assertEquals(StoredConcertB.getDate(), new DateTime(2018, 4, 1, 13, 45));
			
		} catch(FullException e) {
			fail();
		}
		
	}
	
	@Test
	public void testUpdateConcertFalse() throws RemoteException {
		
		try {
			
			Concert ConcertA = _proxy.createConcert(new Concert("Hang Over", new DateTime(2017, 1, 27, 11, 45)));
			Long id = new Long(ConcertA.getId() + 1);
			Concert Concert = new Concert(id,"Hang Over", new DateTime(2017, 3, 27, 11, 45));
			
			assertFalse(_proxy.updateConcert(Concert));
			
		} catch(FullException e) {
			fail();
		}
		
	}
	
	
	@Test
	public void testDeleteConcertTrue() throws RemoteException {
		
		try {
			
			Concert newConcertA = _proxy.createConcert(new Concert("Hell Freezes Over", new DateTime(2017, 11, 27, 20, 45)));
			_proxy.createConcert(new Concert("Madhouse Rock", new DateTime(2019, 12, 11, 11, 45)));
		
			Long id = new Long(newConcertA.getId());
			assertTrue(_proxy.deleteConcert(id));
			
			List<Concert> remoteConcerts = _proxy.getAllConcerts();
			assertFalse(remoteConcerts.contains(newConcertA));
			
			
		} catch(FullException e) {
			fail();
		}
		
	}
	
	
	@Test
	public void testDeleteConcertFalse() throws RemoteException {
		
		try {
			Concert Concert = _proxy.createConcert(new Concert("The Sam and Dave Tour", new DateTime(2019, 11, 20, 21, 45)));
			List<Concert> remoteConcerts = _proxy.getAllConcerts();
			int originalSize = remoteConcerts.size();
			
			Long id = new Long(Concert.getId() + 5);
			assertFalse(_proxy.deleteConcert(id));
			
			remoteConcerts = _proxy.getAllConcerts();
			assertEquals(originalSize, remoteConcerts.size());
			
		} catch (FullException e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void testClear() throws RemoteException {
		
		try {
			
			_proxy.createConcert(new Concert("Ratt Poison", new DateTime(2017, 7, 21, 9, 30)));
			
			List<Concert> remoteConcerts = _proxy.getAllConcerts();
			_proxy.clear();
			remoteConcerts = _proxy.getAllConcerts();
			assertEquals(0, remoteConcerts.size());
			
		} catch (FullException e) {
			e.printStackTrace();
		}

	}
	
}
