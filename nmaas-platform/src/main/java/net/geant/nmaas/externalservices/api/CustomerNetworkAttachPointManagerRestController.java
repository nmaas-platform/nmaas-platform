package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.inventory.network.BasicCustomerNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.exceptions.AttachPointNotFoundException;
import net.geant.nmaas.externalservices.inventory.network.repositories.BasicCustomerNetworkAttachPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/network")
public class CustomerNetworkAttachPointManagerRestController {

    private BasicCustomerNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository;

    @Autowired
    public CustomerNetworkAttachPointManagerRestController(
            BasicCustomerNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository) {
        this.basicCustomerNetworkAttachPointRepository = basicCustomerNetworkAttachPointRepository;
    }

    @RequestMapping(
            value = "/customernetworks",
            method = RequestMethod.GET)
    public List<BasicCustomerNetworkAttachPoint> listAllBasicCustomerNetworkAttachPoints() {
        return basicCustomerNetworkAttachPointRepository.findAll();
    }

    @RequestMapping(
            value = "/customernetworks/{customerid}",
            method = RequestMethod.GET)
    public BasicCustomerNetworkAttachPoint getBasicCustomerNetworkAttachPoint(
            @PathVariable("customerid") long customerId) throws AttachPointNotFoundException {
        return basicCustomerNetworkAttachPointRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new AttachPointNotFoundException(String.valueOf(customerId)));
    }

    @RequestMapping(
            value = "/customernetworks",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addBasicCustomerNetworkAttachPoint(
            @RequestBody BasicCustomerNetworkAttachPoint basicCustomerNetworkAttachPoint) throws DataAccessException {
        basicCustomerNetworkAttachPointRepository.save(basicCustomerNetworkAttachPoint);
    }

    @RequestMapping(
            value = "/customernetworks/{customerid}",
            method = RequestMethod.PUT,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateBasicCustomerNetworkAttachPoint(
            @PathVariable("customerid") long customerId,
            @RequestBody BasicCustomerNetworkAttachPoint modifiedBasicCustomerNetworkAttachPoint)
            throws AttachPointNotFoundException, DataAccessException {
        BasicCustomerNetworkAttachPoint currentBasicCustomerNetworkAttachPoint = basicCustomerNetworkAttachPointRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new AttachPointNotFoundException(String.valueOf(customerId)));
        currentBasicCustomerNetworkAttachPoint.setRouterName(modifiedBasicCustomerNetworkAttachPoint.getRouterName());
        currentBasicCustomerNetworkAttachPoint.setRouterId(modifiedBasicCustomerNetworkAttachPoint.getRouterId());
        currentBasicCustomerNetworkAttachPoint.setRouterInterfaceName(modifiedBasicCustomerNetworkAttachPoint.getRouterInterfaceName());
        currentBasicCustomerNetworkAttachPoint.setRouterInterfaceUnit(modifiedBasicCustomerNetworkAttachPoint.getRouterInterfaceUnit());
        currentBasicCustomerNetworkAttachPoint.setRouterInterfaceVlan(modifiedBasicCustomerNetworkAttachPoint.getRouterInterfaceVlan());
        currentBasicCustomerNetworkAttachPoint.setBgpLocalIp(modifiedBasicCustomerNetworkAttachPoint.getBgpLocalIp());
        currentBasicCustomerNetworkAttachPoint.setBgpNeighborIp(modifiedBasicCustomerNetworkAttachPoint.getBgpNeighborIp());
        currentBasicCustomerNetworkAttachPoint.setAsNumber(modifiedBasicCustomerNetworkAttachPoint.getAsNumber());
        basicCustomerNetworkAttachPointRepository.save(currentBasicCustomerNetworkAttachPoint);
    }

    @RequestMapping(
            value = "/customernetworks/{customerid}",
            method = RequestMethod.DELETE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeBasicCustomerNetworkAttachPoint(
            @PathVariable("customerid") long customerId) throws AttachPointNotFoundException, DataAccessException {
        Long id = basicCustomerNetworkAttachPointRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new AttachPointNotFoundException(String.valueOf(customerId)))
                .getId();
        basicCustomerNetworkAttachPointRepository.delete(id);
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
