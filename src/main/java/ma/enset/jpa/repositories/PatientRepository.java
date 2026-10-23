package ma.enset.jpa.repositories;

import ma.enset.jpa.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByNom(String nom);
    List<Patient> findByMaladeTrue();
}
