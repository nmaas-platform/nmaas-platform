package net.geant.nmaas.orchestration.entities;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode
public class AppAccessMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ServiceAccessMethodType type;

    private String name;

    private String tag;

    @ElementCollection
    @Fetch(FetchMode.SELECT)
    private Map<String, String> deployParameters;

    public AppAccessMethod(ServiceAccessMethodType type, String name, String tag, Map<String, String> deployParameters) {
        this.type = type;
        this.name = name;
        this.tag = tag;
        this.deployParameters = deployParameters;
    }

}
