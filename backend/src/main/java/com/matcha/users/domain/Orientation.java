package com.matcha.users.domain;

public enum Orientation {
    hetero, homo, bi, unspecified;

    public static Orientation fromString(String s) {
        if (s == null) return unspecified;
        try { return Orientation.valueOf(s.trim().toLowerCase()); }
        catch (IllegalArgumentException e) { return unspecified; }
    }
}
