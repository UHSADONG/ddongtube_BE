package com.uhsadong.ddtube.global.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {

    // TODO: Random은 정말 Random인가?
    private static final Random random = new Random();
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    public static String generateShortId(int length) {
        return NanoIdUtils.randomNanoId(random, ALPHABET, length);
    }

}
