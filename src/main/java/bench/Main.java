package bench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class Main {
    /**
     * https://stackoverflow.com/questions/6068197/read-resource-text-file-to-string-in-java
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */

    static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null)
                return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    public static void main(String[] args) {
        // System.load("./thor_lib.so");

        // Loading functions
        // MemorySegment start_rapl_symbol =
        // SymbolLookup.loaderLookup().find("start_rapl").get();
        // MethodHandle start_rapl =
        // Linker.nativeLinker().downcallHandle(start_rapl_symbol,
        // FunctionDescriptor.of(ValueLayout.JAVA_INT));

        // MemorySegment stop_rapl_symbol =
        // SymbolLookup.loaderLookup().find("stop_rapl").get();
        // MethodHandle stop_rapl =
        // Linker.nativeLinker().downcallHandle(stop_rapl_symbol,
        // FunctionDescriptor.of(ValueLayout.JAVA_INT));

        //try {
        //    start_rapl.invoke();
        //} catch (Throwable e) {
        //    e.printStackTrace();
        //}

        String input = "ToBeSorted.txt";

        try {
            input = getResourceFileAsString(input);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String[] data = input.replace("[", "").replace("]", "").split(",");
        List<Long> sortParam = Arrays.stream(data).map(String::trim).map(Long::valueOf).toList();

        long fib = Fib.recFibN(47);
        NBody.N_Body(50000000);
        var MergeSorted = Merge.mergeSort(sortParam);
        var QuickSorted = QuickSort.quickSort(sortParam);

        System.out.println("Fibonacci: " + fib);
        System.out.println("Merge Sort: " + MergeSorted);
        System.out.println("Quick Sort: " + QuickSorted);
    }
}
