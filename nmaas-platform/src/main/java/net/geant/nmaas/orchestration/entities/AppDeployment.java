package net.geant.nmaas.orchestration.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Details of single application deployment in the system.
 */
@Entity
@Table(name = "app_deployment")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Unique identifier of this deployment. */
    @Column(nullable = false, unique = true)
    private Identifier deploymentId;

    /** Name of the client domain for this deployment. */
    @Column(nullable = false)
    private String domain;

    /** Identifier of the application being deployed. */
    @Column(nullable = false)
    private Identifier applicationId;

    /** Name of the deployment provided by the user. */
    @Column(nullable = false)
    private String deploymentName;

    /** Current deployment state. */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppDeploymentState state = AppDeploymentState.REQUESTED;

    /** Initial application configuration provided by the user. */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AppConfiguration configuration;

    /** Store all of deployment state changes */
    @OneToMany(mappedBy = "app", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private List<AppDeploymentHistory> history = new ArrayList<>();

    /** Indicates if GitLab instance is required during deployment */
    @Column(nullable = false)
    private boolean configFileRepositoryRequired;

    /** Contains information about deployment fails */
    @Lob
    @Type(type = "text")
    private String errorMessage;

    /** Required storage space to be allocated for this particular instance in GB */
    private Integer storageSpace;

    private String owner;

    private String appName;

    public void addChangeOfStateToHistory(AppDeploymentState previousState, AppDeploymentState currentState){
        history.add(new AppDeploymentHistory(this, new Date(), previousState, currentState));
    }

    @Override
    public String toString() {
        return "AppDeployment{" +
                "deploymentId=" + deploymentId +
                ", domain='" + domain + '\'' +
                ", appInstanceName='" + deploymentName + '\'' +
                '}';
    }
}
