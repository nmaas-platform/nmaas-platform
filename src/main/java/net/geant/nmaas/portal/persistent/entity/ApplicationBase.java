package net.geant.nmaas.portal.persistent.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ApplicationBase implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true)
    @EqualsAndHashCode.Include
    private String name;

    private String license;
    private String licenseUrl;

    private String wwwUrl;
    private String sourceUrl;
    private String issuesUrl;
    private String nmaasDocumentationUrl;

    @Column(nullable = false)
    private String owner;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    private FileInfo logo;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<FileInfo> screenshots = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AppDescription> descriptions;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "application_base_tag", joinColumns = @JoinColumn(name = "application_base_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "application")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ApplicationVersion> versions = new HashSet<>();

    public ApplicationBase(String name) {
        this.name = name;
    }

    public ApplicationBase(Long id, String name) {
        this(name);
        this.id = id;
    }

    public ApplicationBase(long id, String name, Set<Tag> tags, List<AppDescription> descriptions) {
        this(name);
        this.id = id;
        this.tags = tags;
        this.descriptions = descriptions;
    }

    public void validate() {
        Validate.isTrue(StringUtils.isNotEmpty(name), "App must have name");
        Validate.isTrue(name.matches("^[a-zA-Z0-9- ]+$"), "Name contains illegal characters");
        Validate.isTrue(descriptions != null && !descriptions.isEmpty(), "Descriptions cannot be null or empty");
    }

}
