package net.geant.nmaas.portal.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableReferenceTest {

    @Test
    void shouldDetectVariableReference() {
        assertTrue(VariableReference.isReference("${var:proxy.url}"));
        assertTrue(VariableReference.isReference(" ${var:proxy.url} "));
    }

    @Test
    void shouldNotTreatStaticValueAsReference() {
        assertFalse(VariableReference.isReference("https://proxy.example.com"));
        assertFalse(VariableReference.isReference("%RANDOM_STRING_20%"));
        assertFalse(VariableReference.isReference("${var:}"));
        assertFalse(VariableReference.isReference(null));
    }

    @Test
    void shouldExtractVariableName() {
        assertEquals(Optional.of("proxy.url"), VariableReference.extractName("${var:proxy.url}"));
        assertEquals(Optional.of("API_PASSWORD"), VariableReference.extractName("${var:API_PASSWORD}"));
    }

    @Test
    void shouldNotExtractNameFromStaticValue() {
        assertEquals(Optional.empty(), VariableReference.extractName("plain-value"));
        assertEquals(Optional.empty(), VariableReference.extractName(null));
    }

}
