package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

}