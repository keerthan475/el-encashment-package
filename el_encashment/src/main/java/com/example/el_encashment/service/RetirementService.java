package com.example.el_encashment.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class RetirementService {

    public LocalDate calculateRetirementDate(LocalDate dob) {

        LocalDate sixtyYearsLater = dob.plusYears(60);

        if (dob.getDayOfMonth() == 1) {
            LocalDate previousMonth = sixtyYearsLater.minusMonths(1);
            return previousMonth.withDayOfMonth(previousMonth.lengthOfMonth());
        } else {
            return sixtyYearsLater.withDayOfMonth(sixtyYearsLater.lengthOfMonth());
        }
    }
}
