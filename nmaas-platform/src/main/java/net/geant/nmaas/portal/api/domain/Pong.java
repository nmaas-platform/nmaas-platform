package net.geant.nmaas.portal.api.domain;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Pong {
	Date timestamp;
	String username;

}
