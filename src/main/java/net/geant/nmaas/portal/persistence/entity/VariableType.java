package net.geant.nmaas.portal.persistence.entity;

public enum VariableType {

    /**
     * Standard/environment variable. Its value can be presented to the user in unmasked form.
     */
    STANDARD,

    /**
     * Secret variable (e.g. password). Its value can never be unmasked.
     * To change it, the user needs to overwrite it with new content.
     */
    SECRET

}
