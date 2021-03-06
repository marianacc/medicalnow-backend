package com.ucb.medicalnow.Model;

import java.sql.Date;

public class PrescriptionDateListModel {

    private Integer prescriptionId;
    private Date prescriptionDate;

    public PrescriptionDateListModel() {
    }

    public PrescriptionDateListModel(Integer prescriptionId, Date prescriptionDate) {
        this.prescriptionId = prescriptionId;
        this.prescriptionDate = prescriptionDate;
    }

    public Integer getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Integer prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Date getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(Date prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    @Override
    public String toString() {
        return "PrescriptionModel{" +
                "prescriptionId=" + prescriptionId +
                ", prescriptionDate=" + prescriptionDate +
                '}';
    }
}
