package ru.ifmo.ctddev.gizatullin.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * @author Aydar Gizatullin a.k.a. lightning95 (aydar.gizatullin@gmail.com)
 *         Created on 3/19/15.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Task> queue;
    private final Thread[] threads;

    /**
     * Create ParallelMapper with given number of threads.
     *
     * @param numOfThreads number of threads to work on
     */
    public ParallelMapperImpl(int numOfThreads) {
        queue = new ArrayDeque<>();
        threads = new Thread[numOfThreads];
        for (int i = 0; i < numOfThreads; i++) {
            threads[i] = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    Task task;
                    synchronized (queue) {
                        while (queue.isEmpty()) {
                            try {
                                queue.wait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                        task = queue.poll();
                    }
                    task.execute();
                    Thread.currentThread().interrupt();
                }
            });
        }
        Arrays.stream(threads).forEach(Thread::start);
    }

    /**
     * Adds task to working queue.
     *
     * Adds task to working queue and returns the task-object
     *
     * @param <T> type of argument
     * @param <R> type of function's result
     * @param function function to apply
     * @param argument arguments to apply function to
     * @return task-object to return
     */
    private <T, R> Task<T, R> addTask(Function<? super T, ? extends R> function, T argument) {
        Task<T, R> task = new Task<>(function, argument);
        synchronized (queue) {
            queue.add(task);
            queue.notifyAll();
        }
        return task;
    }

    /**
     * Apply function to all elements of list and return list of results.
     *
     * @param function function for applying
     * @param list     list to process
     * @param <T>      type of elements of given list
     * @param <R>      type of results
     * @return list with results of applying function
     * @throws InterruptedException if one of thread-workers was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        List<Task<T, R>> tasks = new ArrayList<>();
        list.forEach(t -> tasks.add(addTask(function, t)));

        List<R> ans = new ArrayList<>();
        tasks.forEach(t -> ans.add(t.get()));
        return ans;
    }

    /**
     * Stop all thread-workers of parallelMapper.
     *
     * @throws InterruptedException
     */
    @Override
    public void close() throws InterruptedException {
        Arrays.stream(threads).forEach(Thread::interrupt);
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private class Task<T, R> {
        private Function<? super T, ? extends R> function;
        private T argument;
        private R answer;
        private boolean ready;
        private boolean aborted;

        public Task(Function<? super T, ? extends R> function, T argument) {
            this.function = function;
            this.argument = argument;
            ready = false;
            aborted = false;
        }

        public synchronized void execute() {
            try {
                answer = function.apply(argument);
                ready = true;
            } catch (Exception e) {
                aborted = true;
            }
            notifyAll();
        }

        public synchronized R get() {
            while (!ready && !aborted) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
            return answer;
        }
    }
}