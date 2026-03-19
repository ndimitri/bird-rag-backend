//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.dto;

import java.util.Map;

public record RetrievedResult(
    String text,
    double score,
    Map<String, Object> metadata) {
}
