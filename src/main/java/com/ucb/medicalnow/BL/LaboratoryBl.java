package com.ucb.medicalnow.BL;

import com.ucb.medicalnow.DAO.LaboratoryDao;
import com.ucb.medicalnow.Model.LaboratoryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class LaboratoryBl {

    private LaboratoryDao laboratoryDao;

    @Autowired
    public LaboratoryBl (LaboratoryDao laboratoryDao) {this.laboratoryDao = laboratoryDao; }

    public ArrayList<LaboratoryModel> returnAllLaboratoriesByPatient(int userId) {
        return this.laboratoryDao.returnAllLaboratoriesByPatient(userId);
    }

    public ArrayList<LaboratoryModel> returnAllLaboratoriesByDoctor(int userId) {
        return this.laboratoryDao.returnAllLaboratoriesByDoctor(userId);
    }
}
