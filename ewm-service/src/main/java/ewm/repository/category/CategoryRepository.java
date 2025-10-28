package ewm.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import ewm.model.category.Category;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}