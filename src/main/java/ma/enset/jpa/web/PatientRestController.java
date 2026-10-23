package ma.enset.jpa.web;

import lombok.AllArgsConstructor;
import ma.enset.jpa.entities.Patient;
import ma.enset.jpa.entities.User;
import ma.enset.jpa.repositories.PatientRepository;
import ma.enset.jpa.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class PatientRestController {
    private final PatientRepository patientRepository;
    private final UserService userService;

    @GetMapping("/patients")
    public List<Patient> patients() {
        return patientRepository.findAll();
    }

    @GetMapping("/users/{username}")
    public User user(@PathVariable String username) {
        return userService.findUserByUserName(username);
    }
}
