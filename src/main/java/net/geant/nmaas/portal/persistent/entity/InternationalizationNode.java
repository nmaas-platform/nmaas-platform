package net.geant.nmaas.portal.persistent.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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
    @Column(length = 1024)
    private String content;

}
