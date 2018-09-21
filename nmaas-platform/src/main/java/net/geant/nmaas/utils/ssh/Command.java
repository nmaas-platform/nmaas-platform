package net.geant.nmaas.utils.ssh;

import java.util.function.Predicate;

public interface Command {

    String asString();

    Predicate<String> isOutputCorrect();

}
