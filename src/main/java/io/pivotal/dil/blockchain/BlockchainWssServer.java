package io.pivotal.dil.blockchain;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BlockchainWssServer extends WebSocketServer {

	private static final int DEFAULT_PORT = 18080;

	public BlockchainWssServer() {
		this(new InetSocketAddress(DEFAULT_PORT));
	}

	public BlockchainWssServer(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		broadcast("new connection: " + handshake.getResourceDescriptor());
		System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		String msg = conn + " has disconnected";
		broadcast(msg);
		System.out.println(msg);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		broadcast(message);
		System.out.println(conn + ": " + message);
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
		broadcast(message.array());
		System.out.println(conn + ": " + message);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
	}

	@Override
	public void onStart() {
		System.out.println("Server started!");
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					String msg;
					try {
						msg = BlockchainWssClientApplication.TXN_QUEUE.take();
						BlockchainTxn txn = BlockchainTxn.fromJSON(msg);
						String statusMsg = "No BlockchainTxn";
						if (txn != null) {
							statusMsg = "Got a BlockchainTxn (timeAsDate: " + txn.getTimeAsDate().toString() + ")";
						}
						System.out.println(statusMsg); // DEBUG
						System.out.println(txn.toJSON()); // DEBUG
					} catch (InterruptedException | JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					broadcast(msg);
				}
			}
		}).start();
	}

}
