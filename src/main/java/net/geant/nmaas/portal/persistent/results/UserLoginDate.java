package net.geant.nmaas.portal.persistent.results;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLoginDate {
    private long userId;
    private OffsetDateTime minLoginDate;
    private OffsetDateTime maxLoginDate;
}
