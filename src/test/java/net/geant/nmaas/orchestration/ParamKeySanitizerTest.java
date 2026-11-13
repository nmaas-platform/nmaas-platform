package net.geant.nmaas.orchestration;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ParamKeySanitizerTest {

    @Test
    void shouldReplaceDotsWithTagsWhenRestoringKeys() {
        Map<String, String> input = new HashMap<>();
        input.put("keywith.", "value1");
        input.put("keywith.inthemiddle", "value2");
        Map<String, String> output = ParamKeySanitizer.restoreKeys(input);
        assertThat(output.size(), is(2));
        assertThat(output.get("keywith_dot_"), is(equalTo("value1")));
        assertThat(output.get("keywith_dot_inthemiddle"), is(equalTo("value2")));
    }

    @Test
    void shouldReplaceBracketsWithTagsWhenRestoringKeys() {
        Map<String, String> input = new HashMap<>();
        input.put("targets[0].Addresses[0].ipAddress", "1.1.1.1:1000");
        Map<String, String> output = ParamKeySanitizer.restoreKeys(input);
        assertThat(output.size(), is(1));
        assertThat(output.get("targets_bracket_0_bracket__dot_Addresses_bracket_0_bracket__dot_ipAddress"), is(equalTo("1.1.1.1:1000")));
    }

    @Test
    void shouldConvertBracketsIndependentlyOfPairing() {
        Map<String, String> input = new HashMap<>();
        input.put("keywith[", "value");
        Map<String, String> output = ParamKeySanitizer.restoreKeys(input);
        assertThat(output.get("keywith_bracket_"), is(equalTo("value")));
    }

    @Test
    void shouldReturnEmptyMapForNullParameters() {
        assertThat(ParamKeySanitizer.restoreKeys(null).isEmpty(), is(true));
    }

    @Test
    void shouldSanitizeTagsIntoDotsAndBrackets() {
        Map<String, String> input = new HashMap<>();
        input.put("keywith_dot_", "value1");
        input.put("keywith_dot_inthemiddle", "value2");
        Map<String, String> output = DefaultAppLifecycleManager.preprocessParameters(input);
        assertThat(output.size(), is(2));
        assertThat(output.keySet().containsAll(java.util.Arrays.asList("keywith.", "keywith.inthemiddle")), is(true));
    }

    @Test
    void shouldCountBrackets() {
        assertThat(ParamKeySanitizer.countBrackets("keywith_bracket_and_bracket_"), is(2));
        assertThat(ParamKeySanitizer.countBrackets("keywith_bracket_"), is(1));
        assertThat(ParamKeySanitizer.countBrackets("plainkey"), is(0));
    }

}
