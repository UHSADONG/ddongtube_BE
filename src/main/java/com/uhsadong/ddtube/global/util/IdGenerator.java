package com.uhsadong.ddtube.global.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.security.SecureRandom;

public class IdGenerator {

    // TODO: Random은 정말 Random인가?
    private static final SecureRandom random = new SecureRandom();
    // 혼동되는 문자열은 제거
    private static final char[] ALPHABET = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    public static String generateShortId(int length) {
        return NanoIdUtils.randomNanoId(random, ALPHABET, length);
    }

}
