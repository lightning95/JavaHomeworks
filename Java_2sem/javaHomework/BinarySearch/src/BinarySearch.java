/**
 * Created by Aydar on 27.02.14.
 */

public class BinarySearch {
    // for all i : 1 <= i < a.length - 1 : a[i - 1] <= a[i] <= a[i + 1]
    public static int binarySearchIterative(int x, int[] a) {
        int l = -1;
		int r = a.length;
        // let a[-1] <= x, x < a[a.length]
        // inv : -1 <= l < r <= a.length,  a[l] < x <= a[r]
        while (l < r - 1) {
            int m = (l + r) >> 1;
			// l < m < r
            if (a[m] < x) {
                l = m;
				// a[l] < x <= a[r]
            } else {
                r = m;
				// a[l] < x <= a[r]
            }
        }
        return r;
    }
	// let x < a[a.length]
    // result in [0..a.length] : x <= a[result] && (result == 0 || result > 0 && a[result - 1] < x)

    // -1 <= l <= r - 1 < a.length, for all i : l + 2 <= i <= r - 2 : a[i - 1] <= a[i] <= a[i + 1]
    // let a[-1] <= x, x <= a[a.length], a[l] <= x < a[r]
    public static int binarySearchRecursive(int x, int[] a, int l, int r) {
        if (l >= r - 1) {
            return r;
        }
        int m = (l + r) >> 1;
		// l < m < r
        if (a[m] <= x) {
			// a[l] <= x < a[r]
            return binarySearchRecursive(x, a, m, r);
        } else {
			// a[l] <= x < a[r]
            return binarySearchRecursive(x, a, l, m);
        }
    }
	// let x < a[a.length] 
    // -1 <= l <= r - 1 < a.length, result in [l + 1, r] : x < a[result] && (result == 0 || result > 0 && a[result - 1] <= x)

    public static void main(String[] args) {
        int f = Integer.parseInt(args[0]);
        int[] a = new int[args.length - 1];

        // inv : for all j < i : a[j] == toInt(args[j + 1])
        for (int i = 0; i < a.length; ++i) {
            a[i] = Integer.parseInt(args[i + 1]);
        }

		int l = binarySearchIterative(f, a);
		int r = binarySearchRecursive(f, a, -1, a.length);
        System.out.println(l + " " + (r - l));
    }
}
