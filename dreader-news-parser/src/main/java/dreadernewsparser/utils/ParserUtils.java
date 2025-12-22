package dreadernewsparser.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class ParserUtils {

    public static String getRootUrl(String fullUrl) {
        try {
            URI uri = new URI(fullUrl);
            return uri.getScheme() + "://" + uri.getHost();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + fullUrl, e);
        }
    }
}