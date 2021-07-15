package no.nav.brevserver.server.comptest.stabilitet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import no.nav.brevserver.server.comptest.AbstractComponentTest;
import no.nav.brevserver.server.comptest.util.MessageHandler;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

public class MQStabTest extends AbstractComponentTest {

	
	@Autowired
	private MessageHandler messageHandler;
	
	private final String arkivQueue = "queue:///QA.T475.BREVSERVER_ONLINEBREV?targetClient=1";
	
	private final String XML_FILE = "C:/Temp/MQXML/BI-3333-meldinger.xml";
	
	private final Long intervall = new Long(9000); //millisekunder
	
	String[] fileList;
	List<Thread> threads;
	int counter = 0;
	int startRef = 30000;
	
	boolean finished = false;
	
	
	@Ignore
	@Test
	public void runTest() throws Exception{

		readFileAndExtractMessages();
		sendMessagesToMQ();
		
		
		while (!finished) {
			Thread.sleep(2000);
		}
	}
	
	
	
	
	
	
	//Leser XML filen og plukker ut meldingene
	public void readFileAndExtractMessages(){
		
		System.out.println("LESER XML MAPPEN...");
		
		File msgs = new File(XML_FILE);
		
		try {
			String fileData = FileUtils.readFileToString(msgs, "ISO-8859-1");
			fileList = fileData.split("#@#@#");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("...OK");

	}
	
	
	
	
	//Sender meldingene på kø
	public void sendMessagesToMQ(){
		
		System.out.println("STARTERåSENDE MELDINGER TIL kø MED ET INTERVALL på "+intervall/1000+" SEKUNDER");
		
		final Timer timer = new Timer();
		
		TimerTask senderTimerTask = new TimerTask(){
			public void run(){
				System.out.println("Counter " + counter);
				System.out.println("Filelist " + fileList.length);
				
				//Sjekk om listen er ferdiglest
				if(fileList.length <= counter){
					System.out.println("ALLE MELDINGENE ER SENDT TIL kø. Intervall stoppet.");
					timer.cancel();
//					System.exit(0);
					finished = true;
				}else{
					//Start ny tråd
					System.out.println("Ny tråd...");
					
					String message = fileList[counter];
					
					
					messageHandler.sendByteMessage(arkivQueue, message.getBytes());
					System.out.println(counter+". melding sendt.");
					
					counter++;	
				}
			}
		};
		
		timer.scheduleAtFixedRate(senderTimerTask, 0, intervall);
	}
	
	
	
}
