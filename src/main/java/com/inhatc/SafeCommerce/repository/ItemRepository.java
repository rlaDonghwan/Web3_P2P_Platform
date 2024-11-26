package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.isDeleted = false")
    List<Item> findAllActiveItems();

    @Query("SELECT i.user.id FROM Item i WHERE i.itemId = :itemId AND i.isDeleted = false")
    Long findAuthorIdByItemId(@Param("itemId") Long itemId);

    @Query("SELECT i FROM Item i WHERE i.itemId = :itemId AND i.isDeleted = false")
    Optional<Item> findActiveItemById(@Param("itemId") Long itemId);
}