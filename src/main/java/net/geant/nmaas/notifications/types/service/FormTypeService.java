package net.geant.nmaas.notifications.types.service;

import lombok.AllArgsConstructor;
import net.geant.nmaas.notifications.types.model.FormTypeRequest;
import net.geant.nmaas.notifications.types.model.FormTypeView;
import net.geant.nmaas.notifications.types.persistence.entity.FormType;
import net.geant.nmaas.notifications.types.persistence.repository.FormTypeRepository;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.DataConflictException;
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
                t -> new FormTypeView(t.getKeyValue(), t.getAccess(), t.getTemplateName())
        ).collect(Collectors.toList());
    }

    public Optional<FormType> findOne(String key) {
        return this.typeRepository.findById(key);
    }

    public void create(FormType ent) {
        if(!this.typeRepository.existsById(ent.getKeyValue())) {
            this.typeRepository.save(ent);
        } else {
            throw new DataConflictException(String.format("Form type %s already exists", ent.getTemplateName()));
        }
    }

    public void create(FormTypeRequest ftv) {
        this.create(new FormType(ftv.getKey(), ftv.getAccess(), ftv.getTemplateName(), ftv.getEmails(), ftv.getSubject()));
    }
}
