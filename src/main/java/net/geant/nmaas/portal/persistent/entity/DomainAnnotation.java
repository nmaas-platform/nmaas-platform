package net.geant.nmaas.portal.persistent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
