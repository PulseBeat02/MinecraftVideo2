package io.github.bananapuncher714.minecraftvideo.httpd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copied over from https://resources.oreilly.com/examples/9780596002855/blob/master/CD-ROM/examples/ch12/TinyHttpd.java
 * A mini http daemon intended to serve client resource pack requests.
 * 
 * @author BananaPuncher714
 */
public class MineHttpd extends Thread {
	private volatile boolean running = true;

	protected final int port;
	protected final ServerSocket socket;

	public MineHttpd( int port ) throws IOException  {
		this.port = port;
		socket = new ServerSocket( port );
		socket.setReuseAddress( true );
	}

	@Override
	public void run() {
		while ( running ) {
			try {
				new Thread( new MineConnection( this, socket.accept() ) ).start();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		if ( !socket.isClosed() ) {
			try {
				socket.close();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}

	public void terminate() {
		running = false;
		if ( !socket.isClosed() ) {
			try {
				socket.close();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Executes whenever a client requests something
	 * 
	 * @param request
	 * The path to the file requested
	 * @return
	 * A file to return to them; null if the file they requested is invalid
	 */
	public File requestFileCallback( String request ) {
		return null;
	}

	public class MineConnection implements Runnable {
		protected final MineHttpd server;
		protected final Socket client;

		public MineConnection( MineHttpd server, Socket client ) {
			this.server = server;
			this.client = client;
		}

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream(), "8859_1" ) );
				OutputStream out = client.getOutputStream();
				PrintWriter pout = new PrintWriter( new OutputStreamWriter( out, "8859_1" ), true );
				String request = in.readLine();
				System.out.println( "Recieved request '" + request + "' from " + client.getInetAddress() );

				Matcher get = Pattern.compile("GET /?(\\S*).*").matcher( request );
				if ( get.matches() ) {
					request = get.group(1);
					File result = requestFileCallback( request );
					if ( result == null ) {
						pout.println( "HTTP/1.0 400 Bad Request" );
					} else {
						System.out.println( "Request '" + request + "' is being served to " + client.getInetAddress() );
						try {
							// Writes zip files specifically; Designed for resource pack hosting
							
							Calendar c = Calendar.getInstance(); 
							SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
							String GMTSTRING = df.format(c.getTime()) + " GMT";
							
							out.write( "HTTP/1.0 200 OK\r\n".getBytes() );
							out.write( "Content-Type: application/zip\r\n".getBytes() );
							out.write( ( "Content-Length: " + result.length() + "\r\n" ).getBytes() );
							out.write( ( "Date: " + GMTSTRING + "\r\n" ).getBytes() );
							out.write( "Server: MineHttpd\r\n\r\n".getBytes() );
							FileInputStream fis = new FileInputStream ( result );
							byte [] data = new byte [ 64*1024 ];
							for( int read; ( read = fis.read( data ) ) > -1; ) {
								out.write( data, 0, read );
							}
							out.flush();
							fis.close();
							System.out.println( "Successfully served '" + request + "' to " + client.getInetAddress() );
						} catch ( FileNotFoundException e ) {
							pout.println( "HTTP/1.0 404 Object Not Found" );
						}
					}
				} else {
					pout.println( "HTTP/1.0 400 Bad Request" );
				}
				client.close();
			} catch ( IOException e ) {
				System.out.println( "I/O error " + e );
			}
		}
	}
}
