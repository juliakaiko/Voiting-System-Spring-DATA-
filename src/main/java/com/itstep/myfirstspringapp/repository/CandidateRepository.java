package com.itstep.myfirstspringapp.repository;

import com.step.springmvcapp.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository <Candidate, Long> {
    
}
