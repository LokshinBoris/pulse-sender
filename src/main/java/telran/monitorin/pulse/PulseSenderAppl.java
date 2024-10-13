package telran.monitorin.pulse;
import java.net.*;
import java.util.*;
import java.util.Random;
import java.util.stream.IntStream;

import telran.monitorin.pulse.dto.SensorData;
public class PulseSenderAppl
{
	private static final int N_PACKETS = 100;
	private static final long TIMEOUT = 500;
	private static final int N_PACIENT = 5;
	private static final int MIN_PULSE_VALUE = 50;
	private static final int MAX_PULSE_VALUE = 200;
	private static final String HOST = "localhost";
	private static final int PORT = 5000;
	private static final int MIN_JUMP_PERCENT = 5;
	private static final int MAX_JUMP_PERCENT = 30;
	private static final int JUMP_POSITIVE_PROBABILITY = 50;
	private static final int JUMP_PROBABILITY = 80;
	private static Map<Long,Integer> history=new HashMap();
	static DatagramSocket socket;
	static Random random=new Random();
	public static void main(String[] args) throws Exception 
	{
		socket=new DatagramSocket();
		IntStream.rangeClosed(1,N_PACKETS).forEach(PulseSenderAppl::sendPulse);

	}
	
	/**
	 * @param seqNumber
	 */
	static void sendPulse(int seqNumber)
	{
		SensorData data=getRandomSensorData(seqNumber);
		String jsonData=data.toString();
		sendDatagramPacket(jsonData);
		try
		{ 
			Thread.sleep(TIMEOUT);
		}
		catch (InterruptedException e) 
		{
		  
		 }
 	}

	private static void sendDatagramPacket(String jsonData)
	{
		byte[] buffer = jsonData.getBytes();
		try 
		{
			DatagramPacket packet=new DatagramPacket(buffer, buffer.length,InetAddress.getByName(HOST),PORT);
			socket.send(packet);
		}
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		
	}

	private static SensorData getRandomSensorData(int seqNumber) 
	{
		long patientId=random.nextInt(1, N_PACIENT+1);
		int value = getRandomPulseValue(patientId);
		return new SensorData(seqNumber,patientId,value,System.currentTimeMillis());
	}

	private static int getRandomPulseValue(long patientId)
	{
		int res=0;

		Integer lastValue=history.get(patientId);
		if(lastValue==null) res=random.nextInt(MIN_PULSE_VALUE,MAX_PULSE_VALUE);
		else
		{
			if(!isJumpProbability()) res=lastValue;
			else
			{
				int sign=getSignJump();
				int persent=getPercentJump();
				res=(int)(lastValue*(1+sign*persent/100.));
				if(res>MAX_PULSE_VALUE) res=MAX_PULSE_VALUE;
				else if (res<MIN_PULSE_VALUE) res=MIN_PULSE_VALUE;
			}
		}
		history.put(patientId,res);
		return res;
	}

	private static int getPercentJump() 
	{
		return random.nextInt(MIN_JUMP_PERCENT, MAX_JUMP_PERCENT);
	}

	private static int getSignJump()
	{
		return random.nextInt(0,100)>=JUMP_POSITIVE_PROBABILITY? 1:-1;
	}

	private static boolean isJumpProbability() 
	{
		return random.nextInt(0,100)<JUMP_PROBABILITY;
	}
}
