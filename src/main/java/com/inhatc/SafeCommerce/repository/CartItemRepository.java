package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Transactional
    void deleteByItem_ItemId(Long itemId); // `item` 필드에서 `id`를 참조하도록 수정

}