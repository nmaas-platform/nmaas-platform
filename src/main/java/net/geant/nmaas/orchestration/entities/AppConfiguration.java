package net.geant.nmaas.orchestration.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

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
//    @Type(type = "text")
    @Column(nullable = false)
    private String jsonInput;

    public AppConfiguration(String jsonInput) {
        this.jsonInput = jsonInput;
    }

}
