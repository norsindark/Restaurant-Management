package com.restaurant_management.services.impls;

import com.restaurant_management.dtos.CategoryDto;
import com.restaurant_management.entites.Category;
import com.restaurant_management.enums.StatusType;
import com.restaurant_management.exceptions.DataExitsException;
import com.restaurant_management.payloads.responses.ApiResponse;
import com.restaurant_management.payloads.responses.CategoryResponse;
import com.restaurant_management.repositories.CategoryRepository;
import com.restaurant_management.services.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Component
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final PagedResourcesAssembler<CategoryResponse> pagedResourcesAssembler;


    @Override
    public PagedModel<EntityModel<CategoryResponse>> getAllCategories(int pageNo, int pageSize, String sortBy)
            throws DataExitsException {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Order.asc(sortBy)));

        Page<Category> pagedResult = categoryRepository.findAll(paging);

        if (pagedResult.hasContent()) {
            return pagedResourcesAssembler.toModel(pagedResult.map(CategoryResponse::new));
        } else {
            throw new DataExitsException("No Category found");
        }
    }

    @Override
    public ApiResponse addCategory(CategoryDto categoryDto) throws DataExitsException {
        Optional<Category> category = categoryRepository.findByName(categoryDto.getName());
        if (category.isPresent()) {
            throw new DataExitsException("Category already exists");
        }
        Category newCategory = new Category();
        newCategory.setName(categoryDto.getName());
        newCategory.setStatus(StatusType.INACTIVE.toString());
        categoryRepository.save(newCategory);
        return new ApiResponse("Category added successfully", HttpStatus.CREATED);
    }

    @Override
    public ApiResponse updateCategory(CategoryDto categoryDto) throws DataExitsException {
        Optional<Category> category = categoryRepository.findById(categoryDto.getId());
        if (category.isEmpty()) {
            throw new DataExitsException("Category not found");
        }
        Optional<Category> categoryName = categoryRepository.findByName(categoryDto.getName());
        if (categoryName.isPresent()) {
            throw new DataExitsException(categoryDto.getName() + " already exists");
        }
        Category _category = category.get();
        _category.setName(categoryDto.getName());
        categoryRepository.save(_category);
        return new ApiResponse("Category updated successfully", HttpStatus.OK);
    }

    @Override
    public ApiResponse deleteCategory(String id) throws DataExitsException {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new DataExitsException("Category not found");
        }
        categoryRepository.deleteById(id);
        return new ApiResponse("Category deleted successfully", HttpStatus.OK);
    }
}
