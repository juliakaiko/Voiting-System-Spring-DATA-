package com.step.springmvcapp.controller;

import com.step.springmvcapp.entity.Candidate;
import com.step.springmvcapp.entity.CandidateDetails;
import com.step.springmvcapp.entity.Elector;
import com.step.springmvcapp.entity.User;
import com.step.springmvcapp.service.VoitingServiceImpl;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller 
@SessionAttributes("user")
@ComponentScan(basePackages = "com.step.springmvcapp")
public class AdminController {
    
    @Autowired
    private VoitingServiceImpl service;
    
    private static final String REGISTRATION = "electors/registration";
    private static final String PROFILE = "electors/elector_profile";
    private static final String ADMIN= "users/admin";
    private static final String ELECTORS_LIST="electors/electors_list";
    private static final String CANDIDATE_FORM="candidates/editPage";
    
    @GetMapping(value = "/Electors")
    public String showElectors(@ModelAttribute("user") User user, ModelMap model) {
        List <Elector> electors = service.findElectors();
        if (electors.isEmpty()) {
            model.addAttribute("message", "The list of electors is empty");
            return ADMIN;
        }
        model.addAttribute("electors_list",  electors);
        return ELECTORS_LIST;
    }
    
    @GetMapping(value = "/Electors/edit/{id}")
    public ModelAndView editPageForElector(@PathVariable("id") Long id,
                                 @ModelAttribute("message") String message,
                                 HttpSession httpSession ) {
        Elector elector = service.findElectorById(id);
        ModelAndView modelAndView = new ModelAndView();
        
        User user =(User)httpSession.getAttribute("user");
        User admin = service.getAdmin();

        if (elector.isVoted()==true){
            modelAndView.addObject("novoting", "You can't change your data after you've voted");
            modelAndView.addObject("electorProfile", elector);
            modelAndView.setViewName(PROFILE);
        } else {
            modelAndView.addObject("elector", elector);
            modelAndView.setViewName(REGISTRATION);
        }
        if (user.getLogin().equals(admin.getLogin())&
            user.getPassword().equals(admin.getPassword())) {
            modelAndView.addObject("elector", elector);
            modelAndView.setViewName(REGISTRATION);
        }
        return modelAndView;
    }

    @PostMapping (value = "/Electors/edit/{id}")
    public ModelAndView editElector(@PathVariable("id") Long id,
            @ModelAttribute("elector") Elector elector, HttpSession httpSession,BindingResult result,
    @RequestParam String login, @RequestParam String password, @RequestParam String firstName, 
    @RequestParam String lastName, @RequestParam Integer age, @RequestParam String passportSeries,
    @RequestParam String passportNum) {
        ModelAndView modelAndView = new ModelAndView();
        if (result.hasErrors()) {
            modelAndView.setViewName(REGISTRATION);
        }         
        User user =(User)httpSession.getAttribute("user");
        User admin = service.getAdmin();
        if (user.getLogin().equals(admin.getLogin())&
            user.getPassword().equals(admin.getPassword())   ) {
            Elector electorEdit = this.service.findElectorById(id);
            Candidate candidate = electorEdit.getCandidate();
            boolean vote = electorEdit.isVoted();
            elector= new Elector (firstName,lastName,age,passportSeries,passportNum,login,password);
            //this.service.deleteElectorById(id);
            elector.setCandidate(candidate);
            elector.setVoted(vote);
            elector.setId(id);           
            this.service.saveElector(elector);
            //this.service.update(id,elector);  
            modelAndView.addObject("message","Data has been successfully changed");
            modelAndView.setViewName("redirect:/Electors/");
        } else {         
            elector= new Elector (firstName,lastName,age,passportSeries,passportNum,login,password);
            //this.service.update(id,elector);
            elector.setId(id);
            this.service.saveElector(elector);
            modelAndView.setViewName(PROFILE);
            modelAndView.addObject("electorProfile", elector);
        }
        return modelAndView;
    }   
    
    @PostMapping(value = "/Electors/delete/{id}")
    public String deleteElector(@PathVariable("id") Long id, ModelMap model) {//@ModelAttribute("message") String message,
        Elector elector = service.findElectorById(id);
        service.deleteElectorById(id);
        model.addAttribute("message", elector.getFirstName() +" "+elector.getLastName()+ " has been deleted");
        return "redirect:/Electors/";
    }

    @GetMapping(value = "/Candidates/edit/{id}")
    public ModelAndView editPageForCandidate(@PathVariable("id") Long id,
                                 HttpSession httpSession ) {
        Candidate candidate = service.findCandidateById(id);
        ModelAndView modelAndView = new ModelAndView();
        
        User user =(User)httpSession.getAttribute("user");
        User admin = service.getAdmin();
        if (user.getLogin().equals(admin.getLogin())&
            user.getPassword().equals(admin.getPassword())) {
            modelAndView.addObject("candidate", candidate);
            modelAndView.setViewName(CANDIDATE_FORM); 
            return modelAndView;
        } 
        modelAndView.addObject("message", "This feature is available for only  administrator");
        modelAndView.setViewName("redirect:/Candidates/");       
        return modelAndView;
    }
    
    @PostMapping (value = "/Candidates/edit/{id}")
    public String editCandidate(@PathVariable("id") Long id,@ModelAttribute("candidate") Candidate cand,
        BindingResult result,ModelMap model, @RequestParam String firstName, 
        @RequestParam String lastName, @RequestParam Integer age, @RequestParam Long voices) {      
        if (result.hasErrors()) {
            model.addAttribute(CANDIDATE_FORM);
        }  
        CandidateDetails details=  service.findCandidateDetailsById(id);//candidate.getDetails();
        details.setCandidate(cand);
        //details.setId(id);
        this.service.saveCandidateDetails(details);
        cand = new Candidate (firstName,lastName, age, voices);
        cand.setId(id);
        cand.setDetails(details);   
        this.service.saveCandidate(cand); 
        model.addAttribute("message","Data has been successfully changed");
        return "redirect:/Candidates/";
    }
    
    
    @PostMapping(value = "/Candidates/delete/{id}")
    public String deleteCandidate(@PathVariable("id") Long id, ModelMap model,HttpSession httpSession ) {//@ModelAttribute("message") String message,
        Candidate candidate = service.findCandidateById(id);
 
        User user =(User)httpSession.getAttribute("user");
        User admin = service.getAdmin();
        if (user.getLogin().equals(admin.getLogin())&
            user.getPassword().equals(admin.getPassword())) {
            service.deleteCandidateById(id);
            model.addAttribute("message", candidate.getFirstName() +" "+candidate.getLastName()+ " has been deleted");
        } else {
            model.addAttribute("message", "This feature is available for only  administrator");
        }
        return "redirect:/Candidates/";
    }
    
    @GetMapping(value="/CandidateDetails/edit/{id}")
    public ModelAndView editPageForCandidateDetails(@PathVariable("id") Long id,HttpSession httpSession ) {    
        ModelAndView modelAndView = new ModelAndView();
        CandidateDetails details = service.findCandidateDetailsById(id);
        
        User user =(User)httpSession.getAttribute("user");
        User admin = service.getAdmin();
        if (user.getLogin().equals(admin.getLogin())&
            user.getPassword().equals(admin.getPassword())) { 
            modelAndView.addObject("details", details);
            modelAndView.setViewName("candidates/editPageForDetails");
        } 
        else {
            modelAndView.addObject("message", "This feature is available for only  administrator");
            modelAndView.setViewName("redirect:/Candidates/"+id);
        }
        return modelAndView;
    }
  
    @PostMapping (value="/CandidateDetails/edit/{id}")
    public ModelAndView editCandidateDetails(@PathVariable("id") Long id,
            @ModelAttribute("details") CandidateDetails details, 
            ModelMap model,BindingResult result, @RequestParam String education, 
        @RequestParam Integer annualIncome, @RequestParam String property, String electionPromises) {
        ModelAndView modelAndView = new ModelAndView();
        if (result.hasErrors()) {
            modelAndView.setViewName("redirect:/Candidates/"+details.getId());
        }  
        details = new CandidateDetails (education, annualIncome, property, electionPromises); 
        details.setId(id);
        details.setCandidate(service.findCandidateById(id));
        this.service.saveCandidateDetails(details);
        modelAndView.addObject("message","Data has been successfully changed");
        modelAndView.setViewName("redirect:/Candidates/"+details.getId());
        return modelAndView;
    }
    
     @GetMapping(value = "/Candidates/add")
    public String addCandidate(ModelMap model, HttpSession httpSession) {
        Candidate candidate = new Candidate();
        User user =(User)httpSession.getAttribute("user");
        User admin = service.getAdmin();

        if (user.getLogin().equals(admin.getLogin())&
            user.getPassword().equals(admin.getPassword())) {
            model.addAttribute("candidate", candidate);
            return CANDIDATE_FORM;
        }
        model.addAttribute("message", "This feature is available for only  administrator");
        return "redirect:/Candidates/";
    }

    @PostMapping(value = "/Candidates/add")
    public String saveCandidate(@Valid Candidate candidate,BindingResult result, 
            ModelMap model,  @RequestParam String firstName, @RequestParam String lastName, 
        @RequestParam Integer age, @RequestParam Long voices) {
        if (result.hasErrors()) {
            return REGISTRATION;
        }
        if (!service.findCandidates().contains(candidate)) {
            candidate = new Candidate (firstName,lastName, age, voices);
            this.service.saveCandidate(candidate);
        } else {
            model.addAttribute("failed_registration", "Such candidate is registered in the system");
        }
        return "redirect:/Candidates/";
    }
}
