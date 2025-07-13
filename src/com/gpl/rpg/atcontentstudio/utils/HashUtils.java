package com.gpl.rpg.atcontentstudio.utils;

import com.zackehh.siphash.SipHash;
import com.zackehh.siphash.SipHashResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class HashUtils {

    private static final Map<String, SipHash> HASHER_CACHE = new LinkedHashMap<String, SipHash>();

    static String siphash(String key, byte[] data) {
        SipHash hasher = HASHER_CACHE.get(key);
        if (hasher == null) {
            hasher = new SipHash("Weblate Sip Hash".getBytes());
            HASHER_CACHE.put(key, hasher);
        }

        if (data != null) {
            SipHashResult result = hasher.hash(data);
            return result.getHex();
        }


        return null;

    }

}
