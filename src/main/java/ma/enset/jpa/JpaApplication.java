package ma.enset.jpa;

import ma.enset.jpa.entities.*;
import ma.enset.jpa.repositories.*;
import ma.enset.jpa.service.IHospitalService;
import ma.enset.jpa.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
public class JpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaApplication.class, args);
    }

    /** Partie 1 (étape 6) : tests des opérations sur les produits. */
    @Bean
    CommandLineRunner testProducts(ProductRepository productRepository) {
        return args -> {
            System.out.println("========== Ajouter des produits ==========");
            productRepository.save(new Product(null, "Computer", 12000, 5));
            productRepository.save(new Product(null, "Printer", 1500, 12));
            productRepository.save(new Product(null, "Smart Phone", 5400, 20));
            productRepository.save(Product.builder().name("Smart Watch").price(2300).quantity(7).build());

            System.out.println("========== Consulter tous les produits ==========");
            productRepository.findAll().forEach(System.out::println);

            System.out.println("========== Consulter un produit (id=1) ==========");
            Product product = productRepository.findById(1L).orElse(null);
            System.out.println(product);

            System.out.println("========== Chercher les produits contenant 'Smart' ==========");
            productRepository.findByNameContains("Smart").forEach(System.out::println);

            System.out.println("========== Chercher (JPQL) nom like '%o%' et prix > 2000 ==========");
            productRepository.search("%o%", 2000).forEach(System.out::println);

            System.out.println("========== Mettre à jour le produit id=2 ==========");
            Product p2 = productRepository.findById(2L).orElseThrow();
            p2.setPrice(1350);
            p2.setQuantity(15);
            productRepository.save(p2);
            System.out.println(productRepository.findById(2L).orElseThrow());

            System.out.println("========== Supprimer le produit id=3 ==========");
            productRepository.deleteById(3L);
            System.out.println("Produits restants : " + productRepository.count());
            productRepository.findAll().forEach(System.out::println);
        };
    }

    /** Partie 2 (étape 8) : patients, médecins, rendez-vous et consultations. */
    @Bean
    CommandLineRunner testHospital(IHospitalService hospitalService, PatientRepository patientRepository,
                                   MedecinRepository medecinRepository, RendezVousRepository rendezVousRepository) {
        return args -> {
            Stream.of("Mohamed", "Hassan", "Najat").forEach(name -> {
                Patient patient = new Patient();
                patient.setNom(name);
                patient.setDateNaissance(new Date());
                patient.setMalade(false);
                patient.setScore(100);
                hospitalService.savePatient(patient);
            });
            Stream.of("Aymane", "Hanane", "Yasmine").forEach(name -> {
                Medecin medecin = new Medecin();
                medecin.setNom(name);
                medecin.setEmail(name.toLowerCase() + "@gmail.com");
                medecin.setSpecialite(Math.random() > 0.5 ? "Cardio" : "Dentiste");
                hospitalService.saveMedecin(medecin);
            });

            Patient patient = patientRepository.findById(1L).orElse(null);
            Patient patient1 = patientRepository.findByNom("Mohamed");
            Medecin medecin = medecinRepository.findByNom("Yasmine");

            RendezVous rendezVous = new RendezVous();
            rendezVous.setDate(new Date());
            rendezVous.setStatus(StatusRDV.PENDING);
            rendezVous.setMedecin(medecin);
            rendezVous.setPatient(patient);
            RendezVous savedRDV = hospitalService.saveRendezVous(rendezVous);
            System.out.println("Rendez-vous enregistré : " + savedRDV.getId());

            RendezVous rendezVous1 = rendezVousRepository.findAll().get(0);
            Consultation consultation = new Consultation();
            consultation.setDateConsultation(new Date());
            consultation.setRendezVous(rendezVous1);
            consultation.setRapport("Rapport de la consultation .....");
            hospitalService.saveConsultation(consultation);

            System.out.println("Patient 1 = " + patient + " / même patient par nom = " + patient1.getNom());
            System.out.println("Patients : " + patientRepository.count() + ", médecins : " + medecinRepository.count());
        };
    }

    /** Partie 2 (étape 8) : utilisateurs et rôles (relation ManyToMany). */
    @Bean
    CommandLineRunner testUsers(UserService userService) {
        return args -> {
            User u = new User();
            u.setUsername("user1");
            u.setPassword("123456");
            userService.addNewUser(u);

            User u2 = new User();
            u2.setUsername("admin");
            u2.setPassword("123456");
            userService.addNewUser(u2);

            Stream.of("STUDENT", "USER", "ADMIN").forEach(r -> {
                Role role1 = new Role();
                role1.setRoleName(r);
                userService.addNewRole(role1);
            });

            userService.addRoleToUser("user1", "STUDENT");
            userService.addRoleToUser("user1", "USER");
            userService.addRoleToUser("admin", "USER");
            userService.addRoleToUser("admin", "ADMIN");

            try {
                User user = userService.authenticate("user1", "123456");
                System.out.println("Authentification réussie : " + user.getUserId() + " / " + user.getUsername());
                System.out.print("Rôles : ");
                user.getRoles().forEach(r -> System.out.print(r.getRoleName() + " "));
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
