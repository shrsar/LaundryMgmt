package com.laundrymgmt.modern.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

@Component
public class CodeFactory {

    public String generate(String prefix, Predicate<String> exists) {
        for (int attempt = 0; attempt < 50; attempt++) {
            String candidate = prefix + "-" + ThreadLocalRandom.current().nextInt(100000, 1_000_000);
            if (!exists.test(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique code for " + prefix);
    }
}
