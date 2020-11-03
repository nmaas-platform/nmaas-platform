package net.geant.nmaas.notifications.types;

import net.geant.nmaas.notifications.types.model.FormTypeRequest;
import net.geant.nmaas.notifications.types.service.FormTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class FormTypeControllerTest {

    private final FormTypeService formTypeService = mock(FormTypeService.class);

    private FormTypeController underTest;

    @BeforeEach
    public void setup() {
        underTest = new FormTypeController(formTypeService);
    }

    @Test
    public void shouldCallProperServiceMethodWhenGetAll() {
        this.underTest.getAll();
        verify(formTypeService, times(1)).getAll();
    }

    @Test
    public void shouldCallProperServiceMethodWhenCreate() {
        FormTypeRequest ftr = new FormTypeRequest();
        this.underTest.create(ftr);
        verify(formTypeService, times(1)).create(any(FormTypeRequest.class));
    }
}
