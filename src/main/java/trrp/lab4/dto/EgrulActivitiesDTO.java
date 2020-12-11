package trrp.lab4.dto;

public class EgrulActivitiesDTO {


    private Integer code;
    private String description;

    public EgrulActivitiesDTO() {
    }

    public EgrulActivitiesDTO(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
