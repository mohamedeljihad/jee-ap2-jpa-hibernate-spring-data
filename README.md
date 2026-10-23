# Activité Pratique N°2 : ORM JPA, Hibernate et Spring Data

**Mohammed ELJIHAD**, Master II-BDCC, ENSET Mohammedia. Module : Architecture JEE et Systèmes Distribués (Pr. Mohamed YOUSSFI).

## Objectif
Mettre en œuvre le mapping objet-relationnel avec **JPA** et son implémentation **Hibernate**, et accéder aux données avec les interfaces **Spring Data JPA** :
- d'abord sur une base **H2** en mémoire ;
- puis sur **MySQL** ;
- avec en complément un modèle plus riche (patients, médecins, rendez-vous, consultations, utilisateurs et rôles).

## Technologies
Java 17, Spring Boot 3.3, Spring Data JPA (Hibernate 6), Spring Web, H2, MySQL Connector/J, Lombok.

## Structure
```
src/main/java/ma/enset/jpa
├── JpaApplication.java          démarrage + CommandLineRunner de test (produits, hôpital, utilisateurs)
├── entities/                    Product, Patient, Medecin, RendezVous, Consultation, StatusRDV, User, Role
├── repositories/                interfaces JpaRepository (Spring Data)
├── service/                     IHospitalService / HospitalServiceImpl, UserService / UserServiceImpl
└── web/                         ProductRestController, PatientRestController
src/main/resources
├── application.properties       H2 (profil par défaut)
└── application-mysql.properties MySQL (profil "mysql")
```

## Partie 1 : produits

### 1-2. Projet Spring Initializr
Dépendances : Spring Data JPA, H2 Database, Spring Web et Lombok, plus MySQL Driver pour l'étape 7.

### 3. Entité JPA Product
```java
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100, nullable = false)
    private String name;
    private double price;
    private int quantity;
}
```
- `@Entity` : la classe est persistante (une table `PRODUCT`).
- `@Id` et `@GeneratedValue` : clé primaire auto-incrémentée.
- Lombok génère les getters, setters, `toString` et constructeurs.

### 4. Unité de persistance (application.properties)
```properties
spring.datasource.url=jdbc:h2:mem:products-db
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
```
Spring Boot crée automatiquement la `DataSource`, l'`EntityManagerFactory` (Hibernate) et le gestionnaire de transactions. Le fichier `persistence.xml` n'est pas nécessaire.

### 5. Interface Spring Data
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContains(String keyword);          // requête dérivée du nom de la méthode
    List<Product> findByPriceGreaterThan(double price);
    @Query("select p from Product p where p.name like :x and p.price > :y")
    List<Product> search(@Param("x") String keyword, @Param("y") double minPrice);   // JPQL
}
```
`JpaRepository` fournit `save`, `findAll`, `findById`, `deleteById`, `count`, la pagination, etc. Spring génère l'implémentation au démarrage.

### 6. Tests des opérations (`CommandLineRunner testProducts`)
| Opération | Méthode utilisée |
|---|---|
| Ajouter des produits | `productRepository.save(new Product(null, "Computer", 12000, 5))`, plus une variante avec `Product.builder()` |
| Consulter tous les produits | `findAll()` |
| Consulter un produit | `findById(1L)` |
| Chercher des produits | `findByNameContains("Smart")` et la requête JPQL `search("%o%", 2000)` |
| Mettre à jour un produit | `findById(2L)`, puis `setPrice` / `setQuantity`, puis `save(p2)` (Hibernate génère un `UPDATE`) |
| Supprimer un produit | `deleteById(3L)` |

Les requêtes SQL générées par Hibernate s'affichent dans la console (`show-sql`). L'endpoint REST `GET /products` (port 8085) renvoie les produits en JSON. La console H2 est accessible sur `http://localhost:8085/h2-console` (JDBC URL : `jdbc:h2:mem:products-db`).

### 7. Migration de H2 vers MySQL
Il suffit de démarrer avec le profil `mysql`. Le code Java ne change pas : seule la configuration de la source de données diffère, ce qui illustre l'intérêt de JPA.
```properties
# application-mysql.properties
spring.datasource.url=jdbc:mysql://localhost:3306/products_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Partie 2 : patients, médecins, rendez-vous, consultations, utilisateurs et rôles

### Modèle et associations
| Association | Mapping JPA |
|---|---|
| Patient 1 --- * RendezVous | `@OneToMany(mappedBy = "patient")` / `@ManyToOne` |
| Medecin 1 --- * RendezVous | `@OneToMany(mappedBy = "medecin")` / `@ManyToOne` |
| RendezVous 1 --- 1 Consultation | `@OneToOne(mappedBy = "rendezVous")` / `@OneToOne` |
| User * --- * Role | `@ManyToMany(mappedBy = "users")` / `@ManyToMany` + `@JoinTable(name = "USERS_ROLES")` |

Choix de mapping :
- `RendezVous.status` est une énumération `StatusRDV` (PENDING, CANCELED, DONE), stockée en texte grâce à `@Enumerated(EnumType.STRING)`.
- L'identifiant des rendez-vous et des utilisateurs est un **UUID** généré par la couche service.
- `@JsonProperty(access = WRITE_ONLY)` coupe les cycles de sérialisation JSON (Patient → RendezVous → Patient…). `@ToString.Exclude` et `@EqualsAndHashCode.Exclude` évitent les récursions de Lombok.

### Couche service (`@Service @Transactional`)
- **`IHospitalService`** : `savePatient`, `saveMedecin`, `saveRendezVous` (génère l'UUID) et `saveConsultation`.
- **`UserService`** : `addNewUser`, `addNewRole`, `addRoleToUser` et `authenticate`. `addRoleToUser` met à jour les deux côtés de l'association. Grâce à `@Transactional`, Hibernate enregistre la table d'association par *dirty checking*, sans appel explicite à `save`.

### Tests (`CommandLineRunner testHospital` et `testUsers`)
- Création de 3 patients et de 3 médecins (spécialité *Cardio* ou *Dentiste*).
- Prise d'un rendez-vous (statut PENDING) entre le patient 1 et le médecin « Yasmine ».
- Enregistrement de la consultation associée.
- Création des utilisateurs `user1` et `admin`, et des rôles STUDENT, USER et ADMIN :
  - `user1` reçoit STUDENT et USER ;
  - `admin` reçoit USER et ADMIN.
- Authentification de `user1` et affichage de ses rôles.

### Endpoints REST
- `GET /products`, `GET /products/{id}`, `GET /products/search?keyword=Smart`
- `GET /patients`
- `GET /users/user1` : l'utilisateur et ses rôles (le mot de passe n'est pas renvoyé)

## Exécution
```bash
mvn spring-boot:run                                       # H2
mvn spring-boot:run -Dspring-boot.run.profiles=mysql      # MySQL (XAMPP/WAMP démarré)
```

## Captures
> Les captures sont à ajouter après exécution dans IntelliJ :
> - console avec les opérations sur les produits ;
> - console H2 avec les tables `PRODUCT`, `PATIENT`, `MEDECIN`, `RENDEZ_VOUS`, `CONSULTATION`, `USERS`, `ROLE` et `USERS_ROLES` ;
> - phpMyAdmin après la migration vers MySQL ;
> - réponses JSON de `/products`, `/patients` et `/users/user1`.

## Conclusion
- JPA sépare le modèle objet de la base : les mêmes entités et repositories fonctionnent sur H2 comme sur MySQL, en changeant seulement la configuration.
- Spring Data JPA supprime le code DAO répétitif : les interfaces suffisent, et les requêtes sont dérivées du nom des méthodes ou écrites en JPQL avec `@Query`.
- Les associations `@OneToMany`, `@ManyToOne`, `@OneToOne` et `@ManyToMany` traduisent directement le modèle métier.
