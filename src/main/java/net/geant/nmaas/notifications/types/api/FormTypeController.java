package net.geant.nmaas.notifications.types.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import net.geant.nmaas.notifications.types.model.FormTypeDto;
import net.geant.nmaas.notifications.types.model.FormTypeRequest;
import net.geant.nmaas.notifications.types.service.FormTypeService;
import net.geant.nmaas.portal.exceptions.DataConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/${nmaas.api.version:v1}/mail/type")
@AllArgsConstructor
@Tag(name = "Contact Forms", description = "Contact forms management API")
public class FormTypeController {

    private final FormTypeService service;

    @GetMapping
    public List<FormTypeDto> getAll() {
        return this.service.getAll();
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createContactFormType(@RequestBody @Valid FormTypeRequest request) {
        this.service.create(request);
    }

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public String handleDataConfigException(DataConflictException e) {
        return e.getMessage();
    }
}
