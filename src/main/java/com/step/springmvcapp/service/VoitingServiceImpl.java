package com.step.springmvcapp.service;

import com.itstep.myfirstspringapp.repository.AdminRepository;
import com.itstep.myfirstspringapp.repository.CandidateDetailsRepository;
import com.itstep.myfirstspringapp.repository.CandidateRepository;
import com.itstep.myfirstspringapp.repository.ElectorRepository;
import com.step.springmvcapp.entity.Admin;
import com.step.springmvcapp.entity.Candidate;
import com.step.springmvcapp.entity.CandidateDetails;
import com.step.springmvcapp.entity.Elector;
import java.util.Collection;
import java.util.List;
import javax.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
//@Service
public class VoitingServiceImpl implements VoitingService { 
    
    @Autowired
    private CandidateRepository candidateDao;
    @Autowired
    private ElectorRepository electorDao;
    @Autowired
    private CandidateDetailsRepository detailsDao;
    @Autowired
    private AdminRepository adminDao;

    public VoitingServiceImpl() {}     

    @Override
    @Transactional(readOnly = true) // в 10 раз быстрее будет работать, только для чтения
    public List<Candidate> findCandidates() {
        return candidateDao.findAll();
    }

    @Override
    @Transactional(readOnly = true) // в 10 раз быстрее будет работать, только для чтения
    public Candidate findCandidateById(Long id) {
        return candidateDao.findOne(id);
    }

    @Override
    @Transactional // добавляет транзакцию!
    public void saveCandidate(Candidate candidate) {
        candidateDao.save(candidate);
    }

    @Override
    @Transactional
    public void addAllCandidates(Collection<Candidate> candidates) {
        for (Candidate c: candidates){
            candidateDao.save(c);
        }
    }

    @Override
    @Transactional
    public void deleteCandidateById(Long id) {
       candidateDao.delete(id);
    }

    @Override
    @Transactional(readOnly = true) // в 10 раз быстрее будет работать, только для чтения
    public List <Elector> findElectors() {
       return electorDao.findAll();
    }

    @Override
    @Transactional(readOnly = true) // в 10 раз быстрее будет работать, только для чтения
    public Elector findElectorById(Long id) {
        return electorDao.findOne(id);        
    }

    @Override
    @Transactional
    public void saveElector(Elector elector) {
        electorDao.save(elector);
       
    }
    
    //@Transactional
    @Override
    public Elector update (Long id, Elector elector){
        List <Elector> electors = findElectors();
        for(Elector e: electors) {
            if(e.getId().equals(id)) {
                electorDao.delete(id);
                elector.setId(id);
                electorDao.save(e);
                return elector;
            }
        }
        return null;
    }  


    @Override
    @Transactional
    public void addAllElectors(Collection<Elector> electors) {
        for (Elector e: electors){
            electorDao.save(e);
        }
    }

    @Override
    @Transactional
    public void deleteElectorById(Long id) {
        Elector elector = findElectorById(id);
        //take one vote away from a candidate when removing the elector
        if (elector.isVoted()==true){
            Candidate candidate = candidateDao.findOne(elector.getCandidate().getId());
            candidate.getElectors().remove(elector);
            Long voices = candidate.getVoices()-1L;
            candidate.setVoices(voices);
            candidateDao.save(candidate);
            elector.setCandidate(null);
        }
        try{
            electorDao.delete(id);
        }catch (NoResultException nre){}   
    }
    
    @Override
    @Transactional
    public void vote (Long electorId, Long candidateId){
        Elector elector = findElectorById(electorId);
        Candidate candidate =  findCandidateById(candidateId);      
        Long voices = candidate.getVoices();
        if (voices==null)
           voices = 0L; 
        voices++;
        candidate.setVoices(voices);
        candidate.getElectors().add(elector);
        elector.setCandidate(candidate);
        elector.setVoted(true);
        saveCandidate(candidate);
        saveElector(elector);
    }
    
    @Transactional
    @Override
    public void saveCandidateDetails(CandidateDetails details) {
        detailsDao.save(details);
    }
    
    @Transactional (readOnly = true)
    @Override
    public CandidateDetails findCandidateDetailsById(Long id){
         return detailsDao.findOne(id);
    }
    
    @Transactional (readOnly = true)
    @Override
    public Admin getAdmin(){
        return adminDao.getAdmin();
    }
 
}
