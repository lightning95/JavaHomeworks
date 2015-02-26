package ru.ifmo.ctddev.gizatullin.arrayset;

import java.util.*;

/**
 * Created by lightning95 on 2/24/15.
 */

public class Main {
    public static void main(String[] args) {

        int[] a = {1985826776, 1246888067, -1300297742, -390516026, 398842845, 2085852955, 1410504140};
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
        ArraySet<Integer> arraySet = new ArraySet<>(list, comp);

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
        System.out.println(arraySet.lower(1985826776));

//        in lower(1985826776) (comparator = All equal, elements = [1985826776, 1246888067, -1300297742, -390516026, 398842845, 2085852955, 1410504140]) expected:<null> but was:<-1300297742>
    }
}
