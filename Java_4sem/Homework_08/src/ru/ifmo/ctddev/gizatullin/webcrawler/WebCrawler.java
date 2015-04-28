package ru.ifmo.ctddev.gizatullin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.*;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 4/22/15.
 */
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloadService, extractService;
    private final int perHost;
    private final static String USAGE = "Usage: WebCrawler url [downloads [extractors [perHost]]]";

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 4
                || Arrays.stream(args).filter(a -> a == null).count() > 0) {
            System.err.println(USAGE);
            return;
        }
        String url = args[0];
        int downloaders = 20;
        int extractors = 20;
        int perHost = 20;
        int depth = 1;
        try {
            if (args.length > 1) {
                downloaders = Integer.parseInt(args[1]);
            }
            if (args.length > 2) {
                extractors = Integer.parseInt(args[2]);
            }
            if (args.length > 3) {
                perHost = Integer.parseInt(args[3]);
            }
        } catch (NumberFormatException ignored) {
            System.err.println(ignored.getMessage());
            System.err.println(USAGE);
        }
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(new File("./default/")),
                downloaders, extractors, perHost)) {
            crawler.download(url, depth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.extractService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    private void produceExtract(int depth, Document doc,
                                final ConcurrentMap<String, Object> visited,
                                final ConcurrentMap<String, Semaphore> hostSemaphores,
                                final ConcurrentLinkedQueue<Pair<String, Integer>> downloadQueues,
                                final Semaphore available) {
        try {
            if (depth > 1) {
                List<String> docLinks = doc.extractLinks();
                docLinks.parallelStream().distinct().filter(link -> !visited.containsKey(link)).forEach(url -> {
                    try {
                        downloadQueues.add(new Pair<>(url, depth - 1));
                        available.acquire();
                        downloadService.submit(() ->
                                this.produceDownload(visited, hostSemaphores, downloadQueues, available));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            available.release();
        }
    }

    private void produceDownload(
            final ConcurrentMap<String, Object> visited,
            final ConcurrentMap<String, Semaphore> hostSemaphores,
            final ConcurrentLinkedQueue<Pair<String, Integer>> downloadQueues,
            final Semaphore available) {
        try {
            if (!downloadQueues.isEmpty()) {
                Pair<String, Integer> pair = downloadQueues.poll();
                String url = pair.getKey();
                int depth = pair.getValue();
                if (!visited.containsKey(url)) {
                    try {
                        String host = URLUtils.getHost(url);
                        hostSemaphores.putIfAbsent(host, new Semaphore(perHost));
                        if (hostSemaphores.get(host).tryAcquire()) {
                            try {
                                visited.put(url, new Object());
                                Document doc = downloader.download(url);
                                available.acquire();
                                extractService.submit(() ->
                                        produceExtract(depth, doc,visited, hostSemaphores, downloadQueues,
                                                available));
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                hostSemaphores.get(host).release();
                                if (!downloadQueues.isEmpty()) {
                                    try {
                                        available.acquire();
                                        downloadService.submit(() ->
                                                produceDownload(visited, hostSemaphores, downloadQueues,
                                                        available));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            downloadQueues.add(pair);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            available.release();
        }
    }

    @Override
    public List<String> download(String url, int depth) throws IOException {
        final int semaphorSize = Integer.MAX_VALUE;
        final Semaphore available = new Semaphore(semaphorSize);
        final ConcurrentMap<String, Object> visited = new ConcurrentHashMap<>();
        final ConcurrentMap<String, Semaphore> hostSemaphores = new ConcurrentHashMap<>();
        final ConcurrentLinkedQueue<Pair<String, Integer>> downloadQueue = new ConcurrentLinkedQueue<>();

        try {
            downloadQueue.add(new Pair<>(url, depth));
            available.acquire();
            downloadService.submit(() ->
                    this.produceDownload(visited, hostSemaphores, downloadQueue, available));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        try {
            available.acquire(semaphorSize);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return visited.keySet().parallelStream().collect(Collectors.toList());
    }

    @Override
    public void close() {
        pleaseShutDown(downloadService, "downloadService");
        pleaseShutDown(extractService, "extractService");
    }

    private void pleaseShutDown(ExecutorService executorService, String name) {
        System.out.println("Closing the service " + name);
        executorService.shutdown();
        if (!executorService.isShutdown()) {
            try {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                executorService.shutdownNow();
            }
        }
    }
}