package net.geant.nmaas.notifications.types.persistence.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class FormType {
    private static final String SPLIT_CHAR = ";";

    @Id
    @Setter(value = AccessLevel.PROTECTED)
    private String key; // unique identifier
    @Column(nullable = false)
    private String access;
    @Column(nullable = false)
    private String templateName;

    @Column
    @Getter(value = AccessLevel.PROTECTED)
    @Setter(value = AccessLevel.PROTECTED)
    private String emails;

    public FormType(String key, String access, String templateName, List<String> emails) {
        this.key = key;
        this.access = access;
        this.templateName = templateName;
        this.setEmailsList(emails);
    }

    public List<String> getEmailsList() {
        return Arrays.asList(this.emails.split(SPLIT_CHAR));
    }

    public void setEmailsList(List<String> emails) {
        this.emails = String.join(SPLIT_CHAR, emails);
    }

    public String getId() {
        return this.key;
    }
}
