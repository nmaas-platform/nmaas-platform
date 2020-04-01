package net.geant.nmaas.portal.persistent.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class InternationalizationNode {

    @NotNull
    @Column(name = "node_key")
    private String key;

    @NotNull
    private String content;
}
