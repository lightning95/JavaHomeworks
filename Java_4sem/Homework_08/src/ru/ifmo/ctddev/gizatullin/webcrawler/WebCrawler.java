package ru.ifmo.ctddev.gizatullin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.*;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 4/29/15.
 */
public class WebCrawler implements Crawler {
    private static final String USAGE = "Usage: WebCrawler url [downloads [extractors [perHost]]]";
    public static final Object NOTHING = new Object();

    private final Downloader downloader;
    private final ExecutorService downloadService;
    private final ExecutorService extractService;
    private final int perHost;

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 4
                || Arrays.stream(args).anyMatch(Predicate.isEqual(null))) {
            System.err.println(USAGE);
            return;
        }
        String url = args[0];
        int downloaders = 50;
        int extractors = 50;
        int perHost = 50;
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
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
            System.err.println(USAGE);
        }
        try (Crawler crawler = new WebCrawler(new CachingDownloader(new File("./tmp/")),
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

    @Override
    public List<String> download(String url, int depth) throws IOException {
        final int TASKS_MAX_COUNT = Integer.MAX_VALUE;
        final Semaphore available = new Semaphore(TASKS_MAX_COUNT);
        final ConcurrentLinkedQueue<Pair<String, Integer>> queue = new ConcurrentLinkedQueue<>();
        final ConcurrentMap<String, Semaphore> hostSemaphores = new ConcurrentHashMap<>();
        final ConcurrentMap<String, Object> visited = new ConcurrentHashMap<>();

        try {
            available.acquire();
            queue.add(new Pair<>(url, depth));
            downloadService.submit(() -> this.download(available, queue, hostSemaphores, visited));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        try {
            available.acquire(TASKS_MAX_COUNT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return visited.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public void close() {
        shutDownService(downloadService, "downloadService");
        shutDownService(extractService, "executorService");
    }

    private void download(final Semaphore available, final ConcurrentLinkedQueue<Pair<String, Integer>> queue,
                          final ConcurrentMap<String, Semaphore> hostSemaphores, final ConcurrentMap<String, Object> visited) {
        try {
            if (!queue.isEmpty()) {
                Pair<String, Integer> pair = queue.poll();
                String url = pair.getKey();
                int depth = pair.getValue();
                if (!visited.containsKey(url)) {
                    try {
                        String host = URLUtils.getHost(url);
                        hostSemaphores.putIfAbsent(host, new Semaphore(perHost));
                        if (hostSemaphores.get(host).tryAcquire()) {
                            try {
                                available.acquire();
                                visited.put(url, NOTHING);
                                Document doc = downloader.download(url);
                                extractService.submit(() -> extract(depth, doc, visited, available, hostSemaphores, queue));
                            } catch (InterruptedException | IOException e) {
                                e.printStackTrace();
                            } finally {
                                hostSemaphores.get(host).release();
                                if (!queue.isEmpty()) {
                                    try {
                                        available.acquire();
                                        downloadService.submit(() -> download(available, queue, hostSemaphores, visited));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            queue.add(pair);
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

    private void extract(final int depth, final Document document, ConcurrentMap<String, Object> visited, final Semaphore available,
                         final ConcurrentMap<String, Semaphore> hostSemaphores, final ConcurrentLinkedQueue<Pair<String, Integer>> queue) {
        try {
            if (depth > 1) {
                List<String> links = document.extractLinks();
                links.parallelStream().distinct().filter(link -> !visited.containsKey(link)).forEach(url -> {
                    try {
                        available.acquire();
                        queue.add(new Pair<>(url, depth - 1));
                        downloadService.submit(() -> download(available, queue, hostSemaphores, visited));
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

    private void shutDownService(ExecutorService executorService, String serviceName) {
        System.out.println("Shutting down " + serviceName);
        executorService.shutdown();
        if (!executorService.isShutdown()) {
            try {
                executorService.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                executorService.shutdownNow();
            }
        }
    }
}
