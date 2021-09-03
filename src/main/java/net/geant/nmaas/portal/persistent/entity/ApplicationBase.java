package net.geant.nmaas.portal.persistent.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

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

    public ApplicationBase(String name){
        this.name = name;
    }

    public ApplicationBase(Long id, String name){
        this(name);
        this.id = id;
    }

    public void validate(){
        checkArgument(StringUtils.isNotEmpty(name), "App must have name");
        checkArgument(name.matches("^[a-zA-Z0-9- ]+$"), "Name contains illegal characters");
        checkArgument(descriptions != null && !descriptions.isEmpty(), "Descriptions cannot be null or empty");
    }
}
