package poc.java.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;
import poc.java.oauth.authorizationservice.UrlUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UrlUtilsTest {

    @Test
    void parseQueryStringTest() {
        var expected = Map.of("test2", List.of("1", "2"),
                "test", List.of("1,2"),
                "test0", List.of("0")
        );
        assertThat(UrlUtils.parseQueryString("test2=1&test=1,2&test2=2&test0=0")).isEqualTo(MultiValueMap.fromMultiValue(expected));
    }

}