package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class KeyValueView {

    @Column(name = "key_string")
    private String key;

    @Column(name = "value_string", length = 8000)
    private String value;
}
