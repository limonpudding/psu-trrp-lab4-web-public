package trrp.lab4.dto;


import java.io.Serializable;

public class EgrulInfoDTO {

    private String  ogrn;
    private String activityCode;
    private String description;
    private Boolean isMain;
    private Integer id;

    public EgrulInfoDTO() {}

    public EgrulInfoDTO(String ogrn, String activityCode, Boolean isMain, Integer id) {
        this.ogrn = ogrn;
        this.activityCode = activityCode;
        this.isMain = isMain;
        this.id = id;
    }

    public String getOgrn() {
        return this.ogrn;
    }

    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }

    public String getActivityCode() {
        return this.activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public Boolean getIsMain() {
        return this.isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
