package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.portal.api.exceptions.BundleNotFoundException;
import net.geant.nmaas.portal.api.exceptions.InvalidBundleException;
import net.geant.nmaas.portal.dto.BundleBasicDto;
import net.geant.nmaas.portal.dto.BundleDescriptionDto;
import net.geant.nmaas.portal.dto.BundleDto;
import net.geant.nmaas.portal.service.BundleService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BundleControllerTest {

    private final BundleService bundleService = mock(BundleService.class);
    private final BundleController controller = new BundleController(bundleService);

    @Test
    void shouldCreateBundle() {
        BundleBasicDto request = basicDto();
        BundleDto created = bundleDto(10L, "Test Bundle", "test-bundle");
        when(bundleService.create(request)).thenReturn(created);

        ResponseEntity<BundleDto> result = controller.create(request);

        assertEquals(201, result.getStatusCode().value());
        assertSame(created, result.getBody());
        verify(bundleService).create(request);
    }

    @Test
    void shouldPropagateErrorWhenCreatedBundleIsInvalid() {
        BundleBasicDto request = basicDto();
        when(bundleService.create(request)).thenThrow(new InvalidBundleException("Bundle must contain at least 2 applications"));

        assertThrows(InvalidBundleException.class, () -> controller.create(request));
    }

    @Test
    void shouldUpdateBundle() {
        BundleBasicDto request = basicDto();
        BundleDto updated = bundleDto(10L, "Updated Bundle", "updated-bundle");
        when(bundleService.update(10L, request)).thenReturn(updated);

        ResponseEntity<BundleDto> result = controller.update(10L, request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(updated, result.getBody());
        verify(bundleService).update(10L, request);
    }

    @Test
    void shouldGetOneBundle() {
        BundleDto bundle = bundleDto(10L, "Test Bundle", "test-bundle");
        when(bundleService.findById(10L)).thenReturn(bundle);

        ResponseEntity<BundleDto> result = controller.getOne(10L);

        assertEquals(200, result.getStatusCode().value());
        assertSame(bundle, result.getBody());
        verify(bundleService).findById(10L);
    }

    @Test
    void shouldPropagateErrorWhenBundleNotFound() {
        when(bundleService.findById(10L)).thenThrow(new BundleNotFoundException(10L));

        assertThrows(BundleNotFoundException.class, () -> controller.getOne(10L));
    }

    @Test
    void shouldGetAllBundles() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<BundleBasicDto> page = new PageImpl<>(List.of(basicDto()));
        when(bundleService.findAll(pageable)).thenReturn(page);

        Page<BundleBasicDto> result = controller.getAll(pageable);

        assertSame(page, result);
        verify(bundleService).findAll(pageable);
    }

    @Test
    void shouldGetAllBundlesByApplication() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<BundleBasicDto> page = new PageImpl<>(List.of(basicDto()));
        when(bundleService.findByApplication(1L, pageable)).thenReturn(page);

        Page<BundleBasicDto> result = controller.getAllByApplication(1L, pageable);

        assertSame(page, result);
        verify(bundleService).findByApplication(1L, pageable);
    }

    @Test
    void shouldGetAllBundlesByApplicationRaw() {
        List<BundleDto> bundles = List.of(bundleDto(10L, "Test Bundle", "test-bundle"));
        when(bundleService.findByApplication(1L)).thenReturn(bundles);

        ResponseEntity<List<BundleDto>> result = controller.getAllByApplication(1L);

        assertEquals(200, result.getStatusCode().value());
        assertSame(bundles, result.getBody());
        verify(bundleService).findByApplication(1L);
    }

    @Test
    void shouldSearchBundlesByName() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<BundleBasicDto> page = new PageImpl<>(List.of(basicDto()));
        when(bundleService.findByName("test", pageable)).thenReturn(page);

        Page<BundleBasicDto> result = controller.searchByName("test", pageable);

        assertSame(page, result);
        verify(bundleService).findByName("test", pageable);
    }

    @Test
    void shouldReturnTrueWhenApplicationIsInBundle() {
        when(bundleService.isApplicationInBundle(1L)).thenReturn(true);

        ResponseEntity<Boolean> result = controller.isApplicationInBundle(1L);

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody());
        verify(bundleService).isApplicationInBundle(1L);
    }

    @Test
    void shouldReturnFalseWhenApplicationIsNotInBundle() {
        when(bundleService.isApplicationInBundle(1L)).thenReturn(false);

        ResponseEntity<Boolean> result = controller.isApplicationInBundle(1L);

        assertEquals(200, result.getStatusCode().value());
        assertFalse(result.getBody());
        verify(bundleService).isApplicationInBundle(1L);
    }

    private BundleBasicDto basicDto() {
        return new BundleBasicDto(
                null,
                "Test Bundle",
                "test-bundle",
                List.of(new BundleDescriptionDto(null, "en", "brief", "full description")),
                Set.of(1L, 2L)
        );
    }

    private BundleDto bundleDto(Long id, String name, String codeName) {
        return new BundleDto(
                id,
                name,
                codeName,
                List.of(new BundleDescriptionDto(100L, "en", "brief", "full description")),
                Set.of()
        );
    }

}
