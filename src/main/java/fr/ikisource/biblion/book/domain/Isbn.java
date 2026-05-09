package fr.ikisource.biblion.book.domain;

public record Isbn(String value) {

    public Isbn {
        if (value == null) {
            throw new IllegalArgumentException("ISBN must not be null");
        }
        value = value.replaceAll("[\\s-]", "");
        if (!isValidIsbn10(value) && !isValidIsbn13(value)) {
            throw new IllegalArgumentException("Invalid ISBN: " + value);
        }
    }

    private static boolean isValidIsbn10(String s) {
        if (s.length() != 10) return false;
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
            sum += (c - '0') * (10 - i);
        }
        char last = s.charAt(9);
        if (last == 'X' || last == 'x') {
            sum += 10;
        } else if (last >= '0' && last <= '9') {
            sum += last - '0';
        } else {
            return false;
        }
        return sum % 11 == 0;
    }

    private static boolean isValidIsbn13(String s) {
        if (s.length() != 13) return false;
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
            int digit = c - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return sum % 10 == 0;
    }
}
