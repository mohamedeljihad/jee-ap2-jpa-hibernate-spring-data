package ma.enset.jpa.repositories;

import ma.enset.jpa.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // requêtes dérivées du nom de la méthode
    List<Product> findByNameContains(String keyword);
    List<Product> findByPriceGreaterThan(double price);
    // requête JPQL explicite
    @Query("select p from Product p where p.name like :x and p.price > :y")
    List<Product> search(@Param("x") String keyword, @Param("y") double minPrice);
}
