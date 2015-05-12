package ru.ifmo.ctddev.gizatullin.helloudp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 5/12/15.
 */
public class Main {
    public static void main(String[] args) {
        HelloUDPServer helloUDPServer = new HelloUDPServer();
        final int[] port = {8888};
        int threads = 10;
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; ++i) {
            service.submit(() -> {
                helloUDPServer.start(++port[0], threads);
            });
        }
        helloUDPServer.close();

        System.err.println("HIII");
        helloUDPServer.start(++port[0], threads);
        helloUDPServer.close();

        service.shutdown();
    }
}
