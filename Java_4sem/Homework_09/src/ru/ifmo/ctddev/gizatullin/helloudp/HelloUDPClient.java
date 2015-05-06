package ru.ifmo.ctddev.gizatullin.helloudp;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Client that sends request and receives responses, and if response is client is waiting for sends next request.
 * <p>
 * Client cab be created from the command line with five parameters:
 * "url/ip-address port request number_of_threads number_of_requests_per_thread"
 * <p>
 * Client begins to send request with the method {@code start}.
 *
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 4/29/15.
 * @see info.kgeorgiy.java.advanced.hello.HelloClient
 * @see #main
 * @see #start
 */
public class HelloUDPClient implements HelloClient {
    private static final String USAGE = "url/ip-address port request number_of_threads number_of_requests_per_thread";
    private static final long TERMINATION_TIME_OUT = 10;
    private static final int SOCKET_TIMEOUT = 50;

    /**
     * Method to create the client and start it from the command line. Usage for the parameters:
     * {@value "url/ip-address port request number_of_threads number_of_requests_per_thread"}
     * url/ip-address - address to send requests to
     * port - port to send requests to
     * request - string to send as a request
     * number_of_threads - number of threads to send requests on
     * number_of_requests_per_thread - number of the requests to send on one thread
     *
     * @param args parameters from the command line
     * @see #start
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 5 ||
                Arrays.stream(args).anyMatch(Predicate.isEqual(null))) {
            System.err.println(USAGE);
            return;
        }
        try {
            String url = args[0];
            String request = args[2];
            int port = Integer.parseInt(args[1]);
            int numberOfThreads = Integer.parseInt(args[3]);
            int numberOfRequestsPerThread = Integer.parseInt(args[4]);
            if (port < 0 || port > 0xFFFF || numberOfThreads < 1 || numberOfRequestsPerThread < 1) {
                throw new NumberFormatException();
            }
            new HelloUDPClient().start(url, port, request, numberOfThreads, numberOfRequestsPerThread);
        } catch (NumberFormatException ignored) {
            System.err.println(USAGE);
        }
    }

    /**
     * Method to start sending requests.
     *
     * @param url      address to send requests to
     * @param port     port to connect to
     * @param prefix   the prefix of the request
     * @param requests number of the requests per thread
     * @param threads  number of the threads to send requests from
     * @see java.util.concurrent.ExecutorService
     * @see java.net.InetAddress
     * @see java.net.DatagramSocket
     * @see java.net.DatagramPacket
     */
    @Override
    public void start(String url, int port, String prefix, int requests, int threads) {
        if (port < 0 || port > 0xFFFF) {
            System.err.println("Port id is incorrect, has to be between 0 and 65535");
            return;
        }
        if (threads < 1) {
            System.err.println("Number of threads is non-positive");
            return;
        }
        ExecutorService service = Executors.newFixedThreadPool(threads);
        try {
            InetAddress address = InetAddress.getByName(url);
            for (int i = 0; i < threads; ++i) {
                final int threadId = i;
                service.submit(() -> {
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.setSoTimeout(SOCKET_TIMEOUT);
                        for (int requestId = 0; requestId < requests; ++requestId) {
                            String request = prefix + threadId + "_" + requestId;
                            int len = request.getBytes().length;
                            DatagramPacket sendingPacket =
                                    new DatagramPacket(request.getBytes(), request.getBytes().length, address, port);
                            DatagramPacket receivedPacket = new DatagramPacket(new byte[len + 10], len + 10);
                            String required = "Hello, " + request;
                            String received = "";
                            while (!required.equals(received)) {
                                try {
                                    System.out.println("===Sending request = " + request);
                                    socket.send(sendingPacket);

                                    socket.receive(receivedPacket);
                                    received = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                                    System.out.println("Received = " + received);
                                } catch (IOException ignored) {
                                }
                            }
                        }
                    } catch (SocketException ignored) {
                    }
                });
            }

            service.shutdownNow();
            service.awaitTermination(TERMINATION_TIME_OUT, TimeUnit.SECONDS);
        } catch (UnknownHostException ignored) {
            System.err.println("Host can't be identified: " + url);
        } catch (InterruptedException ignored) {
        }
    }
}
