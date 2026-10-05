package net.geant.nmaas.portal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "variable",
        uniqueConstraints = @UniqueConstraint(columnNames = {"domain_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Variable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Variable name, unique within the given scope (instance or domain).
     */
    @NotNull
    @Column(nullable = false)
    private String name;

    /**
     * Variable value. Never exposed for {@link VariableType#SECRET} variables.
     */
    @Column(nullable = false, columnDefinition = "text")
    private String value;

    /**
     * Timestamp (epoch millis) of the last modification of the variable value.
     * Initially set to the variable creation time; only updated when the
     * stored value is overwritten with new content.
     */
    @Column(name = "last_modified", nullable = false)
    private Long lastModified;

    /**
     * Standard (unmaskable) or secret (never unmaskable) variable.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VariableType type;

    /**
     * Scope of the variable. When null, the variable is defined at the instance
     * (global) level by a system admin. Otherwise, it belongs to the given domain
     * and takes precedence over an instance level variable with the same name.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private Domain domain;

    public boolean isGlobal() {
        return domain == null;
    }

}
