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
        var dll_path = System.getProperty("user.dir") + "/thor_lib.so"; // change to .so for linux
        System.load(dll_path);

        // Loading functions
        MemorySegment start_rapl_symbol = SymbolLookup.loaderLookup().find("start_rapl").get();
        MethodHandle start_rapl = Linker.nativeLinker().downcallHandle(start_rapl_symbol,
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

        MemorySegment stop_rapl_symbol = SymbolLookup.loaderLookup().find("stop_rapl").get();
        MethodHandle stop_rapl = Linker.nativeLinker().downcallHandle(stop_rapl_symbol,
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

        String input = "ToBeSorted.txt";

        try {
            input = getResourceFileAsString(input);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String[] data = input.replace("[", "").replace("]", "").split(",");
        List<Long> sortParam = Arrays.stream(data).map(String::trim).map(Long::valueOf).toList();

        try {
            start_rapl.invoke();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try (Arena arena = Arena.ofConfined()) {
            // c strings
            MemorySegment FibName = arena.allocateFrom("Fib");
            MemorySegment NbodyName = arena.allocateFrom("N-body");
            MemorySegment MergeName = arena.allocateFrom("MergeSort");
            MemorySegment QuickName = arena.allocateFrom("QuickSort");

            start_rapl.invoke(FibName);
            long fib = Fib.recFibN(47);
            stop_rapl.invoke(FibName);

            start_rapl.invoke(NbodyName);
            NBody.N_Body(50000000);
            stop_rapl.invoke((NbodyName));

            start_rapl.invoke(MergeName);
            var MergeSorted = Merge.mergeSort(sortParam);
            stop_rapl.invoke(MergeName);

            start_rapl.invoke(QuickName);
            var QuickSorted = QuickSort.quickSort(sortParam);
            stop_rapl.invoke(QuickName);

            System.out.println("Fibonacci: " + fib);
            System.out.println("Merge Sort: " + MergeSorted);
            System.out.println("Quick Sort: " + QuickSorted);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
