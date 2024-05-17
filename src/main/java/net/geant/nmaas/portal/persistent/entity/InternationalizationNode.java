package net.geant.nmaas.portal.persistent.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

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
