package nz.ac.auckland.concert.common;

public class Config {
		// Port number that the RMI registry will use.
		public static final int REGISTRY_PORT = 8090;
		
		// Name used to advertise/register the concert service in the RMI
		// Registry.
		public static final String SERVICE_NAME = "concert-service";
}
