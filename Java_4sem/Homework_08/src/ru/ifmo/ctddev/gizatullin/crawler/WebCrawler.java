package ru.ifmo.ctddev.gizatullin.crawler;

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
 * Class that downloads websites recursively in parallel with the specified depth and returns all the files and links
 * that are downloaded during the bypass.
 * <p>
 * Class can be created with specified restrictions on simultaneous operations:
 * downloads, extractions and downloads per host.
 * <p>
 * It tries to visit websites starting with the lowest depth to the deepest, if nothing prevents it.
 * <p>
 * To use method {@link #download} user needs to provide {@link info.kgeorgiy.java.advanced.crawler.Downloader}.
 *
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 4/29/15.
 * @see info.kgeorgiy.java.advanced.crawler.Crawler
 * @see info.kgeorgiy.java.advanced.crawler.URLUtils
 * @see info.kgeorgiy.java.advanced.crawler.Downloader
 * @see info.kgeorgiy.java.advanced.crawler.Document
 */
public class WebCrawler implements Crawler {
    private static final String USAGE = "Usage: WebCrawler url [downloaders [extractors [perHost]]]";
    private static final Object NOTHING = new Object();
    private static final int TASKS_MAX_COUNT = Integer.MAX_VALUE;
    private static final int DOWNLOADERS_DEFAULT_NUMBER = 50;
    private static final int EXTRACTORS_DEFAULT_NUMBER = 50;
    private static final int PER_HOST_DEFAULT_NUMBER = 50;
    private static final int DEFAULT_DEPTH = 1;

    private final Downloader downloader;
    private final ExecutorService downloadService;
    private final ExecutorService extractService;
    private final int perHost;

    /**
     * Method to create class and execute from the command line. Usage for parameters to provide:
     * {@code url [dowloaders [extractors [perHost]]]}
     * <p>
     * Class is created with default parameters, if there aren't provided any.
     * <p>
     * The file is saved in ./tmp/ directory.
     *
     * @param args command line arguments
     * @see info.kgeorgiy.java.advanced.crawler.CachingDownloader
     * @see info.kgeorgiy.java.advanced.crawler.Crawler
     * @see #download
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 4
                || Arrays.stream(args).anyMatch(Predicate.isEqual(null))) {
            System.err.println(USAGE);
            return;
        }
        String url = args[0];
        int downloaders = DOWNLOADERS_DEFAULT_NUMBER;
        int extractors = EXTRACTORS_DEFAULT_NUMBER;
        int perHost = PER_HOST_DEFAULT_NUMBER;
        int depth = DEFAULT_DEPTH;
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
        } catch (IOException ignored) {
        }
    }

    /**
     * Constructor of the class.
     * <p>
     * Constructs class with specified parameters.
     *
     * @param downloader  class that provider downloading specified urls
     * @param downloaders maximum number of simultaneous downloading
     * @param extractors  maximum number of simultaneous extractions
     * @param perHost     maximum number of simultaneous downloading from the same host
     * @see java.util.concurrent.Executors
     * @see info.kgeorgiy.java.advanced.crawler.Downloader
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.extractService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    /**
     * Downloads pages and files starting with the given url.
     * <p>
     * Downloads pages and files that aren't distant for more than given depth starting with specified url.
     *
     * @param url   website to start the bypass
     * @param depth maximum depth of the website from the starting one
     * @return list of urls of website visited
     * @throws IOException when a website is failed to be downloaded
     * @see java.util.concurrent.Semaphore
     * @see java.util.concurrent.ExecutorService
     * @see java.util.concurrent.ConcurrentHashMap
     * @see java.util.concurrent.ConcurrentLinkedQueue
     */
    @Override
    public List<String> download(String url, int depth) throws IOException {
        final Semaphore available = new Semaphore(TASKS_MAX_COUNT);
        final ConcurrentLinkedQueue<Pair<String, Integer>> queue = new ConcurrentLinkedQueue<>();
        final ConcurrentMap<String, Semaphore> hostAvailable = new ConcurrentHashMap<>();
        final ConcurrentMap<String, Object> visited = new ConcurrentHashMap<>();

        try {
            available.acquire();
            queue.add(new Pair<>(url, depth));
            downloadService.submit(() -> this.download(available, queue, hostAvailable, visited));
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

    /**
     * Closes the class -- stops the class from visiting websites.
     * <p>
     * Shutdowns services -- {@code downloadService, extractService}-- initiates an orderly shutdown
     * in which previously submitted tasks are executed, but no new tasks will be accepted.
     * If service isn't shut down immediately blocks until all tasks have completed execution after a shutdown
     * request or the timeout of 2 seconds occurs then attempts to stop all actively executing tasks and halts the
     * processing of waiting tasks.
     */
    @Override
    public void close() {
        shutDownService(downloadService, "downloadService");
        shutDownService(extractService, "executorService");
    }

    private void download(final Semaphore available, final ConcurrentLinkedQueue<Pair<String, Integer>> queue,
                          final ConcurrentMap<String, Semaphore> hostAvailable, final ConcurrentMap<String, Object> visited) {
        try {
            if (!queue.isEmpty()) {
                Pair<String, Integer> pair = queue.poll();
                String url = pair.getKey();
                int depth = pair.getValue();
                if (!visited.containsKey(url)) {
                    try {
                        String host = URLUtils.getHost(url);
                        hostAvailable.putIfAbsent(host, new Semaphore(perHost));
                        if (hostAvailable.get(host).tryAcquire()) {
                            try {
                                available.acquire();
                                visited.put(url, NOTHING);
                                Document doc = downloader.download(url);
                                extractService.submit(() -> extract(depth, doc, visited, available, hostAvailable, queue));
                            } catch (InterruptedException | IOException e) {
                                e.printStackTrace();
                            } finally {
                                hostAvailable.get(host).release();
                                if (!queue.isEmpty()) {
                                    try {
                                        available.acquire();
                                        downloadService.submit(() -> download(available, queue, hostAvailable, visited));
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
                         final ConcurrentMap<String, Semaphore> hostAvailable, final ConcurrentLinkedQueue<Pair<String, Integer>> queue) {
        try {
            if (depth > 1) {
                List<String> links = document.extractLinks();
                links.parallelStream().distinct().filter(link -> !visited.containsKey(link)).forEach(url -> {
                    try {
                        available.acquire();
                        queue.add(new Pair<>(url, depth - 1));
                        downloadService.submit(() -> download(available, queue, hostAvailable, visited));
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
