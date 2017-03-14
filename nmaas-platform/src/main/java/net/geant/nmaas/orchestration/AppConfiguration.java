package net.geant.nmaas.orchestration;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppConfiguration {

    private Identifier applicationId;

    private String jsonInput;

    public AppConfiguration() {}

    public AppConfiguration(Identifier applicationId, String jsonInput) {
        this.applicationId = applicationId;
        this.jsonInput = jsonInput;
    }

    public void setApplicationId(Identifier applicationId) {
        this.applicationId = applicationId;
    }

    public void setJsonInput(String jsonInput) {
        this.jsonInput = jsonInput;
    }

    public Identifier getApplicationId() {
        return applicationId;
    }

    public String getJsonInput() {
        return jsonInput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppConfiguration that = (AppConfiguration) o;

        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null)
            return false;
        return jsonInput != null ? jsonInput.equals(that.jsonInput) : that.jsonInput == null;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (jsonInput != null ? jsonInput.hashCode() : 0);
        return result;
    }
}
