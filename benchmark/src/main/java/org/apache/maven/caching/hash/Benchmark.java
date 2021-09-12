package org.apache.maven.caching.hash;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Benchmark {
    public static void main(String[] args) {
        // warmup
        String wpath = "C:/zah/warmup/";
        for (int i=0;i<10;i++) {
            for (HashFactory factory: HashFactory.values()) {
                hashFiles(factory, wpath, true);
            }
        }

        String path = "C:/zah/data/";
        for (int i=0;i<100;i++) {
            for (HashFactory factory: HashFactory.values()) {
                hashFiles(factory, path, false);
            }
        }
    }

    public static void hashFiles(HashFactory factory, String path, boolean warmup) {
        final File directory = new File(path + factory.getAlgorithm());
        final File[] listOfFiles = directory.listFiles();
        final HashChecksum checksum = factory.createChecksum(listOfFiles.length);
        final List<String> hashes = new ArrayList<>(listOfFiles.length+1);

        long time = System.nanoTime();
        for (final File fileEntry : listOfFiles) {
            try {
                hashes.add(checksum.update(fileEntry.toPath()));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        hashes.add(checksum.digest());
        long diff = System.nanoTime() - time;
        if (!warmup) {
            try {
                Files.write(Paths.get(factory.getAlgorithm()), Collections.singletonList(String.valueOf(TimeUnit.NANOSECONDS.toMillis(diff))), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
