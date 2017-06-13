package net.geant.nmaas.utils.ssh;

import java.util.function.Predicate;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface Command {

    String asString();

    Predicate<String> isOutputCorrect();

}
