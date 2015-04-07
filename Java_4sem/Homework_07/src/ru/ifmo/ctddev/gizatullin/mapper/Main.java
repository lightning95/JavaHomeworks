package ru.ifmo.ctddev.gizatullin.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 4/1/15.
 */
public class Main {
    public static void main(String[] args) {
        IterativeParallelism iterativeParallelism = new IterativeParallelism(new ParallelMapperImpl(5));
        List<Integer> list = new ArrayList<>();
        int n = 100;
        for (int i = 0; i < n; ++i) {
            list.add(i);
        }

        try {
            for (int i = 0; i < n; ++i) {
                iterativeParallelism.any(5, list, new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) {
                        return integer > 5;
                    }
                });
                iterativeParallelism.any(5, list, new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) {
                        return integer > 5;
                    }
                });
                iterativeParallelism.any(5, list, new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) {
                        return integer > 5;
                    }
                });
                iterativeParallelism.any(5, list, new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) {
                        return integer > 5;
                    }
                });
            }
            System.err.println("===END");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
