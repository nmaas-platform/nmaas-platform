package net.geant.nmaas.notifications.types;

import lombok.AllArgsConstructor;
import net.geant.nmaas.notifications.types.model.FormTypeRequest;
import net.geant.nmaas.notifications.types.model.FormTypeView;
import net.geant.nmaas.notifications.types.service.FormTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mail/type")
@AllArgsConstructor
public class FormTypeController {

    private final FormTypeService service;

    @GetMapping
    public List<FormTypeView> getAll() {
        return this.service.getAll();
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody @Valid FormTypeRequest request) {
        this.service.create(request);
    }
}
