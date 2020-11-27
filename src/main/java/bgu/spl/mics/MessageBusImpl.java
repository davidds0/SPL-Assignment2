package bgu.spl.mics;


import jdk.internal.net.http.common.Pair;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private HashMap<String,Queue<Message>> MapOfMicroService;
	private HashMap<Class<? extends Event>,Queue<MicroService>> MapOfEvents;
	private HashMap<Class<? extends Broadcast>,Queue<MicroService>> MapOfBroadcast;

	//public MessageBusImpl(){}; // constructor TODO check if it necessary to write a default constructor 
	
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if( MapOfMicroService.containsKey(m.getName())){
			Queue<MicroService> queue= new PriorityQueue<>();
			MapOfEvents.put(type, queue);
		}
		else System.out.println("need to register before subscribe");
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if( MapOfMicroService.containsKey(m.getName())){
			Queue<MicroService> queue= new PriorityQueue<>();
			MapOfBroadcast.put(type, queue);
		}
		else System.out.println("need to register before subscribe");
    }

	@Override
	public <T> void complete(Event<T> e, T result) {
		e.getFuture().resolve(result);
		
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		Queue<MicroService> queueOfBroadcast= MapOfBroadcast.get(b);
		for (MicroService a :queueOfBroadcast ) {
			MapOfMicroService.get(a.getName()).add(b);
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Queue<MicroService> queueOfEvent= MapOfEvents.get(e);
		MicroService first= queueOfEvent.poll();               // round-robin implement
		queueOfEvent.add(first);                               // round-robin implement
		MapOfMicroService.get(first.getName()).add(e);
		Future<T> result=e.getFuture();
        return result;
	}

	@Override
	public void register(MicroService m) {
		Queue<Message> queue= new PriorityQueue<Message>();
		MapOfMicroService.put(m.getName(), queue);
	}

	@Override
	public void unregister(MicroService m) {
		MapOfMicroService.remove(m.getName());
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {// TODO check where to throw exception
		while(MapOfMicroService.get(m.getName()).isEmpty()){};// this call is blocking
		return MapOfMicroService.get(m.getName()).poll();
	}
}
