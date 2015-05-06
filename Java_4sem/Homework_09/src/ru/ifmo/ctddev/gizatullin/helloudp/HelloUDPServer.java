package ru.ifmo.ctddev.gizatullin.helloudp;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Server that receives request, processes them and sends responses.
 * Server will reply to every request if the number of threads it is working on allows.
 * The reply will be {@code "Hello, " + received}, where "receive" is the string representation of the request.
 * <p>
 * Server can be created from the command line with two parameters:
 * port and number of threads.
 * <p>
 * Server is started with the method {@code start}.
 * <p>
 * To use method start user has to provide two integers - port and threads' number.
 * <p>
 * Server can be started several times on different ports.
 * <p>
 * To close the server there is the method {@code close}.
 *
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 5/6/15.
 * @see info.kgeorgiy.java.advanced.hello.HelloServer
 * @see #start
 */
public class HelloUDPServer implements HelloServer {
    private static final int BUFFER_SIZE = 1024;
    private static final String USAGE = "Port number_of_threads";
    private ConcurrentLinkedQueue<DatagramSocket> receivingSockets;
    private ConcurrentLinkedQueue<ExecutorService> services;

    /**
     * Method to create class and execute from the command line. Usage for parameters to provide:
     * {@code port number_of_threads}
     * port - port to start the server on
     * number_of_threads - number of threads to process request on
     *
     * @param args arguments of the command line
     * @see #start
     */
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

    /**
     * Method to start server with the given parameters
     *
     * @param port    number of the port to start server on
     * @param threads number of threads to process requests on
     * @see java.util.concurrent.ExecutorService
     * @see java.net.DatagramSocket
     * @see java.net.DatagramPacket
     */
    @Override
    public void start(int port, int threads) {
        if (port < 0 || port > 0xFFFF) {
            System.err.println("Port id is incorrect, has to be between 0 and 65535");
            return;
        }
        if (threads < 1) {
            System.err.println("Number of threads is non-positive");
            return;
        }
        if (services == null) {
            services = new ConcurrentLinkedQueue<>();
        }
        ExecutorService service = Executors.newFixedThreadPool(threads);
        services.add(service);
        try {
            DatagramSocket receivingSocket = new DatagramSocket(port);
            if (receivingSockets == null) {
                receivingSockets = new ConcurrentLinkedQueue<>();
            }
            receivingSockets.add(receivingSocket);
            for (int i = 0; i < threads; ++i) {
                service.submit(() -> {
                    try (DatagramSocket sendingSocket = new DatagramSocket()) {
                        DatagramPacket receivingPacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                        while (!Thread.interrupted() && !receivingSocket.isClosed()) {
                            receivingSocket.receive(receivingPacket);
                            String received = new String(receivingPacket.getData(), 0, receivingPacket.getLength());
                            String sending = "Hello, " + received;
                            sendingSocket.send(new DatagramPacket(sending.getBytes(), sending.getBytes().length,
                                    receivingPacket.getAddress(), receivingPacket.getPort()));
                        }
                    } catch (IOException ignored) {
                    }
                });
            }
        } catch (SocketException e) {
            System.err.println("Socket is misbehaving!");
        }
    }

    /**
     * Method to close server
     *
     * @see java.util.concurrent.ExecutorService
     * @see java.net.DatagramSocket
     */
    @Override
    public void close() {
        if (services != null) {
            services.parallelStream().forEach(service -> {
                service.shutdown();
                try {
                    service.awaitTermination(1, TimeUnit.SECONDS);
                    service.shutdownNow();
                } catch (InterruptedException ignored) {
                }
            });
        }
        if (receivingSockets != null) {
            receivingSockets.parallelStream().forEach(DatagramSocket::close);
        }
    }
}
