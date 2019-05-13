package net.geant.nmaas.portal.persistent.entity;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ApplicationBase {

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
    private List<ApplicationVersion> versions = new ArrayList<>();

    public ApplicationBase(String name){
        this.name = name;
    }

    public ApplicationBase(Long id, String name){
        this(name);
        this.id = id;
    }

    public void validate(){
        checkArgument(StringUtils.isNotEmpty(name), "App must have name");
        checkArgument(name.matches("^[a-zA-Z0-9-]+$"), "Name contains illegal characters");
        checkArgument(descriptions != null && !descriptions.isEmpty(), "Descriptions cannot be null or empty");
        checkArgument(versions.stream().map(ApplicationVersion::getVersion).allMatch(new HashSet<>()::add), "App versions must be unique");
    }
}
