package net.geant.nmaas.orchestration.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Application configuration in Json format provided by the user.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode
@Table(name = "app_deployment_configuration")
public class AppConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic(fetch = FetchType.EAGER)
    @Lob
    @Type(type = "text")
    @Column(nullable = false)
    private String jsonInput;

    public AppConfiguration(String jsonInput) {
        this.jsonInput = jsonInput;
    }

}
