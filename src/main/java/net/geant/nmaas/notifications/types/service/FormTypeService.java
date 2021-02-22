package net.geant.nmaas.notifications.types.service;

import lombok.AllArgsConstructor;
import net.geant.nmaas.notifications.types.model.FormTypeRequest;
import net.geant.nmaas.notifications.types.model.FormTypeView;
import net.geant.nmaas.notifications.types.persistence.entity.FormType;
import net.geant.nmaas.notifications.types.persistence.repository.FormTypeRepository;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FormTypeService {

    private final FormTypeRepository typeRepository;

    public List<FormTypeView> getAll() {
        return this.typeRepository.findAll().stream().map(
                t -> new FormTypeView(t.getKey(), t.getAccess(), t.getTemplateName())
        ).collect(Collectors.toList());
    }

    public Optional<FormType> findOne(String key) {
        return this.typeRepository.findById(key);
    }

    public void create(FormType ent) {
        if(!this.typeRepository.existsById(ent.getKey())) {
            this.typeRepository.save(ent);
        } else {
            throw new ProcessingException("Form type already exists");
        }
    }

    public void create(FormTypeRequest ftv) {
        this.create(new FormType(ftv.getKey(), ftv.getAccess(), ftv.getTemplateName(), ftv.getEmails(), ftv.getSubject()));
    }
}
