package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // 기본적인 CRUD 메서드(저장, 조회, 수정, 삭제)는 JpaRepository가 제공하므로 추가 구현이 필요하지 않습니다.
    // 필요한 경우 커스텀 쿼리 메서드를 여기에 정의할 수 있습니다.
}