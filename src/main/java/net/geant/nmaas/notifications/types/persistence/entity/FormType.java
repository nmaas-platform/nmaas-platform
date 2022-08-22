package net.geant.nmaas.notifications.types.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String keyValue; // unique identifier
    @Column(nullable = false)
    private String access;
    @Column(nullable = false)
    private String templateName;

    @Column
    @Getter(value = AccessLevel.PROTECTED)
    @Setter(value = AccessLevel.PROTECTED)
    private String emails;

    @Column(nullable = false)
    private String subject;

    public FormType(String keyValue, String access, String templateName, List<String> emails, String subject) {
        this.keyValue = keyValue;
        this.access = access;
        this.templateName = templateName;
        this.setEmailsList(emails);
        this.subject = subject;
    }

    public List<String> getEmailsList() {
        return Arrays.asList(this.emails.split(SPLIT_CHAR));
    }

    public void setEmailsList(List<String> emails) {
        this.emails = String.join(SPLIT_CHAR, emails);
    }

    public String getId() {
        return this.keyValue;
    }
}
