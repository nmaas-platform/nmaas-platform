package net.geant.nmaas.notifications.types;

import net.geant.nmaas.notifications.types.model.FormTypeRequest;
import net.geant.nmaas.notifications.types.service.FormTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FormTypeControllerTest {

    private final FormTypeService formTypeService = mock(FormTypeService.class);

    private FormTypeController underTest;

    @BeforeEach
    void setup() {
        underTest = new FormTypeController(formTypeService);
    }

    @Test
    void shouldCallProperServiceMethodWhenGetAll() {
        this.underTest.getAll();
        verify(formTypeService, times(1)).getAll();
    }

    @Test
    void shouldCallProperServiceMethodWhenCreate() {
        FormTypeRequest ftr = new FormTypeRequest();
        this.underTest.create(ftr);
        verify(formTypeService, times(1)).create(any(FormTypeRequest.class));
    }

}
