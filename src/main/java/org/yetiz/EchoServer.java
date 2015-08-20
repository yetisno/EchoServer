package org.yetiz;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yeti on 15/7/27.
 */
public class EchoServer {
	Selector selector;
	ServerSocketChannel serverSocketChannel;
	AtomicLong count = new AtomicLong(1);

	public EchoServer() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			new Thread(new Runnable() {
				@Override
				public void run() {
					AtomicLong atomicLong = new AtomicLong(0);
					ByteBuffer buffer = null;
					try {
						serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 10315));
						serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
						while (true) {
							if (selector.select(1000) == 0)
								continue;
							Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
							while (keys.hasNext()) {
								SelectionKey key = keys.next();
								if (key.isAcceptable()) {
									long id = atomicLong.getAndIncrement();
									SocketChannel socketChannel = serverSocketChannel.accept();
									socketChannel.configureBlocking(false);
									socketChannel.register(selector, SelectionKey.OP_READ, id);
									System.out.println(String.format("%d: Linked", id));
								}
								if (key.isReadable()) {
									try {
										buffer = ByteBuffer.allocate(10240);
										SocketChannel socketChannel = ((SocketChannel) key.channel());
										long id = ((long) key.attachment());
										int length = socketChannel.read(buffer);
										if (length <= 0) {
											key.cancel();
											socketChannel.close();
											continue;
										}
										socketChannel.register(selector, SelectionKey.OP_WRITE, id);
									} catch (Throwable t) {
									}
								}
								if (key.isWritable()) {
									try {
										buffer.flip();
										SocketChannel socketChannel = ((SocketChannel) key.channel());
										long id = ((long) key.attachment());
										socketChannel.write(buffer);
										System.out.println(String.format("%d: %s %s", id, buffer.limit(), count.getAndIncrement()));
										socketChannel.register(selector, SelectionKey.OP_READ, id);
									} catch (Throwable t) {
									}
								}
								keys.remove();
							}
						}
					} catch (IOException e) {
					}
				}
			}).start();
		} catch (IOException e) {
		}
	}

	public static void main(String... args) {
		new EchoServer();
	}
}
