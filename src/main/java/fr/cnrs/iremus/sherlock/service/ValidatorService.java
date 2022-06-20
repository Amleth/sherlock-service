package fr.cnrs.iremus.sherlock.service;

import jakarta.inject.Singleton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class ValidatorService {

    private final Pattern hexColorPattern = Pattern.compile("^([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");;
    private final Pattern unicodePattern = Pattern.compile("^.$");;
    private Matcher matcher;

    public boolean isHexColorCode(final String hexColorCode) {
        matcher = hexColorPattern.matcher(hexColorCode);
        return matcher.matches();
    }

    public boolean isUnicodePattern(final String unicodeChar) {
        matcher = unicodePattern.matcher(unicodeChar);
        return matcher.matches();
    }
}
