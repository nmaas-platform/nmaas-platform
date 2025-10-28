package net.geant.nmaas.portal.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourcesLimitUpdateDto {
    protected Long id;
    protected Integer memory;
    protected Integer cpu;
    protected Integer instancesNo;
    protected Integer containersNo;
}
