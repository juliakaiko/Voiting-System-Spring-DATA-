package com.itstep.myfirstspringapp.repository;

import com.step.springmvcapp.entity.Admin;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AdminRepositoryImpl implements AdminDAO{

    @PersistenceContext // То же самое, что @Autowired, но только для EntityManager 
    private EntityManager em;
    
    @Override
    public Admin getAdmin() {
        String sql = "from Admin a WHERE a.id=:id"; 
       // Query query = session.createQuery("from Admin c WHERE c.id="+1L);
        return (Admin)this.em.createQuery(sql).setParameter("id",1L).getSingleResult();
    }
    
}
