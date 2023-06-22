package net.geant.nmaas.notifications.types.service;

import net.geant.nmaas.notifications.types.model.FormTypeRequest;
import net.geant.nmaas.notifications.types.persistence.entity.FormType;
import net.geant.nmaas.notifications.types.persistence.repository.FormTypeRepository;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FormTypeServiceTest {

    private final FormTypeRepository repository = mock(FormTypeRepository.class);

    private FormTypeService underTest;

    @BeforeEach
    void setup() {
        this.underTest = new FormTypeService(repository);
    }

    @Test
    void createShouldPersistNewEntityIfKeyDoesNotExist() {
        when(repository.existsById(anyString())).thenReturn(false);
        FormTypeRequest ftr = new FormTypeRequest("CONTACT", "", "", new ArrayList<>(), "");
        this.underTest.create(ftr);

        verify(repository, times(1)).save(any(FormType.class));
    }

    @Test
    void createShouldNotPersistIfKeyAlreadyExists() {
        when(repository.existsById(anyString())).thenReturn(true);
        FormTypeRequest ftr = new FormTypeRequest("CONTACT", "", "", new ArrayList<>(), "");

        assertThrows(ProcessingException.class, () -> this.underTest.create(ftr));

        verify(repository, times(0)).save(any(FormType.class));
    }

    @Test
    void getAllShouldCallProperRepositoryMethod() {
        when(repository.findAll()).thenReturn(
                Arrays.asList(
                        new FormType("CONTACT", "", "", new ArrayList<>(), ""),
                        new FormType("ISSUE", "", "", new ArrayList<>(), "")
                )
        );

        underTest.getAll();

        verify(repository, times(1)).findAll();
    }

    @Test
    void findOneShouldCallProperRepositoryMethod() {
        when(repository.findById(anyString())).thenReturn(Optional.of(new FormType("CONTACT", "", "", new ArrayList<>(), "")));

        underTest.findOne("CONTACT");

        verify(repository, times(1)).findById(anyString());
    }

}
