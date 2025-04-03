package net.geant.nmaas.utils.bash;

import java.util.function.Predicate;

public interface Command {

    String asString();

    Predicate<String> isOutputCorrect();

}
