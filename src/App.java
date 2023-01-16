
/**
 * Project 2 - kth selection algorithm
 * One-file program which implements different algorithms for selecting the kth element of a list.
 * 
 * This file does not contain all the test data and will not run on its own.
 * Please see https://github.com/jembar-cpp/CS3110-Project2.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Stream;

public class App {
    public static void main(String[] args) throws Exception {
        for(int i = 2; i <= 67108864; i*=2) {
            generateListFile(i);
            String filename = "data/list_size" + i + ".txt";
            int[][] data = generateListFromFile(filename);
            int[] list = data[0];
            int k = data[1][0];
            System.out.printf("----- List size %d -----\n", i);

            float times[] = new float[12];

            // Run 12 tests per list
            for(int j = 1; j <= 12; j++) {
                // Time the selection
                long start = System.nanoTime();
                int result = quickSelectPartitionMM(list.clone(), 0, list.length - 1, k);
                long end = System.nanoTime();
                long time = (end-start);
                float time_ms = (float) time / 1000000;
                System.out.printf("Test %d: %.5f ms.\n", j, time_ms);
                times[j-1] = time_ms;
            }

            // Get the average time and cut the smallest and largest values
            Arrays.sort(times);
            float avg_time = 0;
            for(int j = 1; j <= 10; j++) {
                avg_time += times[j];
            }
            avg_time /= 10;
            System.out.printf("Average time: %.5f ms.\n", avg_time);

            // Write results to file
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File("results/quickselectpartitionmm.txt"), true));
            pw.printf("Size %d: Average time %.5f ms.\n", i, avg_time);
            pw.close();
        }
    }

    public static int mergeSelect(int[] arr, int l, int r, int k) {
        if(l < r) {
            int m = l + (r - l) / 2;
            mergeSelect(arr, l, m, k);
            mergeSelect(arr, m+1, r, k);
            merge(arr, l, m, r);
        }
        // Array is sorted now
        return arr[k-1];
    }
    
    // Merge function of mergesort
    public static void merge(int[] arr, int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;
        int[] left = new int[n1];
        int[] right = new int[n2];

        for (int i = 0; i < n1; i++) {
            left[i] = arr[l + i];
        }
        for (int j = 0; j < n2; j++) {
            right[j] = arr[m + j + 1];
        }
        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if (left[i] <= right[j]) {
                arr[k] = left[i];
                i++;
            }
            else {
                arr[k] = right[j];
                j++;
            }
            k++;
        }
 
        while (i < n1) {
            arr[k] = left[i];
            i++;
            k++;
        }
 
        while (j < n2) {
            arr[k] = right[j];
            j++;
            k++;
        }
    }

    // Quicksort using partition to select
    public static int quickSelectPartition(int[] arr, int l, int r, int k) {
        if(l == r) {
            // Correct element already found
            return arr[l];
        }
        int pivot = partition(arr, l, r);

        if(pivot == k - 1) {
            // Pivot position is the kth element: return it
            return arr[pivot];
        }
        if(pivot < k - 1) {
            // Pivot position is smaller: recursively check right side
            return quickSelectPartition(arr, pivot + 1, r, k);
        }
        // Pivot position is larger: recursively check left side
        return quickSelectPartition(arr, l, pivot - 1, k);
    }

    public static int partition(int[] arr, int l, int r) {
        int pivot = arr[l];
        int pivotPos = l;   // position where pivot should end
        int currentPivotPos = l; // current position of the pivot
        int lastSwappedIndex = l;
        for(int i = l + 1; i <= r; i++) {
            if(arr[i] < pivot) {
                // Swap the element with the pivot
                lastSwappedIndex = i;
                int temp = arr[i];
                arr[i] = pivot;
                arr[currentPivotPos] = temp;
                pivotPos++ ;

                // Pivot back to previous position
                temp = arr[pivotPos];
                arr[pivotPos] = arr[lastSwappedIndex];
                arr[lastSwappedIndex] = temp;
                currentPivotPos = pivotPos;
            }
        }
        return pivotPos;
    }

    // Quicksort using median of medians to partition
    public static int quickSelectPartitionMM(int[] arr, int l, int r, int k) {
        if(l == r) {
            // Correct element already found
            return arr[l];
        }
        int pivot = partitionMM(arr, l, r);

        if(pivot == k - 1) {
            // Pivot position is the kth element: return it
            return arr[pivot];
        }
        if(pivot < k - 1) {
            // Pivot position is smaller: recursively check right side
            return quickSelectPartitionMM(arr, pivot + 1, r, k);
        }
        // Pivot position is larger: recursively check left side
        return quickSelectPartitionMM(arr, l, pivot - 1, k);
    }

    // Partition using median of medians
    public static int partitionMM(int[] arr, int l, int r) {
        int len = r - l + 1;
        if(len >= 15) { // Only use median of medians if array size is large enough
            // Use median of medians
            int[] medians = new int[5];
            int arraySize = len / 5;
            for(int i = 0, i2 = l; i < 5; i++, i2 += arraySize) {
                int[] subArr = new int[arraySize];
                for(int j = i2, j2 = 0; j2 < arraySize; j++, j2++) {
                    subArr[j2] = arr[j];
                }
                // Find the median of the subarray
                medians[i] = median(subArr);
            }
            // Find the median of medians
            int mm = median(medians);
            for(int i = l; i < len; i++) {
                if(arr[i] == mm) {
                    // Swap with first element
                    int temp = arr[l];
                    arr[l] = arr[i];
                    arr[i] = temp;
                }
            }
        }
        int pivot = arr[l];
        int pivotPos = l;   // position where pivot should end
        int currentPivotPos = l; // current position of the pivot
        int lastSwappedIndex = l;
        for(int i = l + 1; i <= r; i++) {
            if(arr[i] < pivot) {
                // Swap the element with the pivot
                lastSwappedIndex = i;
                int temp = arr[i];
                arr[i] = pivot;
                arr[currentPivotPos] = temp;
                pivotPos++ ;

                // Pivot back to previous position
                temp = arr[pivotPos];
                arr[pivotPos] = arr[lastSwappedIndex];
                arr[lastSwappedIndex] = temp;
                currentPivotPos = pivotPos;
            }
        }
        return pivotPos;
    }

    // Sorts array and finds the median
    public static int median(int[] arr) {
        Arrays.sort(arr);
        return arr[(arr.length - 1) / 2];
    }
    

    /**
     * Reads a file and writes to a list
     * The file is assumed to be of the following format (and will fail if it isn't):
     * First line: the element to select (element number, not index)
     * Second line: space-separated integers for the list
     * 
     * @param filename   String    The relative path to the file to read the matrix from
     * @return the list, and the number to select in an array
     * @throws FileNotFoundException
     */
    public static int[][] generateListFromFile(String filename) throws FileNotFoundException {
        File f = new File(filename);
        Scanner sc = new Scanner(f);
        int[] k = { Integer.parseInt(sc.nextLine()) };
        int[] list = Stream.of(sc.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
        sc.close();
        return new int[][] {list, k};
    }

    /**
     * Generates a file for a random list of a specified size.
     * The file should not be cleared afterwards, so that subsequent tests can use the same list.
     * Each element in the list is an integer from -100 (inclusive) to 100 (non-inclusive).
     * Files are named as list_size[n].txt, a list of size 64 would be named list_size64.txt
     * 
     * @param n int The size of the matrix
     */
    public static void generateListFile(int n) throws IOException {
        String filename = "data/list_size" + n + ".txt";
        if (new File(filename).isFile()) {
            // File already exists
            return;
        }
        PrintWriter pw = new PrintWriter(filename, "UTF-8");
        int k = (int) (Math.random() * n) + 1;
        pw.println(k);
        for (int i = 0; i < n; i++) {
            int randInt = (int) (Math.random() * 200) - 100;
            pw.printf("%d" + (i == n - 1 ? "" : " "), randInt);
        }
        pw.close();
    }
}