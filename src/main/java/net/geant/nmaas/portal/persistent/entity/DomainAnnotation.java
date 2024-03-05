package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "domain_annotations",  uniqueConstraints = {@UniqueConstraint(columnNames = {"key_string"})})
public class DomainAnnotation {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
    
    @Column(name = "key_string")
    private String key = null;
    @Column(name = "value_string")
    private String value = null;
}
