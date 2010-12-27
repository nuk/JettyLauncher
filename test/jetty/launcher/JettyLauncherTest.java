package jetty.launcher;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.Assert;

import org.apache.http.conn.HttpHostConnectException;
import org.junit.After;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class JettyLauncherTest {

	@After
	public void shutdown() throws Exception{
		JettyLauncher.tearDown();
	}
	
	@Test
	public void should_start_the_server() throws Exception{
		JettyLauncher.main(null);
		testConnection(8080);
	}
	
	@Test
	public void should_start_the_server_at_specific_port() throws Exception{
		JettyLauncher.main(new String[]{"-p","8081"});
		testConnection(8081);
	}
	
	private void testConnection(int port) throws UnknownHostException, IOException {
		Socket s = new Socket("localhost", port);
		Assert.assertTrue(s.isBound());
		Assert.assertTrue(s.isConnected());
		s.close();
	}
	
	@Test
	public void should_start_the_specifyied_application() throws Exception{
		JettyLauncher.main(new String[]{"-app","testFiles/pCookBook-0.1.war"});
		
		WebClient webClient = new WebClient();
		webClient.setTimeout(5000);
	    HtmlPage page = webClient.getPage("http://localhost:8080/pCookBook-0.1");
	    Assert.assertEquals("Receita Listar", page.getTitleText());
	    
	    webClient.closeAllWindows();
	}
	
	@Test
	public void should_start_the_specifyied_application_in_the_specifyied_context_path() throws Exception{
		JettyLauncher.main(new String[]{"-app","testFiles/pCookBook-0.1.war", 
				"-c","/myPath"});
		
		WebClient webClient = new WebClient();
		webClient.setTimeout(5000);
		HtmlPage page = webClient.getPage("http://localhost:8080/myPath");
		Assert.assertEquals("Receita Listar", page.getTitleText());
		
		webClient.closeAllWindows();
	}
	
	@Test
	public void should_kill_server_when_killing_socket_is_used() throws Exception{
		JettyLauncher.main(new String[]{"-app","testFiles/pCookBook-0.1.war"});
		Thread.sleep(500);
		testConnection(8081);
		
		WebClient webClient = new WebClient();
		webClient.setTimeout(5000);
		try {
			webClient.getPage("http://localhost:8080/pCookBook-0.1");
			Assert.fail();
		} catch (HttpHostConnectException e) {}
		
		webClient.closeAllWindows();
	}
}
