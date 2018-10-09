package net.geant.nmaas.externalservices.inventory.network;

import net.geant.nmaas.externalservices.inventory.network.entities.DomainNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.exceptions.AttachPointNotFoundException;
import net.geant.nmaas.externalservices.inventory.network.repositories.DomainNetworkAttachPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/management/domains")
public class DomainManagerController {

    private DomainNetworkAttachPointRepository attachPointRepository;

    @Autowired
    public DomainManagerController(DomainNetworkAttachPointRepository attachPointRepository) {
        this.attachPointRepository = attachPointRepository;
    }

    @GetMapping(value = "/{domainName}/network")
    public DomainNetworkAttachPoint getDomainNetworkAttachPoint(@PathVariable("domainName") String domainName) {
        return attachPointRepository
                .findByDomain(domainName)
                .orElseThrow(() -> new AttachPointNotFoundException(String.valueOf(domainName)));
    }

    @PostMapping(value = "/{domainName}/network", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void setOrUpdateDomainNetworkAttachPoint(@RequestBody DomainNetworkAttachPoint domainNetworkAttachPoint) {
        Optional<DomainNetworkAttachPoint> queryResult = attachPointRepository.findByDomain(domainNetworkAttachPoint.getDomain());
        DomainNetworkAttachPoint attachPoint;
        if (queryResult.isPresent()) {
            attachPoint = queryResult.get().update(domainNetworkAttachPoint);
        } else {
            attachPoint = domainNetworkAttachPoint;
        }
        attachPointRepository.save(attachPoint);
    }

    @DeleteMapping(value = "/{domainName}/network")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeDomainNetworkAttachPoint(
            @PathVariable("domainName") String domainName) {
        DomainNetworkAttachPoint dnap = attachPointRepository
                .findByDomain(domainName)
                .orElseThrow(() -> new AttachPointNotFoundException(String.valueOf(domainName)));
        attachPointRepository.delete(dnap);
    }

    @ExceptionHandler(AttachPointNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAttachPointNotFoundException (AttachPointNotFoundException ex) {
        return "Did not find attach point with provided identifier -> " + ex.getMessage();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String handleDataAccessException(DataAccessException ex) {
        return "Couldn't complete requested operation -> " + ex.getMessage();
    }

}
