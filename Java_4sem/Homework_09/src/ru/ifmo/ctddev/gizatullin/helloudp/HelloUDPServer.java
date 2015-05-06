package ru.ifmo.ctddev.gizatullin.helloudp;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 5/6/15.
 */
public class HelloUDPServer implements HelloServer {
    private static final int BUFFER_SIZE = 256;
    private static final String USAGE = "Port number_of_threads";
    private ExecutorService service;
    private DatagramSocket receivingSocket;

    public static void main(String[] args) {
        if (args == null || args.length < 2 || Arrays.stream(args).anyMatch(Predicate.isEqual(null))) {
            System.out.println(USAGE);
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            if (port < 0 || port > 0xFFFF || threads < 1) {
                throw new NumberFormatException();
            }
            new HelloUDPServer().start(port, threads);
        } catch (NumberFormatException ignored) {
            System.out.println(USAGE);
        }
    }

    @Override
    public void start(int port, int threads) {
        service = Executors.newFixedThreadPool(threads);
        try {
            receivingSocket = new DatagramSocket(port);
            for (int i = 0; i < threads; ++i) {
                service.submit(() -> {
                    try (DatagramSocket sendingSocket = new DatagramSocket()) {
                        DatagramPacket receivingPacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                        while (!Thread.interrupted() && !receivingSocket.isClosed()) {
                            receivingSocket.receive(receivingPacket);
                            String received = new String(receivingPacket.getData(), 0, receivingPacket.getLength());

//                            System.out.println("===Received = " + received);

                            String sending = "Hello, " + received;
                            sendingSocket.send(new DatagramPacket(sending.getBytes(), sending.getBytes().length,
                                    receivingPacket.getAddress(), receivingPacket.getPort()));

//                            System.out.println("Sent = " + sending);
                        }
                    } catch (IOException ignored) {
                    }
                });
            }
        } catch (SocketException e) {
            System.err.println("Socket is misbehaving!");
        }
    }

    @Override
    public void close() {
        service.shutdown();
        try {
            service.awaitTermination(1, TimeUnit.SECONDS);
            service.shutdownNow();
        } catch (InterruptedException ignored) {
        }
        if (receivingSocket != null && !receivingSocket.isClosed()) {
            receivingSocket.close();
        }
    }
}
