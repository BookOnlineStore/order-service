package com.bookstore.orderservice.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.UUID;

/**
 * The interface Order repository.
 */
@RepositoryRestResource(path = "orders")
public interface OrderRepository extends CrudRepository<Order, UUID> {

    /**
     * Disable save for Spring data rest
     *
     * @param entity the entity need to save
     * @return the page
     */
    @RestResource(exported = false)
    @Override
    <S extends Order> S save(S entity);

    /**
     * Find all by created by page.
     *
     * @param createdBy the created by
     * @param pageable  the pageable
     * @return the page
     */
    @RestResource(path = "by-created-by")
    Page<Order> findAllByCreatedBy(@Param("q") String createdBy, Pageable pageable);

    /**
     * Find all by status page.
     *
     * @param status   the status
     * @param pageable the pageable
     * @return the page
     */
    @RestResource(path = "by-status")
    Page<Order> findAllByStatus(@Param("q") OrderStatus status, Pageable pageable);
}
