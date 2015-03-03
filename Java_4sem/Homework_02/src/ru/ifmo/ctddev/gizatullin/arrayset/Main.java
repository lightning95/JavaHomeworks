package ru.ifmo.ctddev.gizatullin.arrayset;

import java.util.*;

/**
 * Created by lightning95 on 2/24/15.
 */

public class Main {
    public static void main(String[] args) {

        int[] a = {1, 1, 2, 2, 3, 5, 5};
        List<Integer> list = new ArrayList<>();
        Comparator<Integer> comp = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return 0;
            }
        };
        for (int i : a){
            list.add(i);
        }

//        TreeSet<Integer> treeSet;
        ArraySet<Integer> arraySet = new ArraySet<>(list);

        /*{
            // -2 1 4 6 10
            int l = 6;
            int r = 3;
            boolean lIn = false;
            boolean rIn = false;
            System.out.println("==Subset: l = " + l + ", r = " + r + ", lInc = " + lIn + ", rInc = " + rIn);
            NavigableSet<Integer> set = arraySet.subSet(l, lIn, r, rIn);
            System.out.println("  size = " + set.size());
        }*/
        System.out.println(arraySet.subSet(1, true, 3, false));
    }
}
