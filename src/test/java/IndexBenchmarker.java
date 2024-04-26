import catalog.Catalog;
import cli.CLI;
import mocks.MockCLI;
import sm.StorageManager;
import util.StrBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * <b>File:</b> IndexBenchmarker.java
 * <p>
 * <b>Description:</b> Tester class for benchmarking index performance
 *
 * @author Derek Garcia
 */
public class IndexBenchmarker {

    private static final String INDEX_BENCHMARK_PATH = "src/test/resources/cmd/in/index-benchmark";

    /**
     * Remove previous database files
     */
    private static void cleanUp(String path) {
        for (File file : Objects.requireNonNull(new File(path).listFiles()))
            if (!file.isDirectory()) {
                file.delete();
            }
    }

    /**
     * Run Benchmark Test
     *
     * @param args Test Database, page size, buffer size
     * @throws IOException failed to open diff file
     */
    public static void main(String[] args) throws IOException {
        String dbRoot = args[0];
        int pageSize = Integer.parseInt(args[1]);
        int bufferSize = Integer.parseInt(args[2]);

        Files.createDirectories(Paths.get(dbRoot + "-no-index"));
        Files.createDirectories(Paths.get(dbRoot + "-index"));

        System.out.println(new StrBuilder()
                .addLine("Running Index Benchmarker")
                .addLine("\tBuffer Size: " + bufferSize)
                .addLine("\tPage Size: " + pageSize)
                .build());

        // Create no index CLI
        Catalog noIndexCatalog = new Catalog(pageSize, bufferSize, dbRoot + "-no-index", false);
        StorageManager noIndexSM = new StorageManager(bufferSize, pageSize, dbRoot + "-no-index", false);
        CLI noIndex = new CLI(noIndexCatalog, noIndexSM);

        // Create index CLI
        Catalog indexCatalog = new Catalog(pageSize, bufferSize, dbRoot + "-index", true);
        StorageManager indexSM = new StorageManager(bufferSize, pageSize, dbRoot + "-index", true);
        CLI index = new CLI(indexCatalog, indexSM);

        List<CompletableFuture<String>> futures = new ArrayList<>();

        // Spawn no index thread
        futures.add(CompletableFuture.supplyAsync( () -> {
            long startTime = System.currentTimeMillis();
            noIndex.runWith(INDEX_BENCHMARK_PATH, true);    // can change to true if want to see cli
            long endTime = System.currentTimeMillis();
            return "Non-Indexed elapsed time:\t%s seconds.".formatted((endTime - startTime) / 1000.);
        }));

        // Spawn index thread
        futures.add(CompletableFuture.supplyAsync( () -> {
            long startTime = System.currentTimeMillis();
            index.runWith(INDEX_BENCHMARK_PATH, true);      // can change to true if want to see cli
            long endTime = System.currentTimeMillis();
            return "    Indexed elapsed time:\t%s seconds.".formatted((endTime - startTime) / 1000.);
        }));

        // Join each thread / wait for each to finish
        futures.forEach(CompletableFuture::join);

        // remove files
        cleanUp(dbRoot +"-no-index");
        cleanUp(dbRoot +"-index");

        // Print results
        for(CompletableFuture<String> result : futures)
            System.out.println(result);

    }

}
