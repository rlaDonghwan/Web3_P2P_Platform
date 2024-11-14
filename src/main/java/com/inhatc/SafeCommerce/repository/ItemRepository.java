package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {


    @Query("SELECT i.user.id FROM Item i WHERE i.itemId = :itemId")
    Long findAuthorIdByItemId(@Param("itemId") Long itemId);

}