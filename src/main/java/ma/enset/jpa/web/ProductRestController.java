package ma.enset.jpa.web;

import lombok.AllArgsConstructor;
import ma.enset.jpa.entities.Product;
import ma.enset.jpa.repositories.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class ProductRestController {
    private final ProductRepository productRepository;

    @GetMapping("/products")
    public List<Product> products() {
        return productRepository.findAll();
    }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Produit introuvable : " + id));
    }

    @GetMapping("/products/search")
    public List<Product> search(@RequestParam(defaultValue = "") String keyword) {
        return productRepository.findByNameContains(keyword);
    }
}
