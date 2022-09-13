package com.itstep.myfirstspringapp.repository;

import com.step.springmvcapp.entity.CandidateDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateDetailsRepository extends JpaRepository <CandidateDetails, Long> {
    
}
