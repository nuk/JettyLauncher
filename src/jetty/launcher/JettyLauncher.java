package jetty.launcher;

import java.net.ServerSocket;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class JettyLauncher {

	private Server server;
	private ServerSocket killingSocket;
	private Integer jettyPort;
	
	private static JettyLauncher launcher;


	public static void main(String [] args) throws Exception {
		launcher = new JettyLauncher(args);
		launcher.launch();
	}

	public static void tearDown() throws Exception {
		launcher.stop();
	}
	
	private void launch() throws Exception {
		server.start();
		Thread t = new Thread(){
			public void run() {
				try {
					killingSocket = new ServerSocket(jettyPort+1);
					killingSocket.accept();
					JettyLauncher.launcher.stop();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		t.start();
	}
	
	private void stop() throws Exception {
		server.stop();
		server.destroy();
		killingSocket.close();
	}

	public JettyLauncher(String [] args) {
		init(args);
	}
	
	private void init (String [] args){
		server = new Server();
		initPort(getArgs(args,"-p"));
		initAppParam(args, getArgs(args,"-app"));
	}

	private void initAppParam(String[] args, String appParam) {
		if (appParam != null ){
			WebAppContext context = new WebAppContext();
			context.setServer(server);
			context.setWar(appParam);
			initContextParam(appParam, context, getArgs(args,"-c"));
			server.setHandler(context);
		}
	}

	private void initContextParam(String appParam, WebAppContext context,
			String contextParam) {
		if (contextParam != null){
			context.setContextPath(contextParam);
		}else{
			String pathName = appParam
								.substring(appParam.lastIndexOf('/'));
			pathName = pathName.substring(0, pathName.lastIndexOf('.'));
			context.setContextPath(pathName);
		}
	}

	private void initPort(String portParam) {
		SocketConnector connector = new SocketConnector();
		if (portParam != null){
			jettyPort = Integer.parseInt(portParam);
		}else{
			jettyPort = 8080;
		}
		connector.setPort(jettyPort);
		server.setConnectors(new Connector[] { connector });
	}
	
	private static String getArgs(String [] args, String key){
		if (args != null){
			for (int i = 0; i < args.length; i++){
				if (args[i].equals(key) && i+1 < args.length){
					return args[i+1];
				}
			}
		}
		return null;
	}
}
