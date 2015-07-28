package org.yetiz;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yeti on 15/7/27.
 */
public class EchoServer {
	Selector selector;
	ServerSocketChannel serverSocketChannel;

	public static void main(String... args) {
		new EchoServer();
	}

	public EchoServer() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			new Thread(new Runnable() {
				@Override
				public void run() {
					AtomicLong atomicLong = new AtomicLong(0);
					try {
						serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 6655));
						serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
						while (true) {
							if (selector.select(1000) == 0)
								continue;
							Set<SelectionKey> keys = selector.selectedKeys();
							for (SelectionKey key : keys) {
								keys.remove(key);
								if (key.isAcceptable()) {
									long id = atomicLong.getAndIncrement();
									SocketChannel socketChannel = serverSocketChannel.accept();
									socketChannel.configureBlocking(false);
									socketChannel.register(selector, SelectionKey.OP_READ, id);
									System.out.println(String.format("%d: Linked", id));
								}
								if (key.isReadable()) {
									ByteBuffer buffer = ByteBuffer.allocate(1024);
									SocketChannel socketChannel = ((SocketChannel) key.channel());
									long id = ((long) key.attachment());
									int length = socketChannel.read(buffer);
									if (length < 0) {
										key.cancel();
										socketChannel.close();
										continue;
									}
									byte[] from = new String(buffer.array()).toUpperCase().getBytes();
									length = from.length;
									byte[] data = new byte[length];
									for (int i = 0; i < length; i++) {
										data[i] = from[i];
									}
									buffer = ByteBuffer.wrap(data);
									System.out.println(String.format("%d: %s", id, new String(data)));
//									buffer.flip();
									socketChannel.write(buffer);
								}
							}
						}
					} catch (IOException e) {
					}
				}
			}).start();
		} catch (IOException e) {
		}
	}
}
