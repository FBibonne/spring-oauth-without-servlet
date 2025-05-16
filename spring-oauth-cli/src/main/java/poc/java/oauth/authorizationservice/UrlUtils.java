package poc.java.oauth.authorizationservice;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.StringTokenizer;

public final class UrlUtils {
    private UrlUtils() {}


    public static MultiValueMap<String, String> parseQueryString(String queryString) {
        MultiValueMap<String, String> ht = new LinkedMultiValueMap<>();
        StringTokenizer st = new StringTokenizer(queryString, "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                ht.add(pair.toLowerCase(), "");
            } else {
                ht.add(pair.substring(0, pos).toLowerCase(),
                        pair.substring(pos + 1));
            }
        }
        return ht;
    }
}
