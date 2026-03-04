package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.service.CodenameValidator;

import java.util.Optional;
import java.util.regex.Pattern;

public class DefaultCodenameValidator implements CodenameValidator {

    private final String pattern;

    public DefaultCodenameValidator(String pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public boolean valid(String codename, Integer lengthLimit) {
        return Optional.of(pattern)
                .map(p -> Pattern.matches(p, codename) && (lengthLimit == null || codename.length() <= lengthLimit))
                .orElse(true);
    }

}
