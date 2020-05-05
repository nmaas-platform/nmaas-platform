package net.geant.nmaas.portal.api.shell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ShellCommandRequest {
    private String command;
    private String signal;
}
