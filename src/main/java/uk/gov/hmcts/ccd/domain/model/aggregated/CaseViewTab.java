package uk.gov.hmcts.ccd.domain.model.aggregated;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseViewTab {
    private String id;
    private String label;
    private Integer order;
    private CaseViewField[] fields;
    @JsonProperty("show_condition")
    private String showCondition;

    public CaseViewTab() {
        // default constructor
    }

    public CaseViewTab(String id, String label, Integer order, CaseViewField[] fields, String showCondition) {
        this.id = id;
        this.label = label;
        this.order = order;
        this.fields = fields;
        this.showCondition = showCondition;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public CaseViewField[] getFields() {
        return fields;
    }

    public void setFields(CaseViewField[] fields) {
        this.fields = fields;
    }

    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }
}
