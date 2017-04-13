package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.inventory.vpnconfigs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RESTful API for managing Ansible playbook VPN configurations for client and cloud side.
 *
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/vpnconfigs")
public class AnsiblePlaybookVpnConfRestController {

    private AnsiblePlaybookVpnConfigRepository repository;

    @Autowired
    public AnsiblePlaybookVpnConfRestController(AnsiblePlaybookVpnConfigRepository repository) {
        this.repository = repository;
    }

    /**
     * Lists all {@link AnsiblePlaybookVpnConfig} cloud instances
     * @return {@link Map} of {@link AnsiblePlaybookVpnConfig} instances by DockerHost name as key
     */
    @RequestMapping(
            value = "/cloud",
            method = RequestMethod.GET)
    public Map<String, AnsiblePlaybookVpnConfig> listAllCloudVpnConfigs() {
        return repository.loadAllCloudVpnConfigs();
    }

    /**
     * Lists all {@link AnsiblePlaybookVpnConfig} customer instances
     * @return {@link Map} of {@link AnsiblePlaybookVpnConfig} instances by customer id as key
     */
    @RequestMapping(
            value = "/customer",
            method = RequestMethod.GET)
    public Map<Long, AnsiblePlaybookVpnConfig> listAllCustomerVpnConfigs() {
        return repository.loadAllClientVpnConfigs();
    }

    /**
     * Fetches {@link AnsiblePlaybookVpnConfig} cloud instance by DockerHost name
     * @param hostName Unique DockerHost name
     * @return {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigNotFoundException when configuration does not exists (HttpStatus.NOT_FOUND)
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "/cloud/{hostname}",
            method = RequestMethod.GET)
    public AnsiblePlaybookVpnConfig getCloudVpnConfig(
            @PathVariable("hostname") String hostName)
            throws AnsiblePlaybookVpnConfigNotFoundException, AnsiblePlaybookVpnConfigInvalidException {
        return repository.loadCloudVpnConfigByDockerHost(hostName);
    }

    /**
     * Fetches {@link AnsiblePlaybookVpnConfig} customer instance by customer id
     * @param customerId Unique customer id
     * @return {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigNotFoundException when configuration does not exists (HttpStatus.NOT_FOUND)
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "/customer/{customerid}",
            method = RequestMethod.GET)
    public AnsiblePlaybookVpnConfig getCustomerVpnConfig(
            @PathVariable("customerid") long customerId)
            throws AnsiblePlaybookVpnConfigNotFoundException, AnsiblePlaybookVpnConfigInvalidException {
        return repository.loadCustomerVpnConfigByCustomerId(customerId);
    }

    /**
     * Stores {@link AnsiblePlaybookVpnConfig} cloud instance
     * @param hostName Unique Docker host name
     * @param newVpnConfig New {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigExistsException when configuration for Docker host exists (HttpStatus.CONFLICT)
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "/cloud/{hostname}",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addCloudVpnConfig(
            @PathVariable("hostname") String hostName,
            @RequestBody AnsiblePlaybookVpnConfig newVpnConfig)
            throws AnsiblePlaybookVpnConfigExistsException, AnsiblePlaybookVpnConfigInvalidException {
        repository.addCloudVpnConfig(hostName, newVpnConfig);
    }

    /**
     * Stores {@link AnsiblePlaybookVpnConfig} customer instance
     * @param customerId Unique customer id
     * @param newVpnConfig New {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigExistsException when configuration for customer id exists (HttpStatus.CONFLICT)
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "/customer/{customerid}",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addCustomerVpnConfig(
            @PathVariable("customerid") long customerId,
            @RequestBody AnsiblePlaybookVpnConfig newVpnConfig)
            throws AnsiblePlaybookVpnConfigExistsException, AnsiblePlaybookVpnConfigInvalidException {
        repository.addCustomerVpnConfig(customerId, newVpnConfig);
    }

    /**
     * Updates {@link AnsiblePlaybookVpnConfig} cloud instance
     * @param hostName Unique Docker host name
     * @param newVpnConfig New {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigNotFoundException when configuration does not exists (HttpStatus.NOT_FOUND)
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "/cloud/{hostname}",
            method = RequestMethod.PUT,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateCloudVpnConfig(
            @PathVariable("hostname") String hostName,
            @RequestBody AnsiblePlaybookVpnConfig newVpnConfig)
            throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigNotFoundException {
        repository.updateCloudVpnConfig(hostName, newVpnConfig);
    }

    /**
     * Updates {@link AnsiblePlaybookVpnConfig} customer instance
     * @param customerId Unique customer id
     * @param newVpnConfig New {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigNotFoundException when configuration does not exists (HttpStatus.NOT_FOUND)
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "/customer/{customerid}",
            method = RequestMethod.PUT,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateCustomerVpnConfig(
            @PathVariable("customerid") long customerId,
            @RequestBody AnsiblePlaybookVpnConfig newVpnConfig)
            throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigNotFoundException {
        repository.updateCustomerVpnConfig(customerId, newVpnConfig);
    }

    /**
     * Removes {@link AnsiblePlaybookVpnConfig} cloud instance
     * @param hostName Unique Docker host name
     * @throws AnsiblePlaybookVpnConfigNotFoundException  when configuration does not exists (HttpStatus.NOT_FOUND)
     */
    @RequestMapping(
            value = "/cloud/{hostname}",
            method = RequestMethod.DELETE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeCloudVpnConfig(
            @PathVariable("hostname") String hostName)
            throws AnsiblePlaybookVpnConfigNotFoundException {
        repository.removeCloudVpnConfig(hostName);
    }

    /**
     * Removes {@link AnsiblePlaybookVpnConfig} cloud instance
     * @param consumerId Unique consumer id
     * @throws AnsiblePlaybookVpnConfigNotFoundException  when configuration does not exists (HttpStatus.NOT_FOUND)
     */
    @RequestMapping(
            value = "/customer/{customerid}",
            method = RequestMethod.DELETE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeConsumerVpnConfig(
            @PathVariable("customerid") long consumerId)
            throws AnsiblePlaybookVpnConfigNotFoundException {
        repository.removeClientVpnConfig(consumerId);
    }

    @ExceptionHandler(AnsiblePlaybookVpnConfigNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAnsiblePlaybookVpnConfigNotFoundException (AnsiblePlaybookVpnConfigNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(AnsiblePlaybookVpnConfigExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleAnsiblePlaybookVpnConfigExistsException(AnsiblePlaybookVpnConfigExistsException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(AnsiblePlaybookVpnConfigInvalidException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String handleAnsiblePlaybookVpnConfigInvalidException(AnsiblePlaybookVpnConfigInvalidException ex) {
        return ex.getMessage();
    }
}
